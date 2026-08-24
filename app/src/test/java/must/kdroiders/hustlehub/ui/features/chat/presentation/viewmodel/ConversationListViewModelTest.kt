package must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Conversation
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationListViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val chatRepository: ChatRepository = mockk(relaxed = true)

    private lateinit var viewModel: ConversationListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val mockConversations = listOf(
            Conversation(
                id = "conv-1",
                otherUserId = "user-2",
                otherUserName = "Alice",
                otherUserAvatar = null,
                serviceId = null,
                lastMessage = "Hey, are you free?",
                lastMessageType = "TEXT",
                lastMessageAt = "2026-08-21T12:00:00Z",
                unreadCount = 2,
                createdAt = "2026-08-21T10:00:00Z",
            ),
            Conversation(
                id = "conv-2",
                otherUserId = "user-3",
                otherUserName = "Bob",
                otherUserAvatar = null,
                serviceId = null,
                lastMessage = "Thanks for the service!",
                lastMessageType = "TEXT",
                lastMessageAt = "2026-08-21T11:00:00Z",
                unreadCount = 0,
                createdAt = "2026-08-21T09:00:00Z",
            ),
        )

        every { chatRepository.getConversations() } returns flowOf(mockConversations)
        coEvery { chatRepository.refreshConversations() } returns Result.success(Unit)

        viewModel = ConversationListViewModel(chatRepository)
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `observes conversations sorted by lastMessageTimestamp`() =
        runTest {
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(2, state.conversations.size)
            assertEquals("conv-1", state.conversations[0].id)
            assertEquals(2, state.conversations[0].unreadCount)
        }

    @Test
    fun `refreshConversations invokes chatRepository refresh`() =
        runTest {
            viewModel.refreshConversations()
            coVerify(exactly = 2) { chatRepository.refreshConversations() }
            assertFalse(viewModel.uiState.value.isRefreshing)
        }

    @Test
    fun `deleteConversation delegates deletion to chatRepository`() =
        runTest {
            coEvery { chatRepository.deleteConversation("conv-1") } returns Result.success(Unit)

            viewModel.deleteConversation("conv-1")

            coVerify(exactly = 1) { chatRepository.deleteConversation("conv-1") }
        }
}
