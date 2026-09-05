package must.kdroiders.hustlehub.ui.features.service.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Wire-format DTO for a single review returned by the backend.
 *
 * Maps to GET /api/v1/services/{serviceId}/reviews (paginated).
 */
data class ReviewResponse(
    val id: String,
    val serviceId: String,
    val providerId: String? = null,
    @SerializedName("reviewerId")
    val customerId: String? = null,
    @SerializedName("reviewerName")
    val customerName: String? = null,
    @SerializedName("reviewerAvatarUrl")
    val customerAvatarUrl: String? = null,
    val rating: Int,
    val comment: String? = null,
    val isAnonymous: Boolean = false,
    val createdAt: String,
)

/**
 * Request body for POST /api/v1/services/{serviceId}/reviews.
 *
 * @param isAnonymous When true the backend omits customer identity from public responses.
 */
data class CreateReviewRequest(
    val rating: Int,
    val comment: String? = null,
    val isAnonymous: Boolean = false,
)
