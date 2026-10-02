package must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.auth.AuthManager
import must.kdroiders.hustlehub.core.cache.LruServiceCache
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.home.domain.usecase.BrowseServicesUseCase
import must.kdroiders.hustlehub.ui.features.notification.data.local.dao.NotificationDao
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetMyServicesUseCase
import timber.log.Timber
import java.util.PriorityQueue
import javax.inject.Inject

private const val PAGE_SIZE = 10
private const val MAX_FEATURED_COUNT = 5

data class HomeUiState(
    val selectedCategory: ServiceCategory = ServiceCategory.ALL,
    val searchQuery: String = "",
    val providerInitials: String = "HH",
    val notificationCount: Int = 0,
    val services: List<Service> = emptyList(),
    val featuredServices: List<Service> = emptyList(),
    val isLoadingServices: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 0,
    val error: String? = null,
    val isLoading: Boolean = false,
    val showProviderBanner: Boolean = false,
)

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val browseServices: BrowseServicesUseCase,
        private val authManager: AuthManager,
        private val userRepository: UserRepository,
        private val notificationRepository: NotificationRepository,
        private val userPreferences: UserPreferences,
        private val getMyServicesUseCase: GetMyServicesUseCase? = null,
        private val notificationDao: NotificationDao? = null,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        companion object {
            private val featuredComparator = Comparator<Service> { a, b ->
                val c = a.createdAt.compareTo(b.createdAt)
                if (c != 0) c else a.averageRating.compareTo(b.averageRating)
            }
        }

        private var searchJob: Job? = null
        private val serviceCache = LruServiceCache(maxSize = 100)
        private val hasListedServices = MutableStateFlow(false)

        init {
            loadUserInitials()
            fetchServices(reset = true)
            loadNotificationCount()
            observeProviderBannerVisibility()
            observeUnreadNotifications()
        }

        private fun observeUnreadNotifications() {
            val dao = notificationDao ?: return
            viewModelScope.launch {
                dao.getUnreadCountFlow().collect { unread ->
                    _uiState.update { it.copy(notificationCount = unread) }
                }
            }
        }

        private fun observeProviderBannerVisibility() {
            viewModelScope.launch {
                combine(
                    userPreferences.cachedUser,
                    userPreferences.isProviderBannerDismissed,
                    hasListedServices,
                ) { user, dismissed, hasServices ->
                    val isProviderRole =
                        user.role == UserRole.ROLE_PROVIDER ||
                            user.role == UserRole.ROLE_BOTH ||
                            user.role == UserRole.ROLE_ADMIN ||
                            user.role == UserRole.ROLE_SUPER_ADMIN
                    if ((isProviderRole || hasServices) && !dismissed) {
                        userPreferences.dismissProviderBanner()
                    }
                    !dismissed && !hasServices && user.id.isNotBlank() && user.role == UserRole.ROLE_CUSTOMER
                }.collect { show ->
                    _uiState.update { it.copy(showProviderBanner = show) }
                }
            }
        }

        fun dismissProviderBanner() {
            viewModelScope.launch { userPreferences.dismissProviderBanner() }
        }

        private fun loadNotificationCount() {
            viewModelScope.launch {
                notificationRepository
                    .getNotifications(0, 50)
                    .onSuccess { list ->
                        val count = list.count { !it.isRead }
                        _uiState.update { it.copy(notificationCount = count) }
                    }
            }
        }

        private fun loadUserInitials() {
            viewModelScope.launch {
                val uid = authManager.currentUser()?.uid ?: return@launch

                val userResult = userRepository.getUserProfile(uid)
                val servicesResult = getMyServicesUseCase?.invoke()
                val hasServices = servicesResult?.getOrNull()?.isNotEmpty() == true

                userResult.onSuccess { user ->
                    if (user != null) {
                        if (user.name.isNotBlank()) {
                            val parts = user.name.trim().split("\\s+".toRegex())
                            val initials = if (parts.size >= 2) {
                                "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
                            } else {
                                parts[0].take(2).uppercase()
                            }
                            _uiState.update { it.copy(providerInitials = initials) }
                        }

                        val isProvider = hasServices || user.role == UserRole.ROLE_PROVIDER || user.role == UserRole.ROLE_BOTH
                        if (isProvider) {
                            _uiState.update { it.copy(showProviderBanner = false) }
                            userPreferences.dismissProviderBanner()
                            val roleToSave = if (hasServices && user.role == UserRole.ROLE_CUSTOMER) {
                                UserRole.ROLE_PROVIDER
                            } else {
                                user.role
                            }
                            userPreferences.writeUser(user.copy(role = roleToSave))
                        } else {
                            userPreferences.writeUser(user)
                        }
                    }
                }

                if (hasServices) {
                    hasListedServices.value = true
                    _uiState.update { it.copy(showProviderBanner = false) }
                    userPreferences.dismissProviderBanner()
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
                    if (reset) {
                        it.copy(isLoadingServices = true, error = null)
                    } else {
                        it.copy(isLoadingMore = true)
                    }
                }

                browseServices(page = page, size = PAGE_SIZE, category = category, query = query)
                    .onSuccess { pageResponse ->
                        _uiState.update { current ->
                            if (reset) serviceCache.clear()
                            serviceCache.putAll(pageResponse.content)
                            val merged = serviceCache.snapshot()

                            val featured = selectTopFeatured(merged)

                            current.copy(
                                services = merged,
                                featuredServices = featured,
                                isLoadingServices = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                currentPage = page + 1,
                                hasMorePages = pageResponse.content.size == PAGE_SIZE,
                            )
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to browse services (page $page)")
                        _uiState.update {
                            it.copy(
                                isLoadingServices = false,
                                isRefreshing = false,
                                isLoadingMore = false,
                                error = if (reset) "Could not load services. Showing cached data." else null,
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
            loadNotificationCount()
        }

        fun loadNextPage() {
            fetchServices(reset = false)
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        override fun onCleared() {
            super.onCleared()
            searchJob?.cancel()
        }

        // O(n log k) min-heap selection of active paid featured services
        private fun selectTopFeatured(
            merged: List<Service>,
            k: Int = MAX_FEATURED_COUNT,
        ): List<Service> {
            val paidFeatured = merged.filter { it.isFeatured }
            if (paidFeatured.size <= k) return paidFeatured.sortedWith(featuredComparator.reversed())
            val heap = PriorityQueue<Service>(k, featuredComparator)
            for (service in paidFeatured) {
                if (heap.size < k) {
                    heap.add(service)
                } else if (featuredComparator.compare(service, heap.peek()!!) > 0) {
                    heap.poll()
                    heap.add(service)
                }
            }
            return heap.sortedWith(featuredComparator.reversed())
        }
    }
