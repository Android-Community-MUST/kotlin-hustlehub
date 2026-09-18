package must.kdroiders.hustlehub.ui.features.bookmarks

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.bookmarks.domain.repository.BookmarkRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val bookmarkRepository: BookmarkRepository = mockk(relaxed = true)
    private val bookmarksFlow = MutableStateFlow<List<BookmarkItem>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { bookmarkRepository.getBookmarksFlow() } returns bookmarksFlow
        coEvery { bookmarkRepository.refreshBookmarks() } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits Empty when repository flow is empty`() =
        runTest(testDispatcher) {
            val viewModel = BookmarkViewModel(bookmarkRepository)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect()
            }
            advanceUntilIdle()

            assertEquals(BookmarkUiState.Empty, viewModel.uiState.value)
        }

    @Test
    fun `emits Success when repository flow has items`() =
        runTest(testDispatcher) {
            val items = listOf(
                BookmarkItem(
                    id = "srv-1",
                    title = "Laundry",
                    category = "LAUNDRY",
                    price = "KES 200",
                    rating = 4.5,
                ),
            )
            bookmarksFlow.value = items

            val viewModel = BookmarkViewModel(bookmarkRepository)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect()
            }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is BookmarkUiState.Success)
            assertEquals(1, (state as BookmarkUiState.Success).items.size)
            assertEquals("srv-1", state.items[0].id)
        }

    @Test
    fun `removeBookmark delegates to repository`() =
        runTest(testDispatcher) {
            val viewModel = BookmarkViewModel(bookmarkRepository)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect()
            }
            advanceUntilIdle()

            viewModel.removeBookmark("srv-1")
            advanceUntilIdle()

            coVerify(exactly = 1) { bookmarkRepository.removeBookmark("srv-1") }
        }
}
