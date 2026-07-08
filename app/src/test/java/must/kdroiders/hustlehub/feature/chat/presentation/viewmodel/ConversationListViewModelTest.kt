package must.kdroiders.hustlehub.feature.chat.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.feature.chat.domain.model.Conversation
import must.kdroiders.hustlehub.feature.chat.domain.repository.ChatRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    private val testDispatcher = UnconfinedTestDispatcher()
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class ConversationListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockChatRepository: ChatRepository
    private val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())

    @Before
    fun setup() {
        mockChatRepository = mockk()
        // Provide default answers for getConversations and refreshConversations to prevent crash on init
        every { mockChatRepository.getConversations() } returns conversationsFlow
        coEvery { mockChatRepository.refreshConversations() } returns Result.success(Unit)
    }

    @Test
    fun `initialization observes conversations and refreshes`() = runTest {
        val conversations = listOf(
            Conversation(
                id = "conv1",
                otherUserId = "user2",
                otherUserName = "Alice",
                otherUserAvatar = null,
                serviceId = "service1",
                lastMessage = "Hello",
                lastMessageType = "TEXT",
                lastMessageAt = "123456",
                unreadCount = 0,
                createdAt = "123450"
            )
        )
        conversationsFlow.value = conversations

        val viewModel = ConversationListViewModel(mockChatRepository)

        // Verify conversations flow is collected and updates UI state
        assertEquals(conversations, viewModel.uiState.value.conversations)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 1) { mockChatRepository.refreshConversations() }
    }

    @Test
    fun `refreshConversations failure sets error in UI state`() = runTest {
        coEvery { mockChatRepository.refreshConversations() } returns Result.failure(Exception("Network error"))

        val viewModel = ConversationListViewModel(mockChatRepository)

        viewModel.refreshConversations()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals("Network error", viewModel.uiState.value.error)
    }

    @Test
    fun `refreshConversations success clears error`() = runTest {
        // Set up initial state with error by failing a refresh first
        coEvery { mockChatRepository.refreshConversations() } returns Result.failure(Exception("Network error"))
        val viewModel = ConversationListViewModel(mockChatRepository)
        advanceUntilIdle()
        assertEquals("Network error", viewModel.uiState.value.error)

        // Now mock success
        coEvery { mockChatRepository.refreshConversations() } returns Result.success(Unit)
        viewModel.refreshConversations()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertNull(viewModel.uiState.value.error)
    }
}
