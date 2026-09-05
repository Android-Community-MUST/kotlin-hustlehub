package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.api.userFriendlyMessage
import must.kdroiders.hustlehub.core.telemetry.HustleAnalytics
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
import must.kdroiders.hustlehub.core.utils.ImageCompressor
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.media.domain.repository.StorageRepository
import must.kdroiders.hustlehub.ui.features.media.domain.repository.UploadResult
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import timber.log.Timber
import javax.inject.Inject

enum class LocationSelectionMode {
    CAMPUS_PRESET,
    CURRENT_GPS,
    MAP_PICKER,
}

data class CreateServiceUiState(
    val isEditMode: Boolean = false,
    val isLoadingExisting: Boolean = false,
    val isProUser: Boolean = false,
    val title: String = "",
    val titleError: String? = null,
    val category: ServiceCategory? = null,
    val categoryError: String? = null,
    val description: String = "",
    val descriptionError: String? = null,
    val minPrice: String = "",
    val maxPrice: String = "",
    val priceError: String? = null,
    val tagInput: String = "",
    val tags: List<String> = emptyList(),
    val tagError: String? = null,
    val openToBarter: Boolean = false,
    // Operating Location
    val locationMode: LocationSelectionMode = LocationSelectionMode.CAMPUS_PRESET,
    val selectedLat: Double? = -0.0076,
    val selectedLng: Double? = 37.6534,
    val locationLabel: String = "MUST Main Campus (Nchiru)",
    // Portfolio
    val portfolioUris: List<Uri> = emptyList(), // newly picked local images
    val existingPortfolioUrls: List<String> = emptyList(), // loaded from server on edit
    // Availability / Current Status
    val availability: ServiceAvailability = ServiceAvailability.AVAILABLE,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val maxAllowedPhotos: Int get() = if (isProUser) 15 else 3
}

sealed class CreateServiceEvent {
    data object Success : CreateServiceEvent()
}

