package must.kdroiders.hustlehub.ui.features.bookmarks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.bookmarks.data.local.entity.BookmarkEntity

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC")
    fun getAllBookmarksFlow(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC")
    suspend fun getAllBookmarks(): List<BookmarkEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE serviceId = :serviceId)")
    fun isBookmarkedFlow(serviceId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE serviceId = :serviceId)")
    suspend fun isBookmarked(serviceId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmark: BookmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(bookmarks: List<BookmarkEntity>)

    @Query("DELETE FROM bookmarks WHERE serviceId = :serviceId")
    suspend fun deleteByServiceId(serviceId: String)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAll()
}
