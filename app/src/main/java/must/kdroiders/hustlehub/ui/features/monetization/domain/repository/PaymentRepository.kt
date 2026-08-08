package must.kdroiders.hustlehub.ui.features.monetization.domain.repository

import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.PaymentStatusResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.StkPushResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.SubscriptionResponseDto

/** Domain interface for M-Pesa payments and subscription queries. */
interface PaymentRepository {
    suspend fun initiateStkPush(
        phoneNumber: String,
        planType: String,
        serviceId: String?,
    ): Result<StkPushResponseDto>

    suspend fun pollPaymentStatus(checkoutRequestId: String): Result<PaymentStatusResponseDto>

    suspend fun getMySubscription(): Result<SubscriptionResponseDto?>
}
