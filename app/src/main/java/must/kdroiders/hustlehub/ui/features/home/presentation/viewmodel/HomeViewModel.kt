package must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.home.domain.usecase.BrowseServicesUseCase
import timber.log.Timber
import javax.inject.Inject
import must.kdroiders.hustlehub.core.auth.AuthManager
import must.kdroiders.hustlehub.data.repository.UserRepository

private const val PAGE_SIZE = 10

data class HomeUiState(
    val selectedCategory: ServiceCategory = ServiceCategory.ALL,
    val searchQuery: String = "",
    val providerInitials: String = "JK",
    val notificationCount: Int = 0,
    // Paginated real services from backend
    val services: List<Service> = emptyList(),
    val isLoadingServices: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 0,
    val error: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val browseServices: BrowseServicesUseCase,
    private val authManager: AuthManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadUserInitials()
        fetchServices(reset = true)
    }

    private fun loadUserInitials() {
        viewModelScope.launch {
            val uid = authManager.currentUser()?.uid ?: return@launch
            userRepository.getUserProfile(uid)
                .onSuccess { user ->
                    if (user != null && user.name.isNotBlank()) {
                        val parts = user.name.trim().split("\\s+".toRegex())
                        val initials = if (parts.size >= 2) {
                            "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
                        } else {
                            parts[0].take(2).uppercase()
                        }
                        _uiState.update { it.copy(providerInitials = initials) }
                    }
                }
        }
    }

    // Fetch services — reset=true for fresh load or filter change, false for next page
    fun fetchServices(reset: Boolean = false) {
        val state = _uiState.value

        // Don't load more if already at the end or currently loading
        if (!reset && (!state.hasMorePages || state.isLoadingMore)) return

        val page = if (reset) 0 else state.currentPage
        val category = state.selectedCategory.takeIf { it != ServiceCategory.ALL }
        val query = state.searchQuery.trim().takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            _uiState.update {
                if (reset) it.copy(isLoadingServices = true, error = null)
                else it.copy(isLoadingMore = true)
            }

            browseServices(page = page, size = PAGE_SIZE, category = category, query = query)
                .onSuccess { pageResponse ->
                    _uiState.update { current ->
                        val merged = if (reset) pageResponse.content
                        else (current.services + pageResponse.content).distinctBy { it.id }

                        current.copy(
                            services = merged,
                            isLoadingServices = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            currentPage = page + 1,
                            hasMorePages = pageResponse.content.size == PAGE_SIZE
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to browse services (page $page)")
                    _uiState.update {
                        it.copy(
                            isLoadingServices = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            error = if (reset) "Could not load services. Showing cached data." else null
                        )
                    }
                }
        }
    }

    fun onCategorySelected(category: ServiceCategory) {
        _uiState.update { it.copy(selectedCategory = category, searchQuery = "") }
        fetchServices(reset = true)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // Debounce search by 400ms
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            fetchServices(reset = true)
        }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        fetchServices(reset = true)
    }

    fun loadNextPage() {
        fetchServices(reset = false)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

}
