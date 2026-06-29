package must.kdroiders.hustlehub.ui.features.notification.domain.model

enum class NotificationType {
    NEW_MESSAGE,
    NEW_REVIEW,
    SERVICE_INQUIRY,
    SYSTEM
}

data class Notification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val data: Map<String, String>?,
    val isRead: Boolean,
    val sentAt: String
)
