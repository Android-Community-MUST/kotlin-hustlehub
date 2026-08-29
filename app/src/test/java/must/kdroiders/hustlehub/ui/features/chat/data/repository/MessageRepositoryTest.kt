package must.kdroiders.hustlehub.ui.features.chat.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.core.security.CryptoManager
import must.kdroiders.hustlehub.core.security.KeyExchangeHandler
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.MessageDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.MessageEntity
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.toDecryptedDomain
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ChatWebSocketService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ConversationApiService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.MessageResponse
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MessageRepositoryTest {
    private val context: Context = mockk(relaxed = true)
    private val conversationApiService: ConversationApiService = mockk(relaxed = true)
    private val conversationDao: ConversationDao = mockk(relaxed = true)
    private val messageDao: MessageDao = mockk(relaxed = true)
    private val chatWebSocketService: ChatWebSocketService = mockk(relaxed = true)
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val firebaseUser: FirebaseUser = mockk(relaxed = true)
    private val keyExchangeHandler: KeyExchangeHandler = mockk(relaxed = true)
    private val cryptoManager: CryptoManager = CryptoManager()

    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setup() {
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "current-user-uid"

        // Mock keyExchangeHandler to return a real key for local testing
        val keyGen = javax.crypto.KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val testSecret = keyGen.generateKey()
        every { keyExchangeHandler.getOrGenerateLocalSecret(any()) } returns testSecret
        every { keyExchangeHandler.getCachedSecret(any()) } returns testSecret

        repository = ChatRepositoryImpl(
            context = context,
            conversationApiService = conversationApiService,
            conversationDao = conversationDao,
            messageDao = messageDao,
            chatWebSocketService = chatWebSocketService,
            firebaseAuth = firebaseAuth,
            keyExchangeHandler = keyExchangeHandler,
            cryptoManager = cryptoManager,
        )
    }

    @Test
    fun `loadMessageHistory fetches messages and caches them in Room`() =
        runTest {
            val mockMessages = listOf(
                MessageResponse(
                    id = "msg-1",
                    conversationId = "conv-1",
                    senderId = "other-user",
                    type = "TEXT",
                    content = "Hello there",
                    mediaUrl = null,
                    thumbnailUrl = null,
                    metadata = null,
                    timestamp = "2026-08-20T10:00:00Z",
                    deliveredAt = null,
                    readAt = null,
                ),
            )

            coEvery {
                conversationApiService.getMessages("conv-1", 0, 50)
            } returns ApiResponse(
                success = true,
                message = "Success",
                data = PageResponse(
                    content = mockMessages,
                    page = 0,
                    size = 50,
                    totalElements = 1L,
                    totalPages = 1,
                ),
            )

            val result = repository.loadMessageHistory("conv-1", 0)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { messageDao.upsertAll(any()) }
        }

    @Test
    fun `getMessages returns decrypted flow of messages from Room`() =
        runTest {
            val entities = listOf(
                MessageEntity(
                    id = "msg-1",
                    conversationId = "conv-1",
                    senderId = "other-user",
                    type = "TEXT",
                    content = "Hi there!",
                    mediaUrl = null,
                    thumbnailUrl = null,
                    metadata = null,
                    timestamp = "2026-08-20T10:00:00Z",
                    deliveredAt = null,
                    readAt = null,
                ),
            )

            every { messageDao.getByConversation("conv-1") } returns flowOf(entities)

            var result = emptyList<must.kdroiders.hustlehub.ui.features.chat.domain.model.Message>()
            repository.getMessages("conv-1").collect {
                result = it
            }

            assertEquals(1, result.size)
            assertEquals("msg-1", result[0].id)
            assertEquals("Hi there!", result[0].content)
            assertEquals(MessageType.TEXT, result[0].type)
        }

    @Test
    fun `sendMessage via WebSocket saves message to local database`() =
        runTest {
            coEvery {
                chatWebSocketService.sendMessage(any())
            } returns Unit

            val result = repository.sendMessage("conv-1", MessageType.TEXT, "How much for haircut?")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { messageDao.upsert(any()) }
        }

    @Test
    fun `sendMessage when offline keeps message in local database as pending unsynced`() =
        runTest {
            coEvery { chatWebSocketService.connect() } throws IllegalStateException("STOMP session not initialized")

            val result = repository.sendMessage("conv-1", MessageType.TEXT, "Offline message test")

            assertTrue(result.isSuccess)
            val slot = io.mockk.slot<MessageEntity>()
            coVerify(atLeast = 1) { messageDao.upsert(capture(slot)) }
            org.junit.Assert.assertFalse(slot.captured.isSynced)
            org.junit.Assert.assertFalse(slot.captured.isFailed)
            assertEquals("Offline message test", slot.captured.content)
        }

    @Test
    fun `resendUnsyncedMessages flushes pending messages over WebSocket`() =
        runTest {
            val pendingEntity = MessageEntity(
                id = "temp_123",
                conversationId = "conv-1",
                senderId = "current-user-uid",
                type = "TEXT",
                content = "Pending message",
                mediaUrl = null,
                thumbnailUrl = null,
                metadata = null,
                timestamp = "2026-08-20T10:00:00Z",
                deliveredAt = null,
                readAt = null,
                isSynced = false,
                isFailed = false,
            )

            coEvery { messageDao.getUnsyncedMessages() } returns listOf(pendingEntity)
            coEvery { chatWebSocketService.connect() } returns Unit
            coEvery { chatWebSocketService.sendMessage(any()) } returns Unit

            val result = repository.resendUnsyncedMessages()

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { chatWebSocketService.sendMessage(any()) }
            coVerify {
                messageDao.upsert(match { it.id == "temp_123" && it.isSynced && !it.isFailed })
            }
        }
}
