package must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** Response from GET /api/v1/payments/status/{checkoutRequestId}. */
@Keep
data class PaymentStatusResponseDto(
    // "PENDING", "COMPLETED", or "FAILED"
    @SerializedName("status")
    val status: String,
    @SerializedName("mpesaReceiptNumber")
    val mpesaReceiptNumber: String?,
)
