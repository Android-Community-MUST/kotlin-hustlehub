package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import android.net.Uri
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service

data class MyServicesUiState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // ID of the service that is waiting for a network response (optimistic locking)
    val updatingServiceId: String? = null,
    // ID of the service pending delete confirmation
    val pendingDeleteServiceId: String? = null,

    // --- Gallery Bottom Sheet State ---
    val selectedServiceForGallery: String? = null,
    val existingPortfolioUrls: List<String> = emptyList(),
    val portfolioUris: List<Uri> = emptyList(),
    val isGallerySaving: Boolean = false,
)
