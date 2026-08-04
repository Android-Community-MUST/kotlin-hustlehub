package must.kdroiders.hustlehub.ui.features.notification.data.repository

import must.kdroiders.hustlehub.ui.features.notification.data.remote.NotificationApiService
import must.kdroiders.hustlehub.ui.features.notification.data.remote.dto.NotificationPreferencesDto
import must.kdroiders.hustlehub.ui.features.notification.data.remote.dto.NotificationResponse
import must.kdroiders.hustlehub.ui.features.notification.data.remote.dto.UpdateNotificationPreferencesRequest
import must.kdroiders.hustlehub.ui.features.notification.domain.model.Notification
import must.kdroiders.hustlehub.ui.features.notification.domain.model.NotificationPreferences
import must.kdroiders.hustlehub.ui.features.notification.domain.model.NotificationType
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl
    @Inject
    constructor(
        private val apiService: NotificationApiService,
    ) : NotificationRepository {
        override suspend fun getNotifications(
            page: Int,
            size: Int,
        ): Result<List<Notification>> =
            runCatching {
                val apiResponse = apiService.getNotifications(page, size)
                val pageResponse = apiResponse.data ?: return@runCatching emptyList()
                pageResponse.content.map { it.toDomain() }
            }

        override suspend fun markRead(id: String): Result<Unit> =
            runCatching {
                apiService.markRead(id)
            }.map { }

        override suspend fun deleteNotification(id: String): Result<Unit> =
            runCatching {
                apiService.deleteNotification(id)
            }.map { }

        override suspend fun markAllRead(): Result<Unit> =
            runCatching {
                apiService.markAllRead()
            }.map { }

        override suspend fun getUnreadCount(): Result<Int> =
            runCatching {
                val response = apiService.getUnreadCount()
                (response.data?.unreadCount ?: 0L).toInt()
            }

        override suspend fun getPreferences(): Result<NotificationPreferences> =
            runCatching {
                val response = apiService.getPreferences()
                response.data?.toDomain() ?: NotificationPreferences()
            }

        override suspend fun updatePreferences(preferences: NotificationPreferences): Result<NotificationPreferences> =
            runCatching {
                val request = UpdateNotificationPreferencesRequest(
                    newMessages = preferences.newMessages,
                    newReviews = preferences.newReviews,
                    serviceInquiries = preferences.serviceInquiries,
                    marketing = preferences.marketing,
                    soundEnabled = preferences.soundEnabled,
                    vibrationEnabled = preferences.vibrationEnabled,
                    quietHoursStart = preferences.quietHoursStart,
                    quietHoursEnd = preferences.quietHoursEnd,
                )
                val response = apiService.updatePreferences(request)
                response.data?.toDomain() ?: preferences
            }

        private fun NotificationResponse.toDomain(): Notification {
            val typeEnum = when (type.uppercase()) {
                "NEW_MESSAGE" -> NotificationType.NEW_MESSAGE
                "NEW_REVIEW" -> NotificationType.NEW_REVIEW
                "SERVICE_INQUIRY" -> NotificationType.SERVICE_INQUIRY
                else -> NotificationType.SYSTEM
            }
            return Notification(
                id = id,
                userId = userId,
                type = typeEnum,
                title = title,
                body = body,
                data = data,
                isRead = isRead,
                sentAt = sentAt,
            )
        }

        private fun NotificationPreferencesDto.toDomain() =
            NotificationPreferences(
                newMessages = newMessages,
                newReviews = newReviews,
                serviceInquiries = serviceInquiries,
                marketing = marketing,
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                quietHoursStart = quietHoursStart,
                quietHoursEnd = quietHoursEnd,
            )
    }
