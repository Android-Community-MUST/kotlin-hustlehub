package must.kdroiders.hustlehub.domain.repository

import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.data.model.Conversation
import must.kdroiders.hustlehub.data.model.Message
import must.kdroiders.hustlehub.data.model.MessageType

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

    suspend fun disconnectWebSocket()
}
