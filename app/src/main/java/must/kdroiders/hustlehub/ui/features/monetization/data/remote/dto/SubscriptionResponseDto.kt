package must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Response from GET /api/v1/subscriptions/me. Null when no active subscription exists. */
data class SubscriptionResponseDto(
    val planType: String,
    val status: String,
    val startDate: String,
    val endDate: String,
    @SerializedName("isActive", alternate = ["active"])
    val isActive: Boolean = false,
)
