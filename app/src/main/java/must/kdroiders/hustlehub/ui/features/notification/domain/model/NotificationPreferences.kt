package must.kdroiders.hustlehub.ui.features.notification.domain.model

data class NotificationPreferences(
    val newMessages: Boolean = true,
    val newReviews: Boolean = true,
    val serviceInquiries: Boolean = true,
    val marketing: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val quietHoursStart: Int = 22,
    val quietHoursEnd: Int = 7,
)
