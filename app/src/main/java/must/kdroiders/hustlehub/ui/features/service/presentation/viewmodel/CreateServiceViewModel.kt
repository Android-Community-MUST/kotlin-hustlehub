package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.model.ServiceAvailability
import must.kdroiders.hustlehub.data.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import timber.log.Timber
import javax.inject.Inject

data class CreateServiceUiState(
    val isEditMode: Boolean = false,
    val isLoadingExisting: Boolean = false,
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
    // Portfolio
    val portfolioUris: List<Uri> = emptyList(), // newly picked local images
    val existingPortfolioUrls: List<String> = emptyList(), // loaded from server on edit
    // Availability / Current Status
    val availability: ServiceAvailability = ServiceAvailability.AVAILABLE,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class CreateServiceEvent {
    data object Success : CreateServiceEvent()
}

@HiltViewModel
class CreateServiceViewModel
    @Inject
    constructor(
        private val serviceRepository: ServiceRepository,
        private val getServiceById: GetServiceByIdUseCase,
    ) : ViewModel() {
        private var editServiceId: String? = null
        private var originalAvailability: ServiceAvailability = ServiceAvailability.AVAILABLE

        private val _uiState = MutableStateFlow(CreateServiceUiState())
        val uiState: StateFlow<CreateServiceUiState> = _uiState.asStateFlow()

        private val _events = MutableSharedFlow<CreateServiceEvent>()
        val events: SharedFlow<CreateServiceEvent> = _events.asSharedFlow()

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

        fun onPortfolioImageAdded(uri: Uri) {
            val state = _uiState.value
            val totalCount = state.portfolioUris.size + state.existingPortfolioUrls.size
            if (totalCount < 3) {
                _uiState.update { it.copy(portfolioUris = it.portfolioUris + uri) }
            }
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

        fun publish() {
            if (!validate()) return

            val state = _uiState.value
            val minPrice = state.minPrice.toIntOrNull() ?: 0
            val maxPrice = state.maxPrice.toIntOrNull() ?: 0
            val snapshot = editServiceId

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val result = if (state.isEditMode && snapshot != null) {
                    serviceRepository.updateService(
                        serviceId = snapshot,
                        title = state.title.trim(),
                        category = state.category!!,
                        description = state.description.trim().ifEmpty { null },
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        openToBarter = state.openToBarter,
                        tags = state.tags,
                    )
                } else {
                    serviceRepository.createService(
                        title = state.title.trim(),
                        category = state.category!!,
                        description = state.description.trim().ifEmpty { null },
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        openToBarter = state.openToBarter,
                        tags = state.tags,
                    )
                }

                result
                    .onSuccess { savedService ->
                        val targetId = if (state.isEditMode && snapshot != null) snapshot else savedService.id
                        val availResult = serviceRepository.updateAvailability(targetId, state.availability)

                        if (availResult.isFailure) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Service saved, but failed to update status.",
                                )
                            }
                        } else {
                            resetForm()
                            _events.emit(CreateServiceEvent.Success)
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to save service")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to save service. Please try again.",
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
