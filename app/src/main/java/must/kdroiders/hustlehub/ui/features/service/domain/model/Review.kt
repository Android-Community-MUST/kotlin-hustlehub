package must.kdroiders.hustlehub.ui.features.service.domain.model

/** Immutable domain model representing a single service review. */
data class Review(
    val id: String,
    val serviceId: String,
    val providerId: String,
    val customerId: String,
    val customerName: String,
    val customerAvatarUrl: String,
    /** Star rating in the range 1–5. */
    val rating: Int,
    /** Optional review text. Null when the user submitted a star-only review. */
    val comment: String?,
    val isAnonymous: Boolean,
    val createdAt: Long,
)
