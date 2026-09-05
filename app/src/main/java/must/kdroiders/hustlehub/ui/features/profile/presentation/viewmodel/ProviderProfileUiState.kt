package must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel

import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service

data class ProviderProfileUiState(
    val provider: User? = null,
    val services: List<Service> = emptyList(),
    val reviewCount: Int = 0,
    /** Computed client-side from avgRating × reviewCount until a backend score endpoint exists. */
    val hustleScore: Float = 0f,
    val badges: List<Badge> = emptyList(),
    /** True when the viewer is viewing their own profile. */
    val isOwnProfile: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)
