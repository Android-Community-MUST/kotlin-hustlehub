package must.kdroiders.hustlehub.ui.features.profile.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetUserProfileUseCaseTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private lateinit var useCase: GetProviderProfileUseCase

    @Before
    fun setup() {
        useCase = GetProviderProfileUseCase(userRepository)
    }

    @Test
    fun `invoke delegates userId to userRepository getProviderProfile`() = runTest {
        val user = User(id = "user-1", name = "Jane Doe", email = "jane@must.ac.ke")
        coEvery { userRepository.getProviderProfile("user-1") } returns Result.success(user)

        val result = useCase("user-1")

        assertTrue(result.isSuccess)
        assertEquals("Jane Doe", result.getOrNull()?.name)
        coVerify(exactly = 1) { userRepository.getProviderProfile("user-1") }
    }
}
