package must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.ResendOtpUseCase
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.SignOutUseCase
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.VerifyOtpUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmailVerificationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val verifyOtpUseCase: VerifyOtpUseCase = mockk()
    private val resendOtpUseCase: ResendOtpUseCase = mockk()
    private val signOutUseCase: SignOutUseCase = mockk(relaxed = true)

    private lateinit var viewModel: EmailVerificationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EmailVerificationViewModel(
            verifyOtpUseCase = verifyOtpUseCase,
            resendOtpUseCase = resendOtpUseCase,
            signOutUseCase = signOutUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `verifyOtp success updates isVerified and triggers onSuccess callback`() =
        runTest(testDispatcher) {
            val email = "student@students.must.ac.ke"
            viewModel.setEmail(email)

            coEvery { verifyOtpUseCase(email = email, otp = "") } returns Result.success(Unit)

            var onSuccessCalled = false
            viewModel.verifyOtp(otp = "", onSuccess = { onSuccessCalled = true })
            advanceUntilIdle()

            assertTrue(onSuccessCalled)
            assertTrue(viewModel.uiState.value.isVerified)
            assertFalse(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.errorMessage)
            coVerify(exactly = 1) { verifyOtpUseCase(email = email, otp = "") }
        }

    @Test
    fun `verifyOtp failure updates errorMessage and stops loading`() =
        runTest(testDispatcher) {
            val email = "student@students.must.ac.ke"
            viewModel.setEmail(email)

            coEvery { verifyOtpUseCase(email = email, otp = "") } returns Result.failure(Exception("Email not verified"))

            var onSuccessCalled = false
            viewModel.verifyOtp(otp = "", onSuccess = { onSuccessCalled = true })
            advanceUntilIdle()

            assertFalse(onSuccessCalled)
            assertFalse(viewModel.uiState.value.isVerified)
            assertFalse(viewModel.uiState.value.isLoading)
            assertTrue(
                viewModel.uiState.value.errorMessage!!
                    .contains("Email not verified"),
            )
        }

    @Test
    fun `checkVerificationStatus triggers onSuccess when user already verified`() =
        runTest(testDispatcher) {
            val email = "student@students.must.ac.ke"
            viewModel.setEmail(email)

            coEvery { verifyOtpUseCase(email = email, otp = "") } returns Result.success(Unit)

            var onSuccessCalled = false
            viewModel.checkVerificationStatus(onSuccess = { onSuccessCalled = true })
            advanceUntilIdle()

            assertTrue(onSuccessCalled)
            assertTrue(viewModel.uiState.value.isVerified)
        }

    @Test
    fun `startAutoPolling periodically checks verification until success`() =
        runTest(testDispatcher) {
            val email = "student@students.must.ac.ke"
            viewModel.setEmail(email)

            // First 2 checks fail, 3rd check succeeds
            coEvery { verifyOtpUseCase(email = email, otp = "") } returnsMany listOf(
                Result.failure(Exception("Not yet")),
                Result.failure(Exception("Not yet")),
                Result.success(Unit),
            )

            var onSuccessCalled = false
            viewModel.startAutoPolling(onSuccess = { onSuccessCalled = true })

            advanceTimeBy(4000) // tick 1
            assertFalse(onSuccessCalled)

            advanceTimeBy(4000) // tick 2
            assertFalse(onSuccessCalled)

            advanceTimeBy(4000) // tick 3
            advanceUntilIdle()

            assertTrue(onSuccessCalled)
            assertTrue(viewModel.uiState.value.isVerified)
        }

    @Test
    fun `resendOtp triggers resend and initiates cooldown timer`() =
        runTest(testDispatcher) {
            val email = "student@students.must.ac.ke"
            viewModel.setEmail(email)

            coEvery { resendOtpUseCase(email = email) } returns Result.success(Unit)

            viewModel.resendOtp()
            advanceTimeBy(100)

            assertEquals(60, viewModel.uiState.value.resendCooldown)
            coVerify(exactly = 1) { resendOtpUseCase(email = email) }

            advanceTimeBy(30_000)
            assertEquals(30, viewModel.uiState.value.resendCooldown)

            advanceTimeBy(31_000)
            advanceUntilIdle()
            assertEquals(0, viewModel.uiState.value.resendCooldown)
        }

    @Test
    fun `signOut calls signOutUseCase and invokes onComplete`() =
        runTest(testDispatcher) {
            var onCompleteCalled = false
            viewModel.signOut(onComplete = { onCompleteCalled = true })
            advanceUntilIdle()

            coVerify(exactly = 1) { signOutUseCase() }
            assertTrue(onCompleteCalled)
        }
}
