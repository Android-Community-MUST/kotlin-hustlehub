package must.kdroiders.hustlehub.ui.features.chat.domain.repository

import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Conversation
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType

interface ChatRepository {
    fun getConversations(): Flow<List<Conversation>>

    suspend fun refreshConversations(): Result<Unit>

    suspend fun getOrCreateConversation(
        otherUserId: String,
        serviceId: String,
    ): Result<Conversation>

    fun getMessages(conversationId: String): Flow<List<Message>>

    suspend fun loadMessageHistory(
        conversationId: String,
        page: Int = 0,
    ): Result<Unit>

    suspend fun sendMessage(
        conversationId: String,
        type: MessageType,
        content: String,
        mediaUrl: String? = null,
        metadata: String? = null,
    ): Result<Unit>

    suspend fun markAsRead(conversationId: String): Result<Unit>

    suspend fun connectWebSocket(conversationId: String): Flow<Message>

    suspend fun subscribeToPresence(otherUserId: String): Flow<must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.UserPresence>

    suspend fun disconnectWebSocket()
}
