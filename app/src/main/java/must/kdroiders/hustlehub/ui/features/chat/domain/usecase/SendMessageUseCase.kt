package must.kdroiders.hustlehub.ui.features.chat.domain.usecase

import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        suspend operator fun invoke(
            conversationId: String,
            content: String,
            type: MessageType = MessageType.TEXT,
        ): Result<Unit> {
            val trimmedContent = content.trim()
            if (trimmedContent.isBlank()) {
                return Result.failure(IllegalArgumentException("Message content cannot be blank"))
            }
            if (conversationId.isBlank()) {
                return Result.failure(IllegalArgumentException("Conversation ID cannot be blank"))
            }
            return chatRepository.sendMessage(
                conversationId = conversationId,
                type = type,
                content = trimmedContent,
            )
        }
    }
