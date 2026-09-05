package must.kdroiders.hustlehub.ui.features.notification.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class UnreadCountDto(
    @SerializedName("unreadCount")
    val unreadCount: Long,
)
