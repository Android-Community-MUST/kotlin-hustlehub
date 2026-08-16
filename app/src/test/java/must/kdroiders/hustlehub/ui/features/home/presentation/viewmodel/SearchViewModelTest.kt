package must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.home.domain.model.SearchFilters
import must.kdroiders.hustlehub.ui.features.home.domain.usecase.SearchServicesUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var searchServicesUseCase: SearchServicesUseCase
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchServicesUseCase = mockk(relaxed = true)
        coEvery { searchServicesUseCase(any(), any(), any(), any()) } returns Result.success(
            PageResponse(emptyList(), 0, 20, 0, 0),
        )
        userPreferences = mockk(relaxed = true) {
            every { recentSearches } returns flowOf(listOf("Tutor", "Haircut"))
        }

        viewModel = SearchViewModel(searchServicesUseCase, userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state observes recent searches from userPreferences`() {
        val state = viewModel.uiState.value
        assertEquals(listOf("Tutor", "Haircut"), state.recentSearches)
        assertFalse(state.isFilterSheetOpen)
        assertEquals("", state.query)
    }

    @Test
    fun `onQueryChanged updates query state`() {
        viewModel.onQueryChanged("Laundry")
        assertEquals("Laundry", viewModel.uiState.value.query)
    }

    @Test
    fun `onFilterSheetToggle opens sheet and seeds draft filters`() {
        viewModel.onFilterSheetToggle()

        val state = viewModel.uiState.value
        assertTrue(state.isFilterSheetOpen)
        assertEquals(state.filters, state.draftFilters)
    }

    @Test
    fun `onFiltersApplied updates applied filters, closes sheet, and triggers search`() =
        runTest {
            val newFilters = SearchFilters(maxPrice = 1500)
            viewModel.onDraftFilterChanged(newFilters)
            viewModel.onFiltersApplied()

            val state = viewModel.uiState.value
            assertEquals(newFilters, state.filters)
            assertFalse(state.isFilterSheetOpen)
            coVerify { searchServicesUseCase(any(), newFilters, any(), any()) }
        }

    @Test
    fun `onFiltersReset clears all draft and applied filters`() =
        runTest {
            viewModel.onFiltersReset()

            val state = viewModel.uiState.value
            assertEquals(SearchFilters(), state.filters)
            assertEquals(SearchFilters(), state.draftFilters)
            assertFalse(state.isFilterSheetOpen)
        }
}
