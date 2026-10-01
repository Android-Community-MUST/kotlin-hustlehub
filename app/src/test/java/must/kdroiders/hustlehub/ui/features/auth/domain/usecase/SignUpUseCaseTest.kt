package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.LoginResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SignUpUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        useCase = SignUpUseCase(authRepository)
    }

    @Test
    fun `successful sign up creates firebase account`() =
        runTest {
            val mockFirebaseUser = mockk<FirebaseUser> {
                every { uid } returns "user-123"
                every { photoUrl } returns null
            }
            val loginResult = LoginResult(user = mockFirebaseUser, isEmailVerified = false)

            coEvery { authRepository.signUp("John Doe", "john@students.must.ac.ke", "Password123!") } returns loginResult

            val result = useCase("John Doe", "john@students.must.ac.ke", "Password123!")

            assertTrue(result.isSuccess)
            assertEquals(loginResult, result.getOrNull())
            coVerify(exactly = 1) { authRepository.signUp("John Doe", "john@students.must.ac.ke", "Password123!") }
        }

    @Test
    fun `sign up failure in authRepository returns failure result`() =
        runTest {
            coEvery {
                authRepository.signUp(any(), any(), any())
            } throws RuntimeException("Email already in use")

            val result = useCase("Jane Doe", "jane@students.must.ac.ke", "Password123!")

            assertTrue(result.isFailure)
            assertEquals("Email already in use", result.exceptionOrNull()?.message)
        }
}
