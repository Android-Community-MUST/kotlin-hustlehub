package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

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
class MyServicesViewModel @Inject constructor(
    private val getMyServices: GetMyServicesUseCase,
    private val deleteService: DeleteServiceUseCase,
    private val updateAvailability: UpdateAvailabilityUseCase
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
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to load my services")
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Failed to load services")
                    }
                }
        }
    }

    // --- Availability ---

    fun onAvailabilityChange(serviceId: String, availability: ServiceAvailability) {
        // Optimistic update — apply locally first
        _uiState.update { state ->
            state.copy(
                services = state.services.map { s ->
                    if (s.id == serviceId) s.copy(availability = availability) else s
                },
                updatingServiceId = serviceId
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
                            updatingServiceId = null
                        )
                    }
                }
                .onFailure { e ->
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
                            updatingServiceId = null
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to delete service")
                    _uiState.update {
                        it.copy(
                            updatingServiceId = null,
                            error = e.message ?: "Failed to delete service"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
