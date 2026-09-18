package must.kdroiders.hustlehub.ui.features.bookmarks.domain.repository

import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.bookmarks.BookmarkItem

interface BookmarkRepository {
    fun getBookmarksFlow(): Flow<List<BookmarkItem>>

    fun isBookmarkedFlow(serviceId: String): Flow<Boolean>

    suspend fun refreshBookmarks(): Result<Unit>

    suspend fun toggleBookmark(
        serviceId: String,
        title: String,
        category: String,
        priceRange: String,
        rating: Double,
        imageUrl: String? = null,
    ): Result<Boolean>

    suspend fun removeBookmark(serviceId: String): Result<Unit>
}
