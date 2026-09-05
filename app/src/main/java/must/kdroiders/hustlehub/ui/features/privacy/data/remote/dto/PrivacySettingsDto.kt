package must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
enum class MessagingPermission {
    EVERYONE,
    VERIFIED_ONLY,
}

@Serializable
data class PrivacySettingsDto(
    val showLocationOnMap: Boolean = true,
    val messagingPermission: MessagingPermission = MessagingPermission.EVERYONE,
    val showOnlineStatus: Boolean = true,
    val showLastSeen: Boolean = true,
    val allowReviews: Boolean = true,
)

@Serializable
data class UpdatePrivacySettingsRequestDto(
    val showLocationOnMap: Boolean? = null,
    val messagingPermission: MessagingPermission? = null,
    val showOnlineStatus: Boolean? = null,
    val showLastSeen: Boolean? = null,
    val allowReviews: Boolean? = null,
)
