package must.kdroiders.hustlehub.ui.features.chat.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.ConversationEntity

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC")
    fun getAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): ConversationEntity?

    /** Reactive sum of unread counts across all conversations — drives the bottom nav badge. */
    @Query("SELECT COALESCE(SUM(unreadCount), 0) FROM conversations")
    fun getTotalUnreadCount(): Flow<Int>

    @Upsert
    suspend fun upsert(entity: ConversationEntity)

    @Upsert
    suspend fun upsertAll(entities: List<ConversationEntity>)

    @Query("DELETE FROM conversations WHERE cachedAt < :threshold")
    suspend fun deleteStaleEntries(threshold: Long)
}
