package must.kdroiders.hustlehub.ui.features.monetization.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.PaymentStatusResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.StkPushRequestDto
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.StkPushResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.SubscriptionResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Retrofit interface for M-Pesa STK push and subscription endpoints. */
interface PaymentApiService {
    @POST("payments/stk-push")
    suspend fun initiateStkPush(
        @Body request: StkPushRequestDto,
    ): ApiResponse<StkPushResponseDto>

    @GET("payments/status/{checkoutRequestId}")
    suspend fun getPaymentStatus(
        @Path("checkoutRequestId") checkoutRequestId: String,
    ): ApiResponse<PaymentStatusResponseDto>

    @GET("subscriptions/me")
    suspend fun getMySubscription(): ApiResponse<SubscriptionResponseDto?>
}