@HiltViewModel
class CreateServiceViewModel
    @Inject
    constructor(
        private val serviceRepository: ServiceRepository,
        private val storageRepository: StorageRepository,
        @ApplicationContext private val context: Context,
        private val getServiceById: GetServiceByIdUseCase,
        private val userPreferences: UserPreferences,
        private val hustleAnalytics: HustleAnalytics,
        private val hustleCrashlytics: HustleCrashlytics,
    ) : ViewModel() {
        private var editServiceId: String? = null
        private var originalAvailability: ServiceAvailability = ServiceAvailability.AVAILABLE

        private val _uiState = MutableStateFlow(CreateServiceUiState())
        val uiState: StateFlow<CreateServiceUiState> = _uiState.asStateFlow()

        private val _events = MutableSharedFlow<CreateServiceEvent>()
        val events: SharedFlow<CreateServiceEvent> = _events.asSharedFlow()

        init {
            hustleCrashlytics.setScreen("CreateServiceScreen")
            observeUserProStatus()
        }

        private fun observeUserProStatus() {
            viewModelScope.launch {
                userPreferences.cachedUser.collect { user ->
                    _uiState.update { it.copy(isProUser = user.isVerifiedPro) }
                }
            }
        }

        companion object {
            private const val MAX_PORTFOLIO_IMAGES_CREATE = 3
            private const val MAX_PORTFOLIO_IMAGES_EDIT = 6
        }

        /**
         * Called by the screen when opened in edit mode.
         * Guards against re-loading if already pre-filled.
         */
        fun loadForEdit(serviceId: String) {
            if (_uiState.value.isEditMode) return
            editServiceId = serviceId
            _uiState.update { it.copy(isEditMode = true, isLoadingExisting = true) }
            loadExistingService(serviceId)
        }

        private fun loadExistingService(serviceId: String) {
            viewModelScope.launch {
                getServiceById(serviceId)
                    .onSuccess { service ->
                        val parts = service.priceRange
                            .replace("KSh", "")
                            .replace("ksh", "")
                            .split("-")
                            .map { it.trim() }
                        val min = parts.getOrNull(0)?.filter { it.isDigit() } ?: ""
                        val max = parts.getOrNull(1)?.filter { it.isDigit() } ?: ""

                        originalAvailability = service.availability

                        _uiState.update {
                            it.copy(
                                isLoadingExisting = false,
                                title = service.title,
                                category = service.category,
                                description = service.description,
                                minPrice = min,
                                maxPrice = max,
                                tags = service.tags,
                                openToBarter = service.openToBarter,
                                existingPortfolioUrls = service.portfolio,
                                availability = service.availability,
                            )
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to load service for editing")
                        _uiState.update {
                            it.copy(
                                isLoadingExisting = false,
                                error = "Could not load service details. Please try again.",
                            )
                        }
                    }
            }
        }

        fun onTitleChange(value: String) {
            _uiState.update { it.copy(title = value, titleError = null) }
        }

        fun onCategoryChange(value: ServiceCategory) {
            _uiState.update { it.copy(category = value, categoryError = null) }
        }

        fun onDescriptionChange(value: String) {
            if (value.length <= 300) {
                _uiState.update { it.copy(description = value, descriptionError = null) }
            }
        }

        fun onMinPriceChange(value: String) {
            if (value.all { it.isDigit() }) {
                _uiState.update { it.copy(minPrice = value, priceError = null) }
            }
        }

        fun onMaxPriceChange(value: String) {
            if (value.all { it.isDigit() }) {
                _uiState.update { it.copy(maxPrice = value, priceError = null) }
            }
        }

        fun onTagInputChange(value: String) {
            _uiState.update { it.copy(tagInput = value, tagError = null) }
        }

        fun addTag() {
            val state = _uiState.value
            val trimmed = state.tagInput.trim().lowercase()
            when {
                trimmed.isEmpty() -> return
                state.tags.size >= 5 -> _uiState.update { it.copy(tagError = "Maximum 5 tags allowed") }
                state.tags.contains(trimmed) -> _uiState.update {
                    it.copy(tagError = "Tag already added", tagInput = "")
                }
                else -> _uiState.update {
                    it.copy(tags = it.tags + trimmed, tagInput = "", tagError = null)
                }
            }
        }

        fun removeTag(tag: String) {
            _uiState.update { it.copy(tags = it.tags - tag) }
        }

        fun onOpenToBarterChange(value: Boolean) {
            _uiState.update { it.copy(openToBarter = value) }
        }

        // --- Portfolio ---

        fun onPortfolioImagesPicked(uris: List<Uri>) {
            val state = _uiState.value
            val currentTotal = state.portfolioUris.size + state.existingPortfolioUrls.size
            val availableSlots = state.maxAllowedPhotos - currentTotal
            if (availableSlots <= 0) {
                val msg = if (state.isProUser) {
                    "You have reached the PRO maximum limit of 15 portfolio photos."
                } else {
                    "Free limit reached (3 photos). Upgrade to PRO to upload up to 15 portfolio photos!"
                }
                showTemporaryError(msg)
                return
            }

            val toAdd = uris.take(availableSlots)
            _uiState.update { it.copy(portfolioUris = it.portfolioUris + toAdd) }

            if (uris.size > availableSlots) {
                val msg = if (state.isProUser) {
                    "Added ${toAdd.size} photos. PRO limit is 15 photos max."
                } else {
                    "Added ${toAdd.size} photos. Upgrade to PRO to upload up to 15 portfolio photos!"
                }
                showTemporaryError(msg)
            }
        }

        fun onPortfolioImageAdded(uri: Uri) {
            onPortfolioImagesPicked(listOf(uri))
        }

        fun onPortfolioNewImageRemoved(index: Int) {
            _uiState.update {
                it.copy(portfolioUris = it.portfolioUris.toMutableList().also { list -> list.removeAt(index) })
            }
        }

        fun onPortfolioExistingImageRemoved(index: Int) {
            _uiState.update {
                it.copy(existingPortfolioUrls = it.existingPortfolioUrls.toMutableList().also { list -> list.removeAt(index) })
            }
        }

        // --- Availability / Status ---

        fun onAvailabilityChange(value: ServiceAvailability) {
            _uiState.update { it.copy(availability = value) }
        }

        private fun resetForm() {
            editServiceId = null
            originalAvailability = ServiceAvailability.AVAILABLE
            _uiState.value = CreateServiceUiState()
        }

        private fun showTemporaryError(message: String) {
            _uiState.update { it.copy(error = message) }
            viewModelScope.launch {
                kotlinx.coroutines.delay(4000)
                _uiState.update { if (it.error == message) it.copy(error = null) else it }
            }
        }

        // --- Location ---

        fun onLocationModeChange(mode: LocationSelectionMode) {
            _uiState.update { state ->
                when (mode) {
                    LocationSelectionMode.CAMPUS_PRESET -> state.copy(
                        locationMode = mode,
                        selectedLat = -0.0076,
                        selectedLng = 37.6534,
                        locationLabel = "MUST Main Campus (Nchiru)",
                    )
                    LocationSelectionMode.CURRENT_GPS -> state.copy(
                        locationMode = mode,
                    )
                    LocationSelectionMode.MAP_PICKER -> state.copy(
                        locationMode = mode,
                    )
                }
            }
        }

        fun onLocationPresetSelect(
            name: String,
            lat: Double,
            lng: Double,
        ) {
            _uiState.update {
                it.copy(
                    locationMode = LocationSelectionMode.CAMPUS_PRESET,
                    selectedLat = lat,
                    selectedLng = lng,
                    locationLabel = name,
                )
            }
        }

        fun onCustomLocationSelect(
            lat: Double,
            lng: Double,
            label: String,
        ) {
            _uiState.update {
                it.copy(
                    selectedLat = lat,
                    selectedLng = lng,
                    locationLabel = label,
                )
            }
        }

        fun onLocationLabelChange(label: String) {
            _uiState.update { it.copy(locationLabel = label) }
        }

        fun publish() {
            if (!validate()) return

            val state = _uiState.value
            val minPrice = state.minPrice.toIntOrNull() ?: 0
            val maxPrice = state.maxPrice.toIntOrNull() ?: 0
            val snapshot = editServiceId

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val result = if (state.isEditMode && snapshot != null) {
                    // Update basic details first
                    serviceRepository.updateService(
                        serviceId = snapshot,
                        title = state.title.trim(),
                        category = state.category ?: ServiceCategory.OTHER,
                        description = state.description.trim().ifEmpty { null },
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        openToBarter = state.openToBarter,
                        tags = state.tags,
                        lat = state.selectedLat,
                        lng = state.selectedLng,
                        locationLabel = state.locationLabel,
                    )
                } else {
                    serviceRepository.createService(
                        title = state.title.trim(),
                        category = state.category ?: ServiceCategory.OTHER,
                        description = state.description.trim().ifEmpty { null },
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        openToBarter = state.openToBarter,
                        tags = state.tags,
                        lat = state.selectedLat,
                        lng = state.selectedLng,
                        locationLabel = state.locationLabel,
                    )
                }

                result
                    .onSuccess { savedService ->
                        if (!state.isEditMode) {
                            hustleAnalytics.logServiceCreated(savedService.id, savedService.category.name)
                        }
                        val targetId = if (state.isEditMode && snapshot != null) snapshot else savedService.id

                        // Handle Portfolio Uploads if there are new images
                        val newUrls = mutableListOf<String>()
                        if (state.portfolioUris.isNotEmpty()) {
                            try {
                                val uploadDeferreds = state.portfolioUris.map { uri ->
                                    async {
                                        val bytes = ImageCompressor.compressImage(context, uri)
                                        if (bytes != null) {
                                            // Flow usually emits once then completes for single uploads, collect first result
                                            var url: String? = null
                                            storageRepository.uploadPortfolioImage(targetId, bytes).collect { result ->
                                                if (result is UploadResult.Success) {
                                                    url = result.url
                                                }
                                            }
                                            url
                                        } else {
                                            null
                                        }
                                    }
                                }
                                newUrls.addAll(uploadDeferreds.awaitAll().filterNotNull())

                                // Now update the service with the final combined portfolio URLs
                                val combinedUrls = state.existingPortfolioUrls + newUrls
                                serviceRepository.updateService(
                                    serviceId = targetId,
                                    portfolioUrls = combinedUrls,
                                )
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to upload portfolio images")
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "Failed to upload portfolio images. Please try again.",
                                    )
                                }
                                return@launch
                            }
                        }

                        val availResult = serviceRepository.updateAvailability(targetId, state.availability)

                        if (availResult.isFailure) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Service saved, but failed to update status.",
                                )
                            }
                        } else {
                            val user = userPreferences.cachedUser.firstOrNull()
                            if (user != null && user.role == UserRole.CUSTOMER) {
                                userPreferences.writeUser(user.copy(role = UserRole.PROVIDER))
                            }
                            resetForm()
                            _events.emit(CreateServiceEvent.Success)
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to save service")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.userFriendlyMessage("Failed to save service. Please try again."),
                            )
                        }
                    }
            }
        }

        private fun validate(): Boolean {
            val state = _uiState.value

            val titleError = when {
                state.title.isBlank() -> "Title is required"
                state.title.trim().length < 5 -> "Title must be at least 5 characters"
                state.title.trim().length > 50 -> "Title must be less than 50 characters"
                else -> null
            }
            val categoryError = if (state.category == null) "Please select a category" else null

            val descriptionError = if (state.description.length > 300) {
                "Description must be 300 characters or less"
            } else {
                null
            }

            val minPrice = state.minPrice.toIntOrNull()
            val maxPrice = state.maxPrice.toIntOrNull()
            val priceError = when {
                state.minPrice.isEmpty() && state.maxPrice.isEmpty() ->
                    "Please enter at least a minimum or maximum price"
                minPrice != null && maxPrice != null && minPrice > maxPrice ->
                    "Minimum price cannot exceed maximum price"
                else -> null
            }

            _uiState.update {
                it.copy(
                    titleError = titleError,
                    categoryError = categoryError,
                    descriptionError = descriptionError,
                    priceError = priceError,
                )
            }

            return titleError == null &&
                categoryError == null &&
                descriptionError == null &&
                priceError == null
        }
    }
