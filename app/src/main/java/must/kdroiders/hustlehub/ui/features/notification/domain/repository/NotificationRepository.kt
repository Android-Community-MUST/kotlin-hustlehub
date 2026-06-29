package must.kdroiders.hustlehub.ui.features.notification.domain.repository

import must.kdroiders.hustlehub.ui.features.notification.domain.model.Notification

interface NotificationRepository {
    suspend fun getNotifications(page: Int, size: Int): Result<List<Notification>>
    suspend fun markRead(id: String): Result<Unit>
    suspend fun markAllRead(): Result<Unit>
}
