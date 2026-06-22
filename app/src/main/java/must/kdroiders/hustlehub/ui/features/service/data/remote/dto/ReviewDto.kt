package must.kdroiders.hustlehub.ui.features.service.data.remote.dto

/**
 * Wire-format DTO for a single review returned by the backend.
 *
 * Maps to GET /api/v1/services/{serviceId}/reviews (paginated).
 */
data class ReviewResponse(
    val id: String,
    val serviceId: String,
    val providerId: String,
    val customerId: String,
    val customerName: String,
    val customerAvatarUrl: String?,
    val rating: Int,
    val comment: String?,
    val isAnonymous: Boolean,
    val createdAt: String,
)

/**
 * Request body for POST /api/v1/reviews.
 *
 * @param isAnonymous When true the backend omits customer identity from public responses.
 */
data class CreateReviewRequest(
    val serviceId: String,
    val rating: Int,
    val comment: String? = null,
    val isAnonymous: Boolean = false,
)
