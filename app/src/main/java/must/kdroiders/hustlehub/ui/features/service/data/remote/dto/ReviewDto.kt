package must.kdroiders.hustlehub.ui.features.service.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

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
    @SerializedName("isVerified")
    val isVerified: Boolean = false,
    @SerializedName("providerReply")
    val providerReply: String? = null,
    @SerializedName("providerRepliedAt")
    val providerRepliedAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("createdAt")
    val createdAt: String,
)

@Keep
data class RatingDistributionResponse(
    @SerializedName("serviceId")
    val serviceId: String,
    @SerializedName("averageRating")
    val averageRating: Double,
    @SerializedName("totalReviews")
    val totalReviews: Int,
    @SerializedName("count1Stars")
    val count1Stars: Int,
    @SerializedName("count2Stars")
    val count2Stars: Int,
    @SerializedName("count3Stars")
    val count3Stars: Int,
    @SerializedName("count4Stars")
    val count4Stars: Int,
    @SerializedName("count5Stars")
    val count5Stars: Int,
)

@Keep
data class ProviderReplyRequest(
    @SerializedName("reply")
    val reply: String,
)

@Keep
data class CreateReviewRequest(
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("isAnonymous")
    val isAnonymous: Boolean = false,
)

@Keep
data class UpdateReviewRequest(
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("isAnonymous")
    val isAnonymous: Boolean = false,
)
