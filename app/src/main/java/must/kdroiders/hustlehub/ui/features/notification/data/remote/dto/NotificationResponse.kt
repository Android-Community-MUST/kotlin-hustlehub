package must.kdroiders.hustlehub.ui.features.notification.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class NotificationResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("data")
    val data: Map<String, String>? = null,
    @SerializedName("isRead")
    val isRead: Boolean,
    @SerializedName("sentAt")
    val sentAt: String,
)
