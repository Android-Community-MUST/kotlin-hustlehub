package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.utils.ImageCompressor
import must.kdroiders.hustlehub.ui.features.media.domain.repository.StorageRepository
import must.kdroiders.hustlehub.ui.features.media.domain.repository.UploadResult
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.DeleteServiceUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetMyServicesUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.UpdateAvailabilityUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyServicesViewModel
    @Inject
    constructor(
        private val getMyServices: GetMyServicesUseCase,
        private val deleteService: DeleteServiceUseCase,
        private val updateAvailability: UpdateAvailabilityUseCase,
        private val storageRepository: StorageRepository,
        private val serviceRepository: ServiceRepository,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MyServicesUiState())
        val uiState: StateFlow<MyServicesUiState> = _uiState.asStateFlow()

        init {
            loadServices()
        }

        fun loadServices() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                getMyServices()
                    .onSuccess { services ->
                        _uiState.update { it.copy(services = services, isLoading = false) }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to load my services")
                        _uiState.update {
                            it.copy(isLoading = false, error = e.message ?: "Failed to load services")
                        }
                    }
            }
        }

        // --- Availability ---

        fun onAvailabilityChange(
            serviceId: String,
            availability: ServiceAvailability,
        ) {
            // Optimistic update — apply locally first
            _uiState.update { state ->
                state.copy(
                    services = state.services.map { s ->
                        if (s.id == serviceId) s.copy(availability = availability) else s
                    },
                    updatingServiceId = serviceId,
                )
            }

            viewModelScope.launch {
                updateAvailability(serviceId, availability)
                    .onSuccess { updated ->
                        _uiState.update { state ->
                            state.copy(
                                services = state.services.map { s ->
                                    if (s.id == updated.id) updated else s
                                },
                                updatingServiceId = null,
                            )
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to update availability")
                        // Roll back the optimistic update by reloading
                        _uiState.update { it.copy(updatingServiceId = null) }
                        loadServices()
                    }
            }
        }

        // --- Delete ---

        fun requestDelete(serviceId: String) {
            _uiState.update { it.copy(pendingDeleteServiceId = serviceId) }
        }

        fun cancelDelete() {
            _uiState.update { it.copy(pendingDeleteServiceId = null) }
        }

        fun confirmDelete() {
            val serviceId = _uiState.value.pendingDeleteServiceId ?: return
            _uiState.update { it.copy(pendingDeleteServiceId = null, updatingServiceId = serviceId) }

            viewModelScope.launch {
                deleteService(serviceId)
                    .onSuccess {
                        _uiState.update { state ->
                            state.copy(
                                services = state.services.filter { it.id != serviceId },
                                updatingServiceId = null,
                            )
                        }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to delete service")
                        _uiState.update {
                            it.copy(
                                updatingServiceId = null,
                                error = e.message ?: "Failed to delete service",
                            )
                        }
                    }
            }
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        // --- Gallery / Portfolio ---

        fun openGallery(serviceId: String) {
            val service = _uiState.value.services.find { it.id == serviceId } ?: return
            _uiState.update {
                it.copy(
                    selectedServiceForGallery = serviceId,
                    existingPortfolioUrls = service.portfolio,
                    portfolioUris = emptyList(),
                )
            }
        }

        fun closeGallery() {
            _uiState.update {
                it.copy(
                    selectedServiceForGallery = null,
                    existingPortfolioUrls = emptyList(),
                    portfolioUris = emptyList(),
                )
            }
        }

        fun onGalleryImageAdded(uri: Uri) {
            val state = _uiState.value
            val totalCount = state.portfolioUris.size + state.existingPortfolioUrls.size
            if (totalCount < 6) {
                _uiState.update { it.copy(portfolioUris = it.portfolioUris + uri) }
            } else {
                _uiState.update { it.copy(error = "Maximum 6 images allowed") }
            }
        }

        fun onGalleryExistingImageRemoved(index: Int) {
            _uiState.update {
                val newList = it.existingPortfolioUrls.toMutableList().apply { removeAt(index) }
                it.copy(existingPortfolioUrls = newList)
            }
        }

        fun onGalleryNewImageRemoved(index: Int) {
            _uiState.update {
                val newList = it.portfolioUris.toMutableList().apply { removeAt(index) }
                it.copy(portfolioUris = newList)
            }
        }

        fun saveGallery() {
            val serviceId = _uiState.value.selectedServiceForGallery ?: return
            val state = _uiState.value

            _uiState.update { it.copy(isGallerySaving = true, error = null) }

            viewModelScope.launch {
                try {
                    val uploadedUrls = mutableListOf<String>()

                    // 1. Compress and upload new images
                    for (uri in state.portfolioUris) {
                        val compressedBytes = ImageCompressor.compressImage(context, uri)
                        if (compressedBytes != null) {
                            var finalUrl: String? = null
                            storageRepository.uploadPortfolioImage(serviceId, compressedBytes).collect { result ->
                                if (result is UploadResult.Success) {
                                    finalUrl = result.url
                                } else if (result is UploadResult.Error) {
                                    throw Exception(result.message)
                                }
                            }
                            finalUrl?.let { uploadedUrls.add(it) }
                        }
                    }

                    // 2. Combine surviving existing URLs and newly uploaded URLs
                    val finalPortfolioUrls = state.existingPortfolioUrls + uploadedUrls

                    // 3. Update the service in backend
                    serviceRepository
                        .updateService(
                            serviceId = serviceId,
                            portfolioUrls = finalPortfolioUrls,
                        ).onSuccess { updatedService ->
                            // 4. Update the local cache
                            _uiState.update { current ->
                                val updatedServices = current.services.map { service ->
                                    if (service.id == updatedService.id) updatedService else service
                                }
                                current.copy(
                                    services = updatedServices,
                                    selectedServiceForGallery = null,
                                    existingPortfolioUrls = emptyList(),
                                    portfolioUris = emptyList(),
                                    isGallerySaving = false,
                                )
                            }
                        }.onFailure { e ->
                            Timber.e(e, "Failed to update service portfolio")
                            _uiState.update { it.copy(isGallerySaving = false, error = e.message ?: "Failed to update portfolio") }
                        }
                } catch (e: Exception) {
                    Timber.e(e, "Error saving gallery")
                    _uiState.update { it.copy(isGallerySaving = false, error = e.message ?: "Error saving gallery") }
                }
            }
        }
    }
