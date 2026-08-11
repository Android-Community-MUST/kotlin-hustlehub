package must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto

/** Response from GET /api/v1/payments/status/{checkoutRequestId}. */
data class PaymentStatusResponseDto(
    // "PENDING", "COMPLETED", or "FAILED"
    val status: String,
    val mpesaReceiptNumber: String?,
)
