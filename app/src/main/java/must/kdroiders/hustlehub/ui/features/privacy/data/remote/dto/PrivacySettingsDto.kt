package must.kdroiders.hustlehub.ui.features.privacy.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class MessagingPermission {
    @SerializedName("EVERYONE")
    EVERYONE,
    @SerializedName("VERIFIED_ONLY")
    VERIFIED_ONLY,
}

@Keep
@Serializable
data class PrivacySettingsDto(
    @SerializedName("showLocationOnMap")
    val showLocationOnMap: Boolean = true,
    @SerializedName("messagingPermission")
    val messagingPermission: MessagingPermission = MessagingPermission.EVERYONE,
    @SerializedName("showOnlineStatus")
    val showOnlineStatus: Boolean = true,
    @SerializedName("showLastSeen")
    val showLastSeen: Boolean = true,
    @SerializedName("allowReviews")
    val allowReviews: Boolean = true,
)

@Keep
@Serializable
data class UpdatePrivacySettingsRequestDto(
    @SerializedName("showLocationOnMap")
    val showLocationOnMap: Boolean? = null,
    @SerializedName("messagingPermission")
    val messagingPermission: MessagingPermission? = null,
    @SerializedName("showOnlineStatus")
    val showOnlineStatus: Boolean? = null,
    @SerializedName("showLastSeen")
    val showLastSeen: Boolean? = null,
    @SerializedName("allowReviews")
    val allowReviews: Boolean? = null,
)
