package must.kdroiders.hustlehub.ui.features.chat.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetMessagesUseCaseTest {

    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private lateinit var useCase: GetMessagesUseCase

    @Before
    fun setup() {
        useCase = GetMessagesUseCase(chatRepository)
    }

    @Test
    fun `invoke delegates conversation ID to chatRepository getMessages`() = runTest {
        val messages = listOf(
            Message(id = "m1", conversationId = "conv-1", senderId = "u1", type = MessageType.TEXT, content = "Hi", timestamp = "2026-08-21T12:00:00Z"),
        )
        every { chatRepository.getMessages("conv-1") } returns flowOf(messages)

        val result = useCase("conv-1").first()

        assertEquals(1, result.size)
        assertEquals("m1", result[0].id)
        verify(exactly = 1) { chatRepository.getMessages("conv-1") }
    }
}
