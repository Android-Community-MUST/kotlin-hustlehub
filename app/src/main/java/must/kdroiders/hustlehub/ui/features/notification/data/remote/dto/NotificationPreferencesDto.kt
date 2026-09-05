package must.kdroiders.hustlehub.ui.features.notification.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class NotificationPreferencesDto(
    @SerializedName("newMessages")
    val newMessages: Boolean,
    @SerializedName("newReviews")
    val newReviews: Boolean,
    @SerializedName("serviceInquiries")
    val serviceInquiries: Boolean,
    @SerializedName("marketing")
    val marketing: Boolean,
    @SerializedName("soundEnabled")
    val soundEnabled: Boolean,
    @SerializedName("vibrationEnabled")
    val vibrationEnabled: Boolean,
    @SerializedName("quietHoursStart")
    val quietHoursStart: Int,
    @SerializedName("quietHoursEnd")
    val quietHoursEnd: Int,
)

@Keep
@Serializable
data class UpdateNotificationPreferencesRequest(
    @SerializedName("newMessages")
    val newMessages: Boolean? = null,
    @SerializedName("newReviews")
    val newReviews: Boolean? = null,
    @SerializedName("serviceInquiries")
    val serviceInquiries: Boolean? = null,
    @SerializedName("marketing")
    val marketing: Boolean? = null,
    @SerializedName("soundEnabled")
    val soundEnabled: Boolean? = null,
    @SerializedName("vibrationEnabled")
    val vibrationEnabled: Boolean? = null,
    @SerializedName("quietHoursStart")
    val quietHoursStart: Int? = null,
    @SerializedName("quietHoursEnd")
    val quietHoursEnd: Int? = null,
)
