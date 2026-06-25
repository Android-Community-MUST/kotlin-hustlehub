package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service

data class ServiceDetailUiState(
    val service: Service? = null,
    val provider: User? = null,
    /** First page of reviews (latest 5 shown in the detail view). */
    val reviews: List<Review> = emptyList(),
    val totalReviewCount: Int = 0,
    /** True when the currently logged-in user is the provider of this service. */
    val isOwnService: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
)
