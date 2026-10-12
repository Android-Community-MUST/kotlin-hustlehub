package must.kdroiders.hustlehub.ui.features.service.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val providerIsVerifiedPro: Boolean = false,
    val portfolioJson: String,
    val tagsJson: String,
    val iconUrl: String,
    val lastUpdated: Long,
)
