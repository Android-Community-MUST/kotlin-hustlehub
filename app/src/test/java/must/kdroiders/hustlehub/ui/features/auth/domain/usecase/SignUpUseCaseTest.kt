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
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var useCase: SignUpUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        userRepository = mockk()
        useCase = SignUpUseCase(authRepository, userRepository)
    }

    @Test
    fun `successful sign up creates firebase account and saves user profile`() =
        runTest {
            val mockFirebaseUser = mockk<FirebaseUser> {
                every { uid } returns "user-123"
                every { photoUrl } returns null
            }
            val loginResult = LoginResult(user = mockFirebaseUser, isEmailVerified = false)
            val expectedUser = User(
                id = "user-123",
                name = "John Doe",
                email = "john@students.must.ac.ke",
                role = UserRole.CUSTOMER,
                profilePhotoUrl = "",
                isVerified = false,
            )

            coEvery { authRepository.signUp("John Doe", "john@students.must.ac.ke", "Password123!") } returns loginResult
            coEvery { userRepository.saveUserProfile(any()) } returns Result.success(expectedUser)

            val result = useCase("John Doe", "john@students.must.ac.ke", "Password123!")

            assertTrue(result.isSuccess)
            assertEquals(loginResult, result.getOrNull())
            coVerify(exactly = 1) { authRepository.signUp("John Doe", "john@students.must.ac.ke", "Password123!") }
            coVerify(exactly = 1) { userRepository.saveUserProfile(any()) }
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
            coVerify(exactly = 0) { userRepository.saveUserProfile(any()) }
        }

    @Test
    fun `backend registration failure does not block sign up result`() =
        runTest {
            val mockFirebaseUser = mockk<FirebaseUser> {
                every { uid } returns "user-456"
                every { photoUrl } returns null
            }
            val loginResult = LoginResult(user = mockFirebaseUser, isEmailVerified = false)

            coEvery { authRepository.signUp(any(), any(), any()) } returns loginResult
            coEvery { userRepository.saveUserProfile(any()) } returns Result.failure(RuntimeException("Backend 500"))

            val result = useCase("Jane Doe", "jane@students.must.ac.ke", "Password123!")

            assertTrue(result.isSuccess)
            assertEquals(loginResult, result.getOrNull())
        }
}
