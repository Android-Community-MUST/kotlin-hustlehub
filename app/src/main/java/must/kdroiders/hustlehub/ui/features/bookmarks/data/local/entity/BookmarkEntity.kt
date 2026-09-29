package must.kdroiders.hustlehub.ui.features.bookmarks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val serviceId: String,
    val title: String,
    val category: String,
    val priceRange: String,
    val rating: Double,
    val imageUrl: String? = null,
    val bookmarkedAt: Long = System.currentTimeMillis(),
)
