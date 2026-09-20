package must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
import must.kdroiders.hustlehub.core.search.SearchTrie
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.home.data.remote.DiscoveryApiService
import must.kdroiders.hustlehub.ui.features.home.domain.model.SearchFilters
import must.kdroiders.hustlehub.ui.features.home.domain.usecase.SearchServicesUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import javax.inject.Inject

private const val PAGE_SIZE = 20
private const val SEARCH_DEBOUNCE_MS = 300L

data class SearchUiState(
    val query: String = "",
    val filters: SearchFilters = SearchFilters(),
    val draftFilters: SearchFilters = SearchFilters(),
    val recentSearches: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
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
        private val discoveryApiService: DiscoveryApiService,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SearchUiState())
        val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

        private val _queryFlow = MutableStateFlow("")
        private val trie = SearchTrie()
        private var suggestionsJob: Job? = null

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
            // Instant Trie lookup — zero network, O(prefix length)
            val trieHits = if (query.length >= 2) trie.suggest(query) else emptyList()
            _uiState.update { it.copy(query = query, suggestions = trieHits) }
            _queryFlow.value = query

            // Fetch full-catalog suggestions from backend (seeds the Trie for next time)
            suggestionsJob?.cancel()
            if (query.length >= 2) {
                suggestionsJob = viewModelScope.launch {
                    runCatching { discoveryApiService.getSuggestions(query) }
                        .onSuccess { response ->
                            val serverSuggestions = response.data ?: emptyList()
                            serverSuggestions.forEach { trie.insert(it) }
                            if (_uiState.value.query == query) {
                                _uiState.update { it.copy(suggestions = serverSuggestions) }
                            }
                        }
                }
            } else {
                _uiState.update { it.copy(suggestions = emptyList()) }
            }
        }

        fun onSuggestionSelected(suggestion: String) {
            suggestionsJob?.cancel()
            _uiState.update { it.copy(query = suggestion, suggestions = emptyList()) }
            _queryFlow.value = suggestion
            fetchPage(query = suggestion, filters = _uiState.value.filters, reset = true)
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
