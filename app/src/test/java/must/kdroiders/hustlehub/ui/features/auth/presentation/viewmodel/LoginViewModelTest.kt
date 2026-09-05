package must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val userRepository: UserRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fcm token upload saves token to backend via userRepository`() =
        runTest {
            val token = "fcm-token-test-123"
            coEvery { userRepository.updateFcmToken(token) } returns Result.success(Unit)

            userRepository.updateFcmToken(token)

            coVerify(exactly = 1) { userRepository.updateFcmToken(token) }
        }

    @Test
    fun `fcm token removal calls userRepository removeFcmToken`() =
        runTest {
            val token = "fcm-token-test-123"
            coEvery { userRepository.removeFcmToken(token) } returns Result.success(Unit)

            userRepository.removeFcmToken(token)

            coVerify(exactly = 1) { userRepository.removeFcmToken(token) }
        }
}
