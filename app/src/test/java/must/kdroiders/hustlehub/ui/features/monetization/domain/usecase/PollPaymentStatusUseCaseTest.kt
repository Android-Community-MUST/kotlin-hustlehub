package must.kdroiders.hustlehub.ui.features.monetization.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.PaymentStatusResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.domain.repository.PaymentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PollPaymentStatusUseCaseTest {
    private val paymentRepository: PaymentRepository = mockk()
    private lateinit var useCase: PollPaymentStatusUseCase

    @Before
    fun setUp() {
        useCase = PollPaymentStatusUseCase(paymentRepository)
    }

    @Test
    fun invoke_emitsCompletedWhenStatusIsCompleted() = runTest {
        val checkoutId = "checkout_123"
        coEvery { paymentRepository.pollPaymentStatus(checkoutId) } returns Result.success(
            PaymentStatusResponseDto(status = "COMPLETED", mpesaReceiptNumber = "QWE123RTY"),
        )

        val states = useCase(checkoutId).toList()

        assertEquals(2, states.size)
        assertEquals(PaymentPollState.Polling(1), states[0])
        assertEquals(PaymentPollState.Completed("QWE123RTY"), states[1])
    }

    @Test
    fun invoke_emitsFailedWhenStatusIsFailed() = runTest {
        val checkoutId = "checkout_123"
        coEvery { paymentRepository.pollPaymentStatus(checkoutId) } returns Result.success(
            PaymentStatusResponseDto(status = "FAILED", mpesaReceiptNumber = null),
        )

        val states = useCase(checkoutId).toList()

        assertEquals(2, states.size)
        assertEquals(PaymentPollState.Polling(1), states[0])
        assertTrue(states[1] is PaymentPollState.Failed)
    }

    @Test
    fun invoke_emitsTimeoutWhenMaxAttemptsReached() = runTest {
        val checkoutId = "checkout_123"
        coEvery { paymentRepository.pollPaymentStatus(checkoutId) } returns Result.success(
            PaymentStatusResponseDto(status = "PENDING", mpesaReceiptNumber = null),
        )

        val states = useCase(checkoutId).toList()

        // 10 Polling + 1 Timeout = 11 emissions
        assertEquals(11, states.size)
        assertTrue(states.last() is PaymentPollState.Timeout)
    }
}
