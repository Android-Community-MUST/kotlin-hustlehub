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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.map.domain.usecase.GetMapPinsUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import javax.inject.Inject

data class MapUiState(
    val pins: List<MapPin> = emptyList(),
    val selectedCategory: ServiceCategory? = null,
    val availability: ServiceAvailability? = ServiceAvailability.AVAILABLE,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userLocation: LatLng? = null,
    val isInitialCameraAnimationDone: Boolean = false,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getMapPinsUseCase: GetMapPinsUseCase,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private var isPollingEnabled = true

    // Secondary constructor for testing to prevent infinite loop under TestScope
    constructor(
        getMapPinsUseCase: GetMapPinsUseCase,
        userPreferences: UserPreferences,
        startPollingImmediately: Boolean
    ) : this(getMapPinsUseCase, userPreferences) {
        isPollingEnabled = startPollingImmediately
        if (!startPollingImmediately) {
            pollingJob?.cancel()
        }
    }

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

    fun updateUserLocation(latLng: LatLng) {
        _uiState.update { it.copy(userLocation = latLng) }
    }

    fun setInitialCameraAnimationDone() {
        _uiState.update { it.copy(isInitialCameraAnimationDone = true) }
    }

    fun refreshPins() {
        viewModelScope.launch {
            fetchPins()
        }
    }

    private fun startPolling() {
        if (!isPollingEnabled) return
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchPins()
                delay(10_000) // Poll every 10 seconds for real-time availability updates
            }
        }
    }

    private suspend fun fetchPins() {
        val currentState = _uiState.value
        val lat = currentState.userLocation?.latitude
        val lng = currentState.userLocation?.longitude
        // We use a radius of 2.0 km around the user location for nearby services
        val radius = if (lat != null && lng != null) 2.0 else null

        getMapPinsUseCase(
            lat = lat,
            lng = lng,
            radiusKm = radius,
            category = currentState.selectedCategory,
            availability = currentState.availability,
        ).onSuccess { pins ->
            _uiState.update { it.copy(pins = pins, isLoading = false, error = null) }
        }.onFailure { error ->
            _uiState.update { it.copy(isLoading = false, error = error.message ?: "Unknown error") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
