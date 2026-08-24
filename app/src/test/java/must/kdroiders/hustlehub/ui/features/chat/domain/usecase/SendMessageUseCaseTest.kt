package must.kdroiders.hustlehub.ui.features.chat.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SendMessageUseCaseTest {
    private lateinit var chatRepository: ChatRepository
    private lateinit var useCase: SendMessageUseCase

    @Before
    fun setup() {
        chatRepository = mockk()
        useCase = SendMessageUseCase(chatRepository)
    }

    @Test
    fun `blank content returns failure without invoking repository`() =
        runTest {
            val result = useCase("conv-123", "    ")

            assertTrue(result.isFailure)
            assertEquals("Message content cannot be blank", result.exceptionOrNull()?.message)
            coVerify(exactly = 0) { chatRepository.sendMessage(any(), any(), any(), any(), any()) }
        }

    @Test
    fun `blank conversation ID returns failure`() =
        runTest {
            val result = useCase("", "Hello!")

            assertTrue(result.isFailure)
            assertEquals("Conversation ID cannot be blank", result.exceptionOrNull()?.message)
        }

    @Test
    fun `valid content delegates to chatRepository`() =
        runTest {
            coEvery {
                chatRepository.sendMessage("conv-123", MessageType.TEXT, "Hello there!")
            } returns Result.success(Unit)

            val result = useCase("conv-123", "  Hello there!  ")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { chatRepository.sendMessage("conv-123", MessageType.TEXT, "Hello there!") }
        }
}
