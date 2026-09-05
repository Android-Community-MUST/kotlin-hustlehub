package must.kdroiders.hustlehub.ui.features.notification.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferencesDto(
    val newMessages: Boolean,
    val newReviews: Boolean,
    val serviceInquiries: Boolean,
    val marketing: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val quietHoursStart: Int,
    val quietHoursEnd: Int,
)

@Serializable
data class UpdateNotificationPreferencesRequest(
    val newMessages: Boolean? = null,
    val newReviews: Boolean? = null,
    val serviceInquiries: Boolean? = null,
    val marketing: Boolean? = null,
    val soundEnabled: Boolean? = null,
    val vibrationEnabled: Boolean? = null,
    val quietHoursStart: Int? = null,
    val quietHoursEnd: Int? = null,
)
