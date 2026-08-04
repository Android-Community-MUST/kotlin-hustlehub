package must.kdroiders.hustlehub.ui.features.notification.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UnreadCountDto(
    val unreadCount: Long,
)
