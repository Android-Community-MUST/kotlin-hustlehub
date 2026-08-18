package must.kdroiders.hustlehub.ui.features.notification.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.notification.data.local.entity.NotificationEntity

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY cachedAt DESC")
    fun getNotificationsFlow(): Flow<List<NotificationEntity>>

    @Upsert
    suspend fun upsertAll(notifications: List<NotificationEntity>)

    @Upsert
    suspend fun upsert(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}
