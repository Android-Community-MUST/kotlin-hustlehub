package must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.home.domain.model.SearchFilters
import must.kdroiders.hustlehub.ui.features.home.domain.usecase.SearchServicesUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import javax.inject.Inject

private const val PAGE_SIZE = 20
private const val SEARCH_DEBOUNCE_MS = 300L

data class SearchUiState(
    val query: String = "",
    /** Live applied filters — these drive the active filter chip row. */
    val filters: SearchFilters = SearchFilters(),
    /** Draft filters held in the bottom sheet before the user taps Apply. */
    val draftFilters: SearchFilters = SearchFilters(),
    val recentSearches: List<String> = emptyList(),
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 0,
    val isFilterSheetOpen: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val searchServicesUseCase: SearchServicesUseCase,
        private val userPreferences: UserPreferences,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SearchUiState())
        val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

        // Internal query flow to drive debounced search.
        private val _queryFlow = MutableStateFlow("")

        init {
            observeRecentSearches()
            observeQueryDebounced()
        }

        private fun observeRecentSearches() {
            userPreferences.recentSearches
                .onEach { searches -> _uiState.update { it.copy(recentSearches = searches) } }
                .launchIn(viewModelScope)
        }

        @OptIn(FlowPreview::class)
        private fun observeQueryDebounced() {
            _queryFlow
                .drop(1) // skip initial empty string
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .onEach { query -> fetchPage(query = query, filters = _uiState.value.filters, reset = true) }
                .launchIn(viewModelScope)
        }

        fun onQueryChanged(query: String) {
            _uiState.update { it.copy(query = query) }
            _queryFlow.value = query
        }

        fun onDraftFilterChanged(draft: SearchFilters) {
            _uiState.update { it.copy(draftFilters = draft) }
        }

        fun onFilterSheetToggle() {
            _uiState.update { current ->
                val opening = !current.isFilterSheetOpen
                // When opening: seed draft with the currently applied filters.
                current.copy(
                    isFilterSheetOpen = opening,
                    draftFilters = if (opening) current.filters else current.draftFilters,
                )
            }
        }

        fun onFiltersApplied() {
            val draft = _uiState.value.draftFilters
            _uiState.update { it.copy(filters = draft, isFilterSheetOpen = false) }
            fetchPage(query = _uiState.value.query, filters = draft, reset = true)
        }

        fun onFiltersReset() {
            val empty = SearchFilters()
            _uiState.update { it.copy(draftFilters = empty, filters = empty, isFilterSheetOpen = false) }
            fetchPage(query = _uiState.value.query, filters = empty, reset = true)
        }

        fun clearRecentSearches() {
            viewModelScope.launch {
                userPreferences.clearRecentSearches()
            }
        }

        fun loadNextPage() {
            val state = _uiState.value
            if (!state.hasMorePages || state.isLoadingMore || state.isLoading) return
            fetchPage(query = state.query, filters = state.filters, reset = false, page = state.currentPage)
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        private fun fetchPage(
            query: String,
            filters: SearchFilters,
            reset: Boolean,
            page: Int = 0,
        ) {
            viewModelScope.launch {
                val targetPage = if (reset) 0 else page
                _uiState.update { current ->
                    current.copy(
                        isLoading = reset && targetPage == 0 && current.services.isEmpty(),
                        isLoadingMore = !reset && targetPage > 0,
                        error = null,
                    )
                }

                searchServicesUseCase(query = query, filters = filters, page = targetPage, size = PAGE_SIZE)
                    .onSuccess { pageResponse ->
                        // Persist non-empty queries to recent search history.
                        if (query.isNotBlank() && reset) {
                            userPreferences.addRecentSearch(query.trim())
                        }
                        _uiState.update { current ->
                            val merged = if (reset) {
                                pageResponse.content
                            } else {
                                (current.services + pageResponse.content).distinctBy { it.id }
                            }
                            current.copy(
                                services = merged,
                                isLoading = false,
                                isLoadingMore = false,
                                currentPage = targetPage + 1,
                                hasMorePages = pageResponse.content.size == PAGE_SIZE,
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = error.message) }
                    }
            }
        }
    }
