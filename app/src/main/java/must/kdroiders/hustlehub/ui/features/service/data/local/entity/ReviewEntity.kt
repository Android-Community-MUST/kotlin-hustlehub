package must.kdroiders.hustlehub.ui.features.service.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
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
    val cachedAt: Long = System.currentTimeMillis(),
)

fun ReviewEntity.toDomain(): Review =
    Review(
        id = id,
        serviceId = serviceId,
        providerId = providerId,
        customerId = customerId,
        customerName = customerName,
        customerAvatarUrl = customerAvatarUrl,
        rating = rating,
        comment = comment,
        isAnonymous = isAnonymous,
        isVerified = isVerified,
        providerReply = providerReply,
        providerRepliedAt = providerRepliedAt,
        createdAt = createdAt,
    )

fun Review.toEntity(cachedAt: Long = System.currentTimeMillis()): ReviewEntity =
    ReviewEntity(
        id = id,
        serviceId = serviceId,
        providerId = providerId,
        customerId = customerId,
        customerName = customerName,
        customerAvatarUrl = customerAvatarUrl,
        rating = rating,
        comment = comment,
        isAnonymous = isAnonymous,
        isVerified = isVerified,
        providerReply = providerReply,
        providerRepliedAt = providerRepliedAt,
        createdAt = createdAt,
        cachedAt = cachedAt,
    )
