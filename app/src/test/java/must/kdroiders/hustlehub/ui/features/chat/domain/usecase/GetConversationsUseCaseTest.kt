package must.kdroiders.hustlehub.ui.features.chat.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Conversation
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetConversationsUseCaseTest {

    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private lateinit var useCase: GetConversationsUseCase

    @Before
    fun setup() {
        useCase = GetConversationsUseCase(chatRepository)
    }

    @Test
    fun `invoke delegates conversation flow collection to chatRepository`() = runTest {
        val conversations = listOf(
            Conversation(
                id = "conv-1",
                otherUserId = "u1",
                otherUserName = "Alice",
                otherUserAvatar = null,
                serviceId = null,
                lastMessage = "Hello",
                lastMessageType = "TEXT",
                lastMessageAt = "2026-08-21T12:00:00Z",
                unreadCount = 0,
                createdAt = "2026-08-21T10:00:00Z",
            ),
        )
        every { chatRepository.getConversations() } returns flowOf(conversations)

        val result = useCase().first()

        assertEquals(1, result.size)
        assertEquals("conv-1", result[0].id)
        verify(exactly = 1) { chatRepository.getConversations() }
    }
}
