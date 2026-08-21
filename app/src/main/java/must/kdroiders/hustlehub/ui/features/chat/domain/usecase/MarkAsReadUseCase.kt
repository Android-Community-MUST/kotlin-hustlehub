package must.kdroiders.hustlehub.ui.features.chat.domain.usecase

import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import javax.inject.Inject

class MarkAsReadUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        suspend operator fun invoke(conversationId: String): Result<Unit> = chatRepository.markAsRead(conversationId)
    }
