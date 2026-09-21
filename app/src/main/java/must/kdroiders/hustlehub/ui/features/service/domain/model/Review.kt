package must.kdroiders.hustlehub.ui.features.service.domain.model

data class Review(
    val id: String,
    val serviceId: String,
    val providerId: String,
    val customerId: String,
    val customerName: String,
    val customerAvatarUrl: String,
    val rating: Int,
    val comment: String?,
    val isAnonymous: Boolean,
    val isVerified: Boolean = false,
    val providerReply: String? = null,
    val providerRepliedAt: Long? = null,
    val createdAt: Long,
)

data class RatingDistribution(
    val count1Stars: Int = 0,
    val count2Stars: Int = 0,
    val count3Stars: Int = 0,
    val count4Stars: Int = 0,
    val count5Stars: Int = 0,
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
)

