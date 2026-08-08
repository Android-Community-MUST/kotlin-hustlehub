package must.kdroiders.hustlehub.ui.features.monetization.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import must.kdroiders.hustlehub.ui.features.monetization.domain.repository.PaymentRepository
import javax.inject.Inject

/** States emitted by [PollPaymentStatusUseCase] during M-Pesa confirmation polling. */
sealed interface PaymentPollState {
    /** Currently waiting; [attempt] is 1-based and counts up to [MAX_ATTEMPTS]. */
    data class Polling(val attempt: Int) : PaymentPollState

    /** M-Pesa PIN was confirmed successfully. */
    data class Completed(val receiptNumber: String) : PaymentPollState

    /** M-Pesa transaction was cancelled or declined by the user or network. */
    data class Failed(val reason: String) : PaymentPollState

    /** All [MAX_ATTEMPTS] exhausted without a terminal status. */
    data object Timeout : PaymentPollState
}

/**
 * Polls GET /api/v1/payments/status/{checkoutRequestId} every [POLL_INTERVAL_MS] milliseconds
 * for up to [MAX_ATTEMPTS] attempts, then emits [PaymentPollState.Timeout].
 *
 * Emits immediately with [PaymentPollState.Polling] before each network call so the UI
 * can show the attempt counter in real time.
 */
class PollPaymentStatusUseCase
    @Inject
    constructor(
        private val paymentRepository: PaymentRepository,
    ) {
    companion object {
        const val MAX_ATTEMPTS = 10
        const val POLL_INTERVAL_MS = 3_000L
    }

    operator fun invoke(checkoutRequestId: String): Flow<PaymentPollState> = flow {
        for (attempt in 1..MAX_ATTEMPTS) {
            emit(PaymentPollState.Polling(attempt))
            val result = paymentRepository.pollPaymentStatus(checkoutRequestId)
            result.onSuccess { response ->
                when (response.status) {
                    "COMPLETED" -> {
                        emit(PaymentPollState.Completed(response.mpesaReceiptNumber ?: ""))
                        return@flow
                    }
                    "FAILED" -> {
                        emit(PaymentPollState.Failed("Payment was declined or cancelled."))
                        return@flow
                    }
                    // PENDING — continue polling
                }
            }.onFailure {
                // Network error during poll — treat as transient and keep retrying
            }
            if (attempt < MAX_ATTEMPTS) {
                delay(POLL_INTERVAL_MS)
            }
        }
        emit(PaymentPollState.Timeout)
    }
}
