package must.kdroiders.hustlehub.ui.features.chat.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.MessageEntity

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getByConversation(conversationId: String): Flow<List<MessageEntity>>

    @Upsert
    suspend fun upsert(entity: MessageEntity)

    @Upsert
    suspend fun upsertAll(entities: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteByConversation(conversationId: String)

    @Query("DELETE FROM messages WHERE cachedAt < :threshold")
    suspend fun deleteStaleEntries(threshold: Long)
}
