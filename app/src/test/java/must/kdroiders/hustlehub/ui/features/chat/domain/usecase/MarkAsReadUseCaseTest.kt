package must.kdroiders.hustlehub.ui.features.chat.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarkAsReadUseCaseTest {
    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private lateinit var useCase: MarkAsReadUseCase

    @Before
    fun setup() {
        useCase = MarkAsReadUseCase(chatRepository)
    }

    @Test
    fun `invoke delegates conversation ID to chatRepository markAsRead`() =
        runTest {
            coEvery { chatRepository.markAsRead("conv-1") } returns Result.success(Unit)

            val result = useCase("conv-1")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { chatRepository.markAsRead("conv-1") }
        }
}
