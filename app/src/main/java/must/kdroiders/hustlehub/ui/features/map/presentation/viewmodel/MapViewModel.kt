package must.kdroiders.hustlehub.ui.features.map.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.map.domain.usecase.GetMapPinsUseCase
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class MapUiState(
    val pins: List<MapPin> = emptyList(),
    val selectedCategory: ServiceCategory? = null,
    val availability: ServiceAvailability? = null,
    val searchQuery: String = "",
    val notificationCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userLocation: LatLng? = null,
    val isInitialCameraAnimationDone: Boolean = false,
)

@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        private val getMapPinsUseCase: GetMapPinsUseCase,
        private val userPreferences: UserPreferences,
        private val notificationRepository: NotificationRepository,
    ) : ViewModel() {
        companion object {
            /** How often the map polls the backend for provider availability updates. */
            const val POLL_INTERVAL_MS = 120_000L
        }

        private var isPollingEnabled = true

        // Secondary constructor for testing to prevent infinite loop under TestScope
        constructor(
            getMapPinsUseCase: GetMapPinsUseCase,
            userPreferences: UserPreferences,
            notificationRepository: NotificationRepository,
            startPollingImmediately: Boolean,
        ) : this(getMapPinsUseCase, userPreferences, notificationRepository) {
            isPollingEnabled = startPollingImmediately
            if (!startPollingImmediately) {
                pollingJob?.cancel()
            }
        }

        private var cachedPins: List<MapPin> = emptyList()

        private val _uiState = MutableStateFlow(MapUiState())
        val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

        private var pollingJob: Job? = null

        init {
            viewModelScope.launch {
                val categoryName = runCatching {
                    userPreferences.lastSelectedCategory.first()
                }.getOrNull()
                val category = categoryName?.let {
                    runCatching { ServiceCategory.valueOf(it) }.getOrNull()
                }
                _uiState.update { it.copy(selectedCategory = category) }
                startPolling()
                loadNotificationCount()
            }
        }

        fun loadNotificationCount() {
            viewModelScope.launch {
                notificationRepository
                    .getNotifications(0, 50)
                    .onSuccess { list ->
                        val count = list.count { !it.isRead }
                        _uiState.update { it.copy(notificationCount = count) }
                    }
            }
        }

        fun selectCategory(category: ServiceCategory?) {
            _uiState.update { it.copy(selectedCategory = category) }
            viewModelScope.launch {
                userPreferences.saveLastSelectedCategory(category?.name)
            }
            refreshPins()
        }

        fun selectAvailability(availability: ServiceAvailability?) {
            _uiState.update { it.copy(availability = availability) }
            refreshPins()
        }

        fun updateSearchQuery(query: String) {
            _uiState.update { it.copy(searchQuery = query) }
            applySearchFilter()
        }

        private fun applySearchFilter() {
            val query = _uiState.value.searchQuery
            val filtered = if (query.isNotEmpty()) {
                cachedPins.filter {
                    it.providerName.contains(query, ignoreCase = true) ||
                        it.serviceTitle.contains(query, ignoreCase = true)
                }
            } else {
                cachedPins
            }
            _uiState.update { it.copy(pins = filtered) }
        }

        fun updateUserLocation(latLng: LatLng) {
            _uiState.update { it.copy(userLocation = latLng) }
        }

        fun setInitialCameraAnimationDone() {
            _uiState.update { it.copy(isInitialCameraAnimationDone = true) }
        }

        fun refreshPins() {
            viewModelScope.launch {
                fetchPins()
                loadNotificationCount()
            }
        }

        private fun startPolling() {
            if (!isPollingEnabled) return
            pollingJob?.cancel()
            pollingJob = viewModelScope.launch {
                while (isActive) {
                    fetchPins()
                    loadNotificationCount()
                    delay(POLL_INTERVAL_MS)
                }
            }
        }

        private suspend fun fetchPins() {
            val currentState = _uiState.value
            val userLoc = currentState.userLocation
            val lat = userLoc?.latitude
            val lng = userLoc?.longitude
            // Use a 50 km radius when the user location is known to catch campus & nearby providers
            val radius = if (lat != null && lng != null) 50.0 else null

            getMapPinsUseCase(
                lat = lat,
                lng = lng,
                radiusKm = radius,
                category = currentState.selectedCategory,
                availability = currentState.availability,
            ).onSuccess { rawPins ->
                // Compute distance for each pin and sort nearest-first (nulls last)
                val enriched = rawPins
                    .map { pin ->
                        val dist = if (userLoc != null) {
                            haversineDistance(userLoc.latitude, userLoc.longitude, pin.lat, pin.lng)
                        } else {
                            null
                        }
                        pin.copy(distanceMeters = dist)
                    }.sortedWith(compareBy(nullsLast()) { it.distanceMeters })

                cachedPins = enriched
                applySearchFilter()
                _uiState.update { it.copy(isLoading = false, error = null) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Unknown error") }
            }
        }

        /**
         * Haversine formula: computes the great-circle distance (in metres) between two
         * geographic coordinates. Kept in the ViewModel layer so sorting is testable
         * without depending on any Android/Compose APIs.
         */
        internal fun haversineDistance(
            lat1: Double,
            lng1: Double,
            lat2: Double,
            lng2: Double,
        ): Double {
            val earthRadiusMetres = 6_371_000.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLng = Math.toRadians(lng2 - lng1)
            val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadiusMetres * c
        }

        override fun onCleared() {
            super.onCleared()
            pollingJob?.cancel()
        }
    }
