package must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Response from POST /api/v1/payments/stk-push. */
@Keep
data class StkPushResponseDto(
    @SerializedName("checkoutRequestId")
    val checkoutRequestId: String,
    @SerializedName("responseDescription")
    val responseDescription: String,
)
