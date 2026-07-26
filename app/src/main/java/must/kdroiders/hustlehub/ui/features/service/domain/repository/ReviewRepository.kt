package must.kdroiders.hustlehub.ui.features.service.domain.repository

import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review

/** Repository handling service review operations. */
interface ReviewRepository {
    /**
     * Submits a review for a service.
     */
    suspend fun submitReview(
        serviceId: String,
        rating: Int,
        comment: String? = null,
        isAnonymous: Boolean = false,
    ): Result<Review>

    /**
     * Returns paginated reviews for a service (default page size 10).
     */
    suspend fun getReviewsForService(
        serviceId: String,
        page: Int = 0,
        size: Int = 10,
    ): Result<PageResponse<Review>>

    /**
     * Checks if the currently authenticated user has already submitted a review for [serviceId].
     */
    suspend fun checkDuplicateReview(serviceId: String): Result<Boolean>
}
