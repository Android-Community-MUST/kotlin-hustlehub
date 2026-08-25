package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordUseCaseTest {
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private lateinit var changePasswordUseCase: ChangePasswordUseCase

    @Before
    fun setUp() {
        changePasswordUseCase = ChangePasswordUseCase(authRepository)
    }

    @Test
    fun `invoke with valid passwords calls repository and returns success`() =
        runTest {
            coEvery { authRepository.changePassword("oldPass123", "newPass123") } returns Result.success(Unit)

            val result = changePasswordUseCase("oldPass123", "newPass123")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { authRepository.changePassword("oldPass123", "newPass123") }
        }

    @Test
    fun `invoke with incorrect current password returns failure`() =
        runTest {
            val errorMessage = "Incorrect current password."
            coEvery {
                authRepository.changePassword("wrongPass", "newPass123")
            } returns Result.failure(Exception(errorMessage))

            val result = changePasswordUseCase("wrongPass", "newPass123")

            assertTrue(result.isFailure)
            assertEquals(errorMessage, result.exceptionOrNull()?.message)
        }
}
