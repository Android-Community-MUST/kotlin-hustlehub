package must.kdroiders.hustlehub.ui.features.notification.domain.repository

import must.kdroiders.hustlehub.ui.features.notification.domain.model.Notification
import must.kdroiders.hustlehub.ui.features.notification.domain.model.NotificationPreferences

interface NotificationRepository {
    suspend fun getNotifications(
        page: Int,
        size: Int,
    ): Result<List<Notification>>
    suspend fun markRead(id: String): Result<Unit>
    suspend fun deleteNotification(id: String): Result<Unit>
    suspend fun markAllRead(): Result<Unit>
    suspend fun getUnreadCount(): Result<Int>
    suspend fun getPreferences(): Result<NotificationPreferences>
    suspend fun updatePreferences(preferences: NotificationPreferences): Result<NotificationPreferences>
}
