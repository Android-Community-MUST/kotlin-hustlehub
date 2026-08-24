package must.kdroiders.hustlehub.ui.features.chat.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.MessageDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.ConversationEntity
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.MessageEntity
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ChatWebSocketService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ConversationApiService
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageRepositoryTest {
    private lateinit var context: Context
    private lateinit var conversationApiService: ConversationApiService
    private lateinit var conversationDao: ConversationDao
    private lateinit var messageDao: MessageDao
    private lateinit var chatWebSocketService: ChatWebSocketService
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        conversationApiService = mockk(relaxed = true)
        conversationDao = mockk(relaxed = true)
        messageDao = mockk(relaxed = true)
        chatWebSocketService = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        firebaseUser = mockk(relaxed = true) {
            every { uid } returns "current-user-uid"
        }
        every { firebaseAuth.currentUser } returns firebaseUser

        repository = ChatRepositoryImpl(
            context = context,
            conversationApiService = conversationApiService,
            conversationDao = conversationDao,
            messageDao = messageDao,
            chatWebSocketService = chatWebSocketService,
            firebaseAuth = firebaseAuth,
        )
    }

    @Test
    fun `getConversations maps local entities to domain models`() =
        runTest {
            val entities = listOf(
                ConversationEntity(
                    id = "conv-1",
                    otherUserId = "user-2",
                    otherUserName = "Alice",
                    otherUserAvatar = null,
                    serviceId = null,
                    lastMessage = "Hello",
                    lastMessageType = "TEXT",
                    lastMessageAt = "2026-08-16T10:00:00Z",
                    unreadCount = 2,
                    createdAt = "2026-08-16T10:00:00Z",
                ),
            )
            every { conversationDao.getAll() } returns flowOf(entities)

            val result = repository.getConversations().first()

            assertEquals(1, result.size)
            assertEquals("conv-1", result[0].id)
            assertEquals("Alice", result[0].otherUserName)
            assertEquals(2, result[0].unreadCount)
        }

    @Test
    fun `getMessages returns local flow of messages`() =
        runTest {
            val messages = listOf(
                MessageEntity(
                    id = "msg-1",
                    conversationId = "conv-1",
                    senderId = "user-2",
                    type = "TEXT",
                    content = "Hi there!",
                    mediaUrl = null,
                    thumbnailUrl = null,
                    metadata = null,
                    timestamp = "2026-08-16T10:00:00Z",
                    deliveredAt = null,
                    readAt = null,
                    isSynced = true,
                    isFailed = false,
                ),
            )
            every { messageDao.getByConversation("conv-1") } returns flowOf(messages)

            val result = repository.getMessages("conv-1").first()

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
}
