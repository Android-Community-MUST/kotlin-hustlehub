package must.kdroiders.hustlehub.feature.chat.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.feature.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.feature.chat.data.local.dao.MessageDao
import must.kdroiders.hustlehub.feature.chat.data.local.entity.ConversationEntity
import must.kdroiders.hustlehub.feature.chat.data.local.entity.MessageEntity
import must.kdroiders.hustlehub.feature.chat.data.remote.ChatWebSocketService
import must.kdroiders.hustlehub.feature.chat.data.remote.ConversationApiService
import must.kdroiders.hustlehub.feature.chat.data.remote.dto.ConversationResponse
import must.kdroiders.hustlehub.feature.chat.data.remote.dto.MessageResponse
import must.kdroiders.hustlehub.feature.chat.data.remote.dto.SendMessageRequest
import must.kdroiders.hustlehub.feature.chat.domain.model.MessageType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryTestDispatcherRule : TestWatcher() {
    val testDispatcher = UnconfinedTestDispatcher()
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class ChatRepositoryImplTest {

    @get:Rule
    val testDispatcherRule = RepositoryTestDispatcherRule()

    private lateinit var mockApiService: ConversationApiService
    private lateinit var mockWebSocketService: ChatWebSocketService
    private lateinit var mockConversationDao: ConversationDao
    private lateinit var mockMessageDao: MessageDao
    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setup() {
        mockApiService = mockk()
        mockWebSocketService = mockk()
        mockConversationDao = mockk()
        mockMessageDao = mockk()

        repository = ChatRepositoryImpl(
            mockApiService,
            mockWebSocketService,
            mockConversationDao,
            mockMessageDao
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `getConversations returns mapped domain flow from room`() = runTest {
        val cachedEntities = listOf(
            ConversationEntity(
                id = "conv1",
                otherUserId = "user2",
                otherUserName = "Alice",
                otherUserAvatar = null,
                serviceId = "service1",
                lastMessage = "Hello",
                lastMessageType = "TEXT",
                lastMessageAt = "1000",
                unreadCount = 2,
                createdAt = "500"
            )
        )
        every { mockConversationDao.getAll() } returns flowOf(cachedEntities)

        val conversationsFlow = repository.getConversations()
        val list = conversationsFlow.first()

        assertEquals(1, list.size)
        val domain = list[0]
        assertEquals("conv1", domain.id)
        assertEquals("Alice", domain.otherUserName)
        assertEquals(2, domain.unreadCount)
    }

    @Test
    fun `refreshConversations success fetches API and updates cache`() = runTest {
        val apiResponse = PageResponse(
            content = listOf(
                ConversationResponse(
                    id = "conv1",
                    otherUserId = "user2",
                    otherUserName = "Alice",
                    otherUserAvatar = null,
                    serviceId = "service1",
                    lastMessage = "Hello",
                    lastMessageType = "TEXT",
                    lastMessageAt = "1000",
                    unreadCount = 2,
                    createdAt = "500"
                )
            ),
            page = 0,
            size = 50,
            totalElements = 1,
            totalPages = 1
        )
        coEvery { mockApiService.getConversations(0, 50) } returns ApiResponse(
            success = true,
            message = "Success",
            data = apiResponse
        )
        coEvery { mockConversationDao.upsertAll(any()) } returns Unit

        val result = repository.refreshConversations()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockConversationDao.upsertAll(any()) }
    }

    @Test
    fun `refreshConversations failure returns error result`() = runTest {
        coEvery { mockApiService.getConversations(0, 50) } returns ApiResponse(
            success = false,
            message = "Server error",
            data = null
        )

        val result = repository.refreshConversations()

        assertTrue(result.isFailure)
        assertEquals("Server error", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { mockConversationDao.upsertAll(any()) }
    }

    @Test
    fun `getOrCreateConversation success creates and caches`() = runTest {
        val response = ConversationResponse(
            id = "conv1",
            otherUserId = "user2",
            otherUserName = "Alice",
            otherUserAvatar = null,
            serviceId = "service1",
            lastMessage = null,
            lastMessageType = "TEXT",
            lastMessageAt = "1000",
            unreadCount = 0,
            createdAt = "500"
        )
        coEvery { mockApiService.getOrCreateConversation(any()) } returns ApiResponse(
            success = true,
            message = "Success",
            data = response
        )
        coEvery { mockConversationDao.upsert(any()) } returns Unit

        val result = repository.getOrCreateConversation("user2", "service1")

        assertTrue(result.isSuccess)
        assertEquals("conv1", result.getOrNull()?.id)
        coVerify(exactly = 1) { mockConversationDao.upsert(any()) }
    }

    @Test
    fun `loadMessageHistory fetches and caches remote messages`() = runTest {
        val apiResponse = PageResponse(
            content = listOf(
                MessageResponse(
                    id = "msg1",
                    conversationId = "conv1",
                    senderId = "user2",
                    type = "TEXT",
                    content = "Hey",
                    mediaUrl = null,
                    thumbnailUrl = null,
                    metadata = null,
                    timestamp = "1000",
                    deliveredAt = null,
                    readAt = null
                )
            ),
            page = 0,
            size = 50,
            totalElements = 1,
            totalPages = 1
        )
        coEvery { mockApiService.getMessages("conv1", 0, 50) } returns ApiResponse(
            success = true,
            message = "Success",
            data = apiResponse
        )
        coEvery { mockMessageDao.upsertAll(any()) } returns Unit

        val result = repository.loadMessageHistory("conv1", 0)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockMessageDao.upsertAll(any()) }
    }

    @Test
    fun `sendMessage delegates to websocket service`() = runTest {
        coEvery { mockWebSocketService.sendMessage(any()) } returns Unit

        val result = repository.sendMessage("conv1", MessageType.TEXT, "Hello")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            mockWebSocketService.sendMessage(
                SendMessageRequest(
                    conversationId = "conv1",
                    type = "TEXT",
                    content = "Hello",
                    mediaUrl = null,
                    metadata = null
                )
            )
        }
    }

    @Test
    fun `markAsRead clears unread count locally and calls API`() = runTest {
        coEvery { mockApiService.markAsRead("conv1") } returns ApiResponse(
            success = true,
            message = "Success",
            data = Unit
        )
        val cached = ConversationEntity(
            id = "conv1",
            otherUserId = "user2",
            otherUserName = "Alice",
            otherUserAvatar = null,
            serviceId = "service1",
            lastMessage = "Hello",
            lastMessageType = "TEXT",
            lastMessageAt = "1000",
            unreadCount = 5,
            createdAt = "500"
        )
        coEvery { mockConversationDao.getById("conv1") } returns cached
        coEvery { mockConversationDao.upsert(any()) } returns Unit

        val result = repository.markAsRead("conv1")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockApiService.markAsRead("conv1") }
        coVerify(exactly = 1) {
            mockConversationDao.upsert(
                cached.copy(unreadCount = 0)
            )
        }
    }

    @Test
    fun `connectWebSocket subscribes, maps, caches incoming messages, and updates header`() = runTest {
        val socketFlow = MutableSharedFlow<MessageResponse>()
        coEvery { mockWebSocketService.subscribeToConversation("conv1") } returns socketFlow
        coEvery { mockMessageDao.upsert(any()) } returns Unit
        
        val cachedConv = ConversationEntity(
            id = "conv1",
            otherUserId = "user2",
            otherUserName = "Alice",
            otherUserAvatar = null,
            serviceId = "service1",
            lastMessage = "Hello",
            lastMessageType = "TEXT",
            lastMessageAt = "1000",
            unreadCount = 5,
            createdAt = "500"
        )
        coEvery { mockConversationDao.getById("conv1") } returns cachedConv
        coEvery { mockConversationDao.upsert(any()) } returns Unit

        val receivedMessages = mutableListOf<must.kdroiders.hustlehub.feature.chat.domain.model.Message>()
        
        // Use UnconfinedTestDispatcher so the collector eagerly subscribes
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.connectWebSocket("conv1").toList(receivedMessages)
        }

        val incoming = MessageResponse(
            id = "msg2",
            conversationId = "conv1",
            senderId = "user2",
            type = "TEXT",
            content = "New text message",
            mediaUrl = null,
            thumbnailUrl = null,
            metadata = null,
            timestamp = "2000",
            deliveredAt = null,
            readAt = null
        )

        socketFlow.emit(incoming)

        // Wait for the Dispatchers.IO side-effect in onEach to complete
        var retries = 0
        while (retries < 100) {
            kotlinx.coroutines.yield()
            Thread.sleep(10)
            retries++
            // Check if the IO side-effects have completed
            try {
                coVerify(atLeast = 1) { mockMessageDao.upsert(any()) }
                break
            } catch (_: AssertionError) {
                // Not yet, keep waiting
            }
        }

        assertEquals(1, receivedMessages.size)
        assertEquals("New text message", receivedMessages[0].content)

        coVerify(exactly = 1) { mockMessageDao.upsert(any()) }
        coVerify(exactly = 1) {
            mockConversationDao.upsert(
                cachedConv.copy(
                    lastMessage = "New text message",
                    lastMessageType = "TEXT",
                    lastMessageAt = "2000"
                )
            )
        }

        job.cancel()
    }
}
