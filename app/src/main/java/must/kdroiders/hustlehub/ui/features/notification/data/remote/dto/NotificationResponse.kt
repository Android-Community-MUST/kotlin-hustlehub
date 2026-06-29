package must.kdroiders.hustlehub.ui.features.notification.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val body: String,
    val data: Map<String, String>? = null,
    val isRead: Boolean,
    val sentAt: String
)
