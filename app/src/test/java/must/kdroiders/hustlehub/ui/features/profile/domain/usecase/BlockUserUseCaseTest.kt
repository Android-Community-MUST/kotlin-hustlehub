package must.kdroiders.hustlehub.ui.features.profile.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BlockUserUseCaseTest {
    private lateinit var userRepository: UserRepository
    private lateinit var useCase: BlockUserUseCase

    @Before
    fun setup() {
        userRepository = mockk()
        useCase = BlockUserUseCase(userRepository)
    }

    @Test
    fun `blank target ID returns failure`() =
        runTest {
            val result = useCase("   ")
            assertTrue(result.isFailure)
            assertEquals("Target user ID cannot be blank", result.exceptionOrNull()?.message)
            coVerify(exactly = 0) { userRepository.blockUser(any()) }
        }

    @Test
    fun `valid target ID delegates to userRepository`() =
        runTest {
            coEvery { userRepository.blockUser("user-789") } returns Result.success(Unit)

            val result = useCase("  user-789  ")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { userRepository.blockUser("user-789") }
        }
}
