package must.kdroiders.hustlehub.ui.features.monetization.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.StkPushResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.SubscriptionResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.domain.usecase.GetSubscriptionUseCase
import must.kdroiders.hustlehub.ui.features.monetization.domain.usecase.InitiateStkPushUseCase
import must.kdroiders.hustlehub.ui.features.monetization.domain.usecase.PaymentPollState
import must.kdroiders.hustlehub.ui.features.monetization.domain.usecase.PollPaymentStatusUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MonetizationViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val initiateStkPushUseCase: InitiateStkPushUseCase = mockk()
    private val pollPaymentStatusUseCase: PollPaymentStatusUseCase = mockk()
    private val getSubscriptionUseCase: GetSubscriptionUseCase = mockk()

    private lateinit var viewModel: MonetizationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getSubscriptionUseCase() } returns Result.success(null)
        viewModel = MonetizationViewModel(
            initiateStkPushUseCase,
            pollPaymentStatusUseCase,
            getSubscriptionUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadSubscription_successOnStart() =
        runTest {
            val sub = SubscriptionResponseDto("PRO", "ACTIVE", "2026-01-01", "2026-02-01", true)
            coEvery { getSubscriptionUseCase() } returns Result.success(sub)

            viewModel.loadSubscription()

            val state = viewModel.subscriptionState.value
            assertTrue(state is SubscriptionUiState.Success)
            assertEquals(sub, (state as SubscriptionUiState.Success).data)
        }

    @Test
    fun triggerPayment_successSetsPendingCheckoutId() =
        runTest {
            val rawPhone = "0712345678"
            val stkResponse = StkPushResponseDto("checkout_999", "Accept")
            coEvery { initiateStkPushUseCase(rawPhone, "PRO", null) } returns Result.success(stkResponse)

            viewModel.triggerPayment(rawPhone, "PRO")

            assertEquals("checkout_999", viewModel.pendingCheckoutId.value)
        }

    @Test
    fun pollStatus_updatesPaymentStateToSuccessOnCompleted() =
        runTest {
            val checkoutId = "checkout_999"
            coEvery { pollPaymentStatusUseCase(checkoutId) } returns flowOf(
                PaymentPollState.Polling(1),
                PaymentPollState.Completed("REC123"),
            )
            coEvery { getSubscriptionUseCase() } returns Result.success(null)

            viewModel.pollStatus(checkoutId)

            assertEquals(PaymentUiState.Success("REC123"), viewModel.paymentState.value)
            coVerify { getSubscriptionUseCase() }
        }

    @Test
    fun resetPaymentState_clearsStateToIdle() =
        runTest {
            viewModel.resetPaymentState()

            assertEquals(PaymentUiState.Idle, viewModel.paymentState.value)
            assertNull(viewModel.pendingCheckoutId.value)
        }
}
