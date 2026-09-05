package must.kdroiders.hustlehub.ui.features.chat.domain.model

import com.google.gson.annotations.SerializedName

data class UserPresence(
    @SerializedName("uid") val uid: String = "",
    @SerializedName("userId") val userId: String = uid,
    @SerializedName("online") val online: Boolean = false,
    @SerializedName("lastSeenAt") val lastSeenAt: String? = null,
)
