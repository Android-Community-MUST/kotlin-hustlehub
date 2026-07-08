package must.kdroiders.hustlehub.feature.chat.presentation.viewmodel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.data.remote.MediaApiService
import must.kdroiders.hustlehub.feature.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.feature.chat.data.local.entity.ConversationEntity
import must.kdroiders.hustlehub.feature.chat.data.remote.ChatWebSocketService
import must.kdroiders.hustlehub.feature.chat.data.remote.dto.TypingIndicator
import must.kdroiders.hustlehub.feature.chat.domain.model.Message
import must.kdroiders.hustlehub.feature.chat.domain.model.MessageType
import must.kdroiders.hustlehub.feature.chat.domain.repository.ChatRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherRule : TestWatcher() {
    val testDispatcher = UnconfinedTestDispatcher()
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ChatDetailViewModelTest {

    @get:Rule
    val testDispatcherRule = TestDispatcherRule()

    private lateinit var mockChatRepository: ChatRepository
    private lateinit var mockChatWebSocketService: ChatWebSocketService
    private lateinit var mockMediaApiService: MediaApiService
    private lateinit var mockConversationDao: ConversationDao
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var viewModel: ChatDetailViewModel

    private val messagesFlow = MutableStateFlow<List<Message>>(emptyList())
    private val webSocketMessagesFlow = MutableSharedFlow<Message>()
    private val typingIndicatorFlow = MutableSharedFlow<TypingIndicator>()

    @Before
    fun setup() {
        mockChatRepository = mockk()
        mockChatWebSocketService = mockk()
        mockMediaApiService = mockk()
        mockConversationDao = mockk()
        mockFirebaseAuth = mockk()
        mockFirebaseUser = mockk()

        // Setup common stubbing
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns "test_user_id"
        coEvery { mockConversationDao.getById(any()) } returns null
        every { mockChatRepository.getMessages(any()) } returns messagesFlow
        coEvery { mockChatRepository.connectWebSocket(any()) } returns webSocketMessagesFlow
        coEvery { mockChatWebSocketService.subscribeToTyping(any()) } returns typingIndicatorFlow
        coEvery { mockChatRepository.loadMessageHistory(any(), any()) } returns Result.success(Unit)
        coEvery { mockChatRepository.markAsRead(any()) } returns Result.success(Unit)
        coEvery { mockChatRepository.sendMessage(any(), any(), any(), any(), any()) } returns Result.success(Unit)
        coEvery { mockChatWebSocketService.sendTypingIndicator(any()) } returns Unit
        coEvery { mockChatRepository.disconnectWebSocket() } returns Unit

        viewModel = ChatDetailViewModel(
            mockChatRepository,
            mockChatWebSocketService,
            mockMediaApiService,
            mockConversationDao,
            mockFirebaseAuth
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `initialize loads other user info from conversation cache`() = runTest {
        val cachedConversation = ConversationEntity(
            id = "conv123",
            otherUserId = "user456",
            otherUserName = "Bob Builder",
            otherUserAvatar = "http://avatar.url/bob.png",
            serviceId = "service789",
            lastMessage = "Hey",
            lastMessageType = "TEXT",
            lastMessageAt = "1000",
            unreadCount = 0,
            createdAt = "500"
        )
        coEvery { mockConversationDao.getById("conv123") } returns cachedConversation

        viewModel.initialize("conv123")
        viewModel.uiState.first { it.otherUserName == "Bob Builder" }

        assertEquals("test_user_id", viewModel.uiState.value.currentUserId)
        assertEquals("Bob Builder", viewModel.uiState.value.otherUserName)
        assertEquals("http://avatar.url/bob.png", viewModel.uiState.value.otherUserAvatar)
    }

    @Test
    fun `initialize collects and propagates message Flow from repository`() = runTest {
        viewModel.initialize("conv123")
        advanceUntilIdle()

        val initialMessages = listOf(
            Message(
                id = "msg1",
                conversationId = "conv123",
                senderId = "user456",
                type = MessageType.TEXT,
                content = "Hi",
                mediaUrl = null,
                thumbnailUrl = null,
                metadata = null,
                timestamp = "1000",
                deliveredAt = null,
                readAt = null
            )
        )
        messagesFlow.value = initialMessages
        advanceUntilIdle()

        assertEquals(initialMessages, viewModel.uiState.value.messages)
    }

    @Test
    fun `initialize connects WebSocket and subscribes to typing indicators`() = runTest {
        viewModel.initialize("conv123")
        advanceUntilIdle()

        coVerify(exactly = 1) { mockChatRepository.connectWebSocket("conv123") }
        coVerify(exactly = 1) { mockChatWebSocketService.subscribeToTyping("conv123") }

        // Test typing indicator update when sender is not null
        assertFalse(viewModel.uiState.value.isTyping)
        typingIndicatorFlow.emit(TypingIndicator(conversationId = "conv123", senderId = "user456", isTyping = true))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isTyping)

        typingIndicatorFlow.emit(TypingIndicator(conversationId = "conv123", senderId = "user456", isTyping = false))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isTyping)
    }

    @Test
    fun `loadHistory handles success state transitions`() = runTest {
        coEvery { mockChatRepository.loadMessageHistory("conv123", 0) } coAnswers {
            // Check loading is true during execution
            assertTrue(viewModel.uiState.value.isLoading)
            Result.success(Unit)
        }

        viewModel.initialize("conv123")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadHistory handles failure state transitions`() = runTest {
        coEvery { mockChatRepository.loadMessageHistory("conv123", 0) } returns Result.failure(Exception("HTTP 500"))

        viewModel.initialize("conv123")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("HTTP 500", viewModel.uiState.value.error)
    }

    @Test
    fun `sendTextMessage invokes repository sendMessage`() = runTest {
        viewModel.initialize("conv123")
        advanceUntilIdle()

        viewModel.sendTextMessage("Hello world")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            mockChatRepository.sendMessage(
                conversationId = "conv123",
                type = MessageType.TEXT,
                content = "Hello world",
                mediaUrl = null,
                metadata = null
            )
        }
    }

    @Test
    fun `sendLocationMessage constructs metadata and invokes repository`() = runTest {
        viewModel.initialize("conv123")
        advanceUntilIdle()

        viewModel.sendLocationMessage(0.24, 37.56, "Meru University")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            mockChatRepository.sendMessage(
                conversationId = "conv123",
                type = MessageType.LOCATION,
                content = "Location: Meru University",
                mediaUrl = null,
                metadata = "{\"lat\":0.24,\"lng\":37.56,\"label\":\"Meru University\"}"
            )
        }
    }

    @Test
    fun `sendServiceCardMessage constructs metadata and invokes repository`() = runTest {
        viewModel.initialize("conv123")
        advanceUntilIdle()

        viewModel.sendServiceCardMessage("service_abc", "Coding help", "KES 500-1000")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            mockChatRepository.sendMessage(
                conversationId = "conv123",
                type = MessageType.SERVICE_CARD,
                content = "Coding help",
                mediaUrl = null,
                metadata = "{\"serviceId\":\"service_abc\",\"title\":\"Coding help\",\"priceRange\":\"KES 500-1000\"}"
            )
        }
    }

    @Test
    fun `sendTypingIndicator invokes websocket service`() = runTest {
        viewModel.initialize("conv123")
        advanceUntilIdle()

        viewModel.sendTypingIndicator(true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            mockChatWebSocketService.sendTypingIndicator(
                TypingIndicator(conversationId = "conv123", senderId = "", isTyping = true)
            )
        }
    }

    @Test
    fun `clearError resets error UI state`() = runTest {
        coEvery { mockChatRepository.loadMessageHistory("conv123", 0) } returns Result.failure(Exception("Failure error"))

        viewModel.initialize("conv123")
        advanceUntilIdle()

        assertEquals("Failure error", viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }
}
