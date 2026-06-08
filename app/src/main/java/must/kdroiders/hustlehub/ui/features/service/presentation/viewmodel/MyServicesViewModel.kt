package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.data.model.ServiceAvailability
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
            // TODO: Implement actual S3 upload and API update here in the future

            _uiState.update { state ->
                // Optimistically update the local state with the existing URLs for now
                // (In reality, we would wait for the new URIs to be uploaded and get their remote URLs back)
                val updatedServices = state.services.map { service ->
                    if (service.id == serviceId) {
                        service.copy(portfolio = state.existingPortfolioUrls)
                    } else {
                        service
                    }
                }
                state.copy(
                    services = updatedServices,
                    selectedServiceForGallery = null,
                    existingPortfolioUrls = emptyList(),
                    portfolioUris = emptyList(),
                )
            }
        }
    }
