package must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto

/** Response from POST /api/v1/payments/stk-push. */
data class StkPushResponseDto(
    val checkoutRequestId: String,
    val responseDescription: String,
)
