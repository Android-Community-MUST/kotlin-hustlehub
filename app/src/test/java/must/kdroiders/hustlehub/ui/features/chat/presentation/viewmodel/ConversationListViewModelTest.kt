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
import org.junit.Assert.assertTrue
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
                otherUserName = "Alice Wanjiku",
                otherUserAvatar = null,
                serviceId = "service-101",
                lastMessage = "Hey, are you free for braiding?",
                lastMessageType = "TEXT",
                lastMessageAt = "2026-08-21T12:00:00Z",
                unreadCount = 2,
                createdAt = "2026-08-21T10:00:00Z",
            ),
            Conversation(
                id = "conv-2",
                otherUserId = "user-3",
                otherUserName = "Bob Brian",
                otherUserAvatar = null,
                serviceId = null,
                lastMessage = "Thanks for the design!",
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
    fun `observes conversations correctly`() =
        runTest {
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(2, state.conversations.size)
            assertEquals("conv-1", state.conversations[0].id)
            assertEquals(2, state.conversations[0].unreadCount)
        }

    @Test
    fun `searchQuery filters conversations by contact name or message text`() =
        runTest {
            // Filter by name "Alice"
            viewModel.onSearchQueryChanged("Alice")
            var filtered = viewModel.uiState.value.filteredConversations
            assertEquals(1, filtered.size)
            assertEquals("conv-1", filtered[0].id)

            // Filter by message keyword "design"
            viewModel.onSearchQueryChanged("design")
            filtered = viewModel.uiState.value.filteredConversations
            assertEquals(1, filtered.size)
            assertEquals("conv-2", filtered[0].id)
        }

    @Test
    fun `filterSelected UNREAD filters only unread conversations`() =
        runTest {
            viewModel.onFilterSelected(ConversationFilter.UNREAD)
            val filtered = viewModel.uiState.value.filteredConversations
            assertEquals(1, filtered.size)
            assertEquals("conv-1", filtered[0].id)
        }

    @Test
    fun `filterSelected SERVICES filters only service related conversations`() =
        runTest {
            viewModel.onFilterSelected(ConversationFilter.SERVICES)
            val filtered = viewModel.uiState.value.filteredConversations
            assertEquals(1, filtered.size)
            assertEquals("conv-1", filtered[0].id)
        }

    @Test
    fun `toggleArchiveConversation archives chat and moves it to ARCHIVED tab`() =
        runTest {
            // Archive conv-1
            viewModel.toggleArchiveConversation("conv-1")

            // In ALL tab, conv-1 should no longer be visible
            var allFiltered = viewModel.uiState.value.filteredConversations
            assertEquals(1, allFiltered.size)
            assertEquals("conv-2", allFiltered[0].id)

            // In ARCHIVED tab, conv-1 should be visible
            viewModel.onFilterSelected(ConversationFilter.ARCHIVED)
            val archivedFiltered = viewModel.uiState.value.filteredConversations
            assertEquals(1, archivedFiltered.size)
            assertEquals("conv-1", archivedFiltered[0].id)
            assertTrue(archivedFiltered[0].isArchived)
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
