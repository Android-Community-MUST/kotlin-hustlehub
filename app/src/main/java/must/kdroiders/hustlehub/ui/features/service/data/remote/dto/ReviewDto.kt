package must.kdroiders.hustlehub.ui.features.service.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Wire-format DTO for a single review returned by the backend.
 *
 * Maps to GET /api/v1/services/{serviceId}/reviews (paginated).
 */
@Keep
data class ReviewResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("serviceId")
    val serviceId: String,
    @SerializedName("providerId")
    val providerId: String? = null,
    @SerializedName("reviewerId")
    val customerId: String? = null,
    @SerializedName("reviewerName")
    val customerName: String? = null,
    @SerializedName("reviewerAvatarUrl")
    val customerAvatarUrl: String? = null,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("isAnonymous")
    val isAnonymous: Boolean = false,
    @SerializedName("createdAt")
    val createdAt: String,
)

/**
 * Request body for POST /api/v1/services/{serviceId}/reviews.
 *
 * @param isAnonymous When true the backend omits customer identity from public responses.
 */
@Keep
data class CreateReviewRequest(
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("isAnonymous")
    val isAnonymous: Boolean = false,
)
