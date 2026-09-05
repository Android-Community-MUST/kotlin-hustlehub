package must.kdroiders.hustlehub.ui.features.service.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching service listings locally.
 * Portfolio images and tags are stored as JSON strings.
 */
@Entity(tableName = "cached_services")
data class ServiceEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val title: String,
    val category: String,
    val description: String,
    val priceRange: String,
    val averageRating: Float,
    val reviewCount: Int,
    val availability: String,
    val openToBarter: Boolean,
    val isFeatured: Boolean = false,
    val portfolioJson: String, // JSON array of image URLs
    val tagsJson: String, // JSON array of tags
    val iconUrl: String,
    val lastUpdated: Long, // epoch ms — used for 30-min cache invalidation
)
