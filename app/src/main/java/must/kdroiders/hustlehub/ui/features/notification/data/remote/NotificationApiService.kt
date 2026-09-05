package must.kdroiders.hustlehub.ui.features.notification.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.notification.data.remote.dto.NotificationPreferencesDto
import must.kdroiders.hustlehub.ui.features.notification.data.remote.dto.NotificationResponse
import must.kdroiders.hustlehub.ui.features.notification.data.remote.dto.UpdateNotificationPreferencesRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): ApiResponse<PageResponse<NotificationResponse>>

    @PUT("notifications/{notificationId}/read")
    suspend fun markRead(
        @Path("notificationId") notificationId: String,
    ): ApiResponse<Unit>

    @DELETE("notifications/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: String,
    ): ApiResponse<Unit>

    @PUT("notifications/read-all")
    suspend fun markAllRead(): ApiResponse<Unit>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): ApiResponse<must.kdroiders.hustlehub.ui.features.notification.data.remote.dto.UnreadCountDto>

    @GET("notifications/preferences")
    suspend fun getPreferences(): ApiResponse<NotificationPreferencesDto>

    @PUT("notifications/preferences")
    suspend fun updatePreferences(
        @Body request: UpdateNotificationPreferencesRequest,
    ): ApiResponse<NotificationPreferencesDto>
}
