package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
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
    val locationMode: LocationSelectionMode = LocationSelectionMode.CAMPUS_PRESET,
    val selectedLat: Double? = 0.12678026093702657,
    val selectedLng: Double? = 37.71798703306698,
    val locationLabel: String = "Nchiru",
    val portfolioUris: List<Uri> = emptyList(),
    val existingPortfolioUrls: List<String> = emptyList(),
    val availability: ServiceAvailability = ServiceAvailability.AVAILABLE,
    val isLoading: Boolean = false,
    val loadingStage: String? = null,
    val uploadProgress: Float? = null,
    val error: String? = null,
) {
    val maxAllowedPhotos: Int get() = if (isProUser) 15 else 3
}

sealed class CreateServiceEvent {
    data class Success(val message: String? = null) : CreateServiceEvent()
    data class ShowSnackbar(val message: String) : CreateServiceEvent()
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

        private val precompressedCache = ConcurrentHashMap<Uri, ByteArray>()
        private val compressionJobs = ConcurrentHashMap<Uri, Deferred<ByteArray?>>()

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

            toAdd.forEach { uri ->
                if (!precompressedCache.containsKey(uri) && !compressionJobs.containsKey(uri)) {
                    compressionJobs[uri] = viewModelScope.async(Dispatchers.IO) {
                        try {
                            val bytes = ImageCompressor.compressImage(context, uri)
                            if (bytes != null) {
                                precompressedCache[uri] = bytes
                            }
                            bytes
                        } catch (e: Exception) {
                            Timber.w(e, "Pre-compression failed for uri: $uri")
                            null
                        }
                    }
                }
            }

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
            val uriToRemove = _uiState.value.portfolioUris.getOrNull(index)
            if (uriToRemove != null) {
                precompressedCache.remove(uriToRemove)
                compressionJobs.remove(uriToRemove)?.cancel()
            }
            _uiState.update {
                it.copy(portfolioUris = it.portfolioUris.toMutableList().also { list -> list.removeAt(index) })
            }
        }

        fun onPortfolioExistingImageRemoved(index: Int) {
            _uiState.update {
                it.copy(existingPortfolioUrls = it.existingPortfolioUrls.toMutableList().also { list -> list.removeAt(index) })
            }
        }

        fun onAvailabilityChange(value: ServiceAvailability) {
            _uiState.update { it.copy(availability = value) }
        }

        private fun resetForm() {
            editServiceId = null
            originalAvailability = ServiceAvailability.AVAILABLE
            precompressedCache.clear()
            compressionJobs.values.forEach { it.cancel() }
            compressionJobs.clear()
            _uiState.value = CreateServiceUiState()
        }

        override fun onCleared() {
            super.onCleared()
            precompressedCache.clear()
            compressionJobs.values.forEach { it.cancel() }
            compressionJobs.clear()
        }

        private fun showTemporaryError(message: String) {
            _uiState.update { it.copy(error = message) }
            viewModelScope.launch {
                kotlinx.coroutines.delay(4000)
                _uiState.update { if (it.error == message) it.copy(error = null) else it }
            }
        }

        fun onLocationModeChange(mode: LocationSelectionMode) {
            _uiState.update { state ->
                when (mode) {
                    LocationSelectionMode.CAMPUS_PRESET -> state.copy(
                        locationMode = mode,
                        selectedLat = 0.12678026093702657,
                        selectedLng = 37.71798703306698,
                        locationLabel = "Nchiru",
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
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        loadingStage = if (state.portfolioUris.isNotEmpty()) "Preparing photos…" else if (state.isEditMode) "Saving changes…" else "Publishing service…",
                        uploadProgress = 0f,
                    )
                }

                val newUrls = mutableListOf<String>()
                if (state.portfolioUris.isNotEmpty()) {
                    try {
                        val totalImages = state.portfolioUris.size
                        val uploadedCount = AtomicInteger(0)
                        val uploadSemaphore = Semaphore(3)
                        val entityId = snapshot ?: "portfolio"

                        _uiState.update {
                            it.copy(
                                loadingStage = "Uploading photo 1 of $totalImages…",
                                uploadProgress = 0f,
                            )
                        }

                        val uploadDeferreds = state.portfolioUris.map { uri ->
                            async(Dispatchers.IO) {
                                uploadSemaphore.withPermit {
                                    val bytes = precompressedCache[uri]
                                        ?: compressionJobs[uri]?.await()
                                        ?: ImageCompressor.compressImage(context, uri)

                                    if (bytes == null) {
                                        throw IllegalStateException("Failed to compress image")
                                    }

                                    var uploadedUrl: String? = null
                                    storageRepository.uploadPortfolioImage(entityId, bytes).collect { uploadResult ->
                                        when (uploadResult) {
                                            is UploadResult.Success -> {
                                                uploadedUrl = uploadResult.url
                                            }
                                            is UploadResult.Error -> {
                                                throw IllegalStateException(uploadResult.message)
                                            }
                                            else -> Unit
                                        }
                                    }

                                    val count = uploadedCount.incrementAndGet()
                                    _uiState.update {
                                        it.copy(
                                            loadingStage = if (count < totalImages) "Uploading photo ${count + 1} of $totalImages…" else "Finalizing photos…",
                                            uploadProgress = count.toFloat() / totalImages,
                                        )
                                    }

                                    uploadedUrl ?: throw IllegalStateException("Upload succeeded without returning image URL")
                                }
                            }
                        }

                        newUrls.addAll(uploadDeferreds.awaitAll())
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to upload portfolio images")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.userFriendlyMessage("Failed to upload portfolio images. Please try again."),
                                loadingStage = null,
                                uploadProgress = null,
                            )
                        }
                        return@launch
                    }
                }

                val combinedUrls = state.existingPortfolioUrls + newUrls
                _uiState.update {
                    it.copy(
                        loadingStage = if (state.isEditMode) "Saving your changes…" else "Publishing your service…",
                        uploadProgress = 1f,
                    )
                }

                val result = if (state.isEditMode && snapshot != null) {
                    serviceRepository.updateService(
                        serviceId = snapshot,
                        title = state.title.trim(),
                        category = state.category ?: ServiceCategory.OTHER,
                        description = state.description.trim().ifEmpty { null },
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        openToBarter = state.openToBarter,
                        tags = state.tags,
                        portfolioUrls = combinedUrls,
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
                        portfolioUrls = combinedUrls,
                        lat = state.selectedLat,
                        lng = state.selectedLng,
                        locationLabel = state.locationLabel,
                    )
                }

                result
                    .onSuccess { savedService ->
                        val targetId = if (state.isEditMode && snapshot != null) snapshot else savedService.id

                        if (!state.isEditMode) {
                            hustleAnalytics.logServiceCreated(savedService.id, savedService.category.name)
                        }

                        val availabilityNeedsUpdate = if (state.isEditMode) {
                            state.availability != originalAvailability
                        } else {
                            state.availability != ServiceAvailability.AVAILABLE
                        }

                        if (availabilityNeedsUpdate) {
                            val availResult = serviceRepository.updateAvailability(targetId, state.availability)
                            if (availResult.isFailure) {
                                Timber.w("Service saved, but availability update failed")
                            }
                        }

                        val user = userPreferences.cachedUser.firstOrNull()
                        if (user != null && user.role == UserRole.ROLE_CUSTOMER) {
                            userPreferences.writeUser(user.copy(role = UserRole.ROLE_PROVIDER))
                        }
                        userPreferences.dismissProviderBanner()

                        val successMsg = if (state.isEditMode) {
                            "Service updated successfully!"
                        } else {
                            "Service created successfully!"
                        }

                        _uiState.update { it.copy(isLoading = false, loadingStage = null, uploadProgress = null) }
                        _events.emit(CreateServiceEvent.Success(successMsg))
                        resetForm()
                    }
                    .onFailure { e ->
                        Timber.e(e, "Failed to save service")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.userFriendlyMessage("Failed to save service. Please try again."),
                                loadingStage = null,
                                uploadProgress = null,
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
