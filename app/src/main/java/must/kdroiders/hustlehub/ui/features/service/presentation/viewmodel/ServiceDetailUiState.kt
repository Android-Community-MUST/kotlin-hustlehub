package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.model.ReviewEligibility
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service

data class ServiceDetailUiState(
    val service: Service? = null,
    val provider: User? = null,
    /** First page of reviews (latest 5 shown in the detail view). */
    val reviews: List<Review> = emptyList(),
    val totalReviewCount: Int = 0,
    val ratingDistribution: IntArray = IntArray(5),
    /** True when the currently logged-in user is the provider of this service. */
    val isOwnService: Boolean = false,
    val reviewEligibility: ReviewEligibility? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServiceDetailUiState

        if (service != other.service) return false
        if (provider != other.provider) return false
        if (reviews != other.reviews) return false
        if (totalReviewCount != other.totalReviewCount) return false
        if (!ratingDistribution.contentEquals(other.ratingDistribution)) return false
        if (isOwnService != other.isOwnService) return false
        if (reviewEligibility != other.reviewEligibility) return false
        if (isLoading != other.isLoading) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = service?.hashCode() ?: 0
        result = 31 * result + (provider?.hashCode() ?: 0)
        result = 31 * result + reviews.hashCode()
        result = 31 * result + totalReviewCount
        result = 31 * result + ratingDistribution.contentHashCode()
        result = 31 * result + isOwnService.hashCode()
        result = 31 * result + (reviewEligibility?.hashCode() ?: 0)
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}
