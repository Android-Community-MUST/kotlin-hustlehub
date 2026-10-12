package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import android.net.Uri
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service

data class MyServicesUiState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val updatingServiceId: String? = null,
    val pendingDeleteServiceId: String? = null,
    val isProUser: Boolean = false,
    val isProBannerDismissed: Boolean = false,
    val selectedServiceForGallery: String? = null,
    val existingPortfolioUrls: List<String> = emptyList(),
    val portfolioUris: List<Uri> = emptyList(),
    val isGallerySaving: Boolean = false,
)
