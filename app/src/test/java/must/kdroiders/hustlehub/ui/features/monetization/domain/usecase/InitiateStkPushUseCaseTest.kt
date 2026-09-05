package must.kdroiders.hustlehub.ui.features.monetization.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.StkPushResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.domain.repository.PaymentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InitiateStkPushUseCaseTest {
    private val paymentRepository: PaymentRepository = mockk()
    private lateinit var useCase: InitiateStkPushUseCase

    @Before
    fun setUp() {
        useCase = InitiateStkPushUseCase(paymentRepository)
    }

    @Test
    fun normalizePhone_validFormats() {
        assertEquals("254712345678", useCase.normalizePhone("0712345678"))
        assertEquals("254123456789", useCase.normalizePhone("0123456789"))
        assertEquals("254712345678", useCase.normalizePhone("+254712345678"))
        assertEquals("254712345678", useCase.normalizePhone("254712345678"))
        assertEquals("254712345678", useCase.normalizePhone("712345678"))
    }

    @Test
    fun normalizePhone_invalidFormats() {
        assertEquals(null, useCase.normalizePhone(""))
        assertEquals(null, useCase.normalizePhone("12345"))
        assertEquals(null, useCase.normalizePhone("071234567")) // 9 digits
        assertEquals(null, useCase.normalizePhone("07123456789")) // 11 digits
        assertEquals(null, useCase.normalizePhone("abcdefghijk"))
    }

    @Test
    fun invoke_successWhenPhoneIsValid() =
        runTest {
            val expectedResponse = StkPushResponseDto(checkoutRequestId = "ws_123", responseDescription = "Success")
            coEvery { paymentRepository.initiateStkPush("254712345678", "PRO", null) } returns Result.success(expectedResponse)

            val result = useCase("0712345678", "PRO")

            assertTrue(result.isSuccess)
            assertEquals(expectedResponse, result.getOrNull())
            coVerify(exactly = 1) { paymentRepository.initiateStkPush("254712345678", "PRO", null) }
        }

    @Test
    fun invoke_failureWhenPhoneIsInvalid() =
        runTest {
            val result = useCase("invalid_phone", "PRO")

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { paymentRepository.initiateStkPush(any(), any(), any()) }
        }
}
