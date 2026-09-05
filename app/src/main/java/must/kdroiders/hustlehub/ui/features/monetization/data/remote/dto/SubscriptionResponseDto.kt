package must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Response from GET /api/v1/subscriptions/me. Null when no active subscription exists. */
@Keep
data class SubscriptionResponseDto(
    @SerializedName("planType")
    val planType: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("endDate")
    val endDate: String,
    @SerializedName("isActive", alternate = ["active"])
    val isActive: Boolean = false,
)
