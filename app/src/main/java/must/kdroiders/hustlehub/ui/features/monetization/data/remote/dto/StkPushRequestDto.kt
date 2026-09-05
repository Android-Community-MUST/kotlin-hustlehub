package must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Request body for POST /api/v1/payments/stk-push. */
@Keep
data class StkPushRequestDto(
    // Phone number in 254XXXXXXXXX format (already normalized by InitiateStkPushUseCase)
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    // Must be "PRO" or "FEATURED"
    @SerializedName("planType")
    val planType: String,
    // Only required for FEATURED plan (service to boost)
    @SerializedName("serviceId")
    val serviceId: String? = null,
)
