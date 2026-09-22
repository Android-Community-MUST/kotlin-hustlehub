package must.kdroiders.hustlehub.ui.features.service.domain.repository

import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.service.domain.model.RatingDistribution
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review

interface ReviewRepository {
    suspend fun submitReview(
        serviceId: String,
        rating: Int,
        comment: String? = null,
        isAnonymous: Boolean = false,
    ): Result<Review>

    suspend fun getReviewsForService(
        serviceId: String,
        page: Int = 0,
        size: Int = 10,
    ): Result<PageResponse<Review>>

    suspend fun checkDuplicateReview(serviceId: String): Result<Boolean>

    suspend fun getRatingDistribution(serviceId: String): Result<RatingDistribution>

    suspend fun replyToReview(reviewId: String, reply: String): Result<Review>

    suspend fun updateReview(
        reviewId: String,
        rating: Int,
        comment: String? = null,
        isAnonymous: Boolean = false,
    ): Result<Review>

    suspend fun deleteReview(reviewId: String): Result<Unit>

    suspend fun getMyReviewForService(serviceId: String): Result<Review?>
}

