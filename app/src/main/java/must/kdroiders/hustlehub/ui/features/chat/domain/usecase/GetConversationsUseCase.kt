package must.kdroiders.hustlehub.ui.features.chat.domain.usecase

import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Conversation
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import javax.inject.Inject

class GetConversationsUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        operator fun invoke(): Flow<List<Conversation>> = chatRepository.getConversations()
    }
