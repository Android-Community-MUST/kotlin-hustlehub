package must.kdroiders.hustlehub.ui.features.monetization.data.repository

import must.kdroiders.hustlehub.ui.features.monetization.data.remote.PaymentApiService
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.PaymentStatusResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.StkPushRequestDto
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.StkPushResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.SubscriptionResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.domain.repository.PaymentRepository
import timber.log.Timber

/** Coordinates M-Pesa payment and subscription data from the Spring Boot backend. */
class PaymentRepositoryImpl(
    private val paymentApiService: PaymentApiService,
) : PaymentRepository {
    override suspend fun initiateStkPush(
        phoneNumber: String,
        planType: String,
        serviceId: String?,
    ): Result<StkPushResponseDto> =
        runCatching {
            val response = paymentApiService.initiateStkPush(
                StkPushRequestDto(
                    phoneNumber = phoneNumber,
                    planType = planType,
                    serviceId = serviceId,
                ),
            )
            response.data ?: error("Empty response from STK push endpoint")
        }.onFailure { Timber.e(it, "Failed to initiate STK push") }

    override suspend fun pollPaymentStatus(checkoutRequestId: String): Result<PaymentStatusResponseDto> =
        runCatching {
            val response = paymentApiService.getPaymentStatus(checkoutRequestId)
            response.data ?: error("Empty response from payment status endpoint")
        }.onFailure { Timber.e(it, "Failed to poll payment status: $checkoutRequestId") }

    override suspend fun getMySubscription(): Result<SubscriptionResponseDto?> =
        runCatching {
            paymentApiService.getMySubscription().data
        }.onFailure { Timber.e(it, "Failed to fetch subscription") }
}
