package must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto

/** Request body for POST /api/v1/payments/stk-push. */
data class StkPushRequestDto(
    // Phone number in 254XXXXXXXXX format (already normalized by InitiateStkPushUseCase)
    val phoneNumber: String,
    // Must be "PRO" or "FEATURED"
    val planType: String,
    // Only required for FEATURED plan (service to boost)
    val serviceId: String? = null,
)
