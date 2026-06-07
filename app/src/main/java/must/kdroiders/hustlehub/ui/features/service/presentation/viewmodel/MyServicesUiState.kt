package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.ServiceAvailability

data class MyServicesUiState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // ID of the service that is waiting for a network response (optimistic locking)
    val updatingServiceId: String? = null,
    // ID of the service pending delete confirmation
    val pendingDeleteServiceId: String? = null
)
