package must.kdroiders.hustlehub.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.data.local.dao.MessageDao
import must.kdroiders.hustlehub.data.local.entity.toDomain
import must.kdroiders.hustlehub.data.local.entity.toEntity
import must.kdroiders.hustlehub.data.model.Conversation
import must.kdroiders.hustlehub.data.model.Message
import must.kdroiders.hustlehub.data.model.MessageType
import must.kdroiders.hustlehub.data.remote.ChatWebSocketService
import must.kdroiders.hustlehub.data.remote.ConversationApiService
import must.kdroiders.hustlehub.data.remote.dto.ConversationResponse
import must.kdroiders.hustlehub.data.remote.dto.CreateConversationRequest
import must.kdroiders.hustlehub.data.remote.dto.MessageResponse
import must.kdroiders.hustlehub.data.remote.dto.SendMessageRequest
import must.kdroiders.hustlehub.domain.repository.ChatRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val conversationApiService: ConversationApiService,
    private val chatWebSocketService: ChatWebSocketService,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
) : ChatRepository {

    override fun getConversations(): Flow<List<Conversation>> {
        return conversationDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshConversations(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = conversationApiService.getConversations(0, 50)
                if (response.success && response.data != null) {
                    val entities = response.data.content.map { it.toEntity() }
                    conversationDao.upsertAll(entities)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh conversations")
                Result.failure(e)
            }
        }

    override suspend fun getOrCreateConversation(
        otherUserId: String,
        serviceId: String,
    ): Result<Conversation> =
        withContext(Dispatchers.IO) {
            try {
                val request = CreateConversationRequest(otherUserId, serviceId)
                val response = conversationApiService.getOrCreateConversation(request)
                if (response.success && response.data != null) {
                    val conversation = response.data.toDomainModel()
                    conversationDao.upsert(conversation.toEntity())
                    Result.success(conversation)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to get or create conversation")
                Result.failure(e)
            }
        }

    override fun getMessages(conversationId: String): Flow<List<Message>> {
        return messageDao.getByConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun loadMessageHistory(
        conversationId: String,
        page: Int,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = conversationApiService.getMessages(conversationId, page, 50)
                if (response.success && response.data != null) {
                    val entities = response.data.content.map { it.toEntity() }
                    messageDao.upsertAll(entities)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load message history")
                Result.failure(e)
            }
        }

    override suspend fun sendMessage(
        conversationId: String,
        type: MessageType,
        content: String,
        mediaUrl: String?,
        metadata: String?,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val request = SendMessageRequest(
                    conversationId = conversationId,
                    type = type.name,
                    content = content,
                    mediaUrl = mediaUrl,
                    metadata = metadata,
                )
                chatWebSocketService.sendMessage(request)
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to send message over WebSocket")
                Result.failure(e)
            }
        }

    override suspend fun markAsRead(conversationId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = conversationApiService.markAsRead(conversationId)
                if (response.success) {
                    val cached = conversationDao.getById(conversationId)
                    if (cached != null) {
                        conversationDao.upsert(cached.copy(unreadCount = 0))
                    }
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark conversation as read")
                Result.failure(e)
            }
        }

    override suspend fun connectWebSocket(conversationId: String): Flow<Message> {
        // We connect the WebSocket session, subscribe to the conversation flow,
        // and whenever a new message is received, we:
        // 1. Map to domain model
        // 2. Insert into the local database
        // 3. Update the conversation unread count and last message in local database
        return chatWebSocketService.subscribeToConversation(conversationId)
            .map { it.toDomainModel() }
            .onEach { message ->
                withContext(Dispatchers.IO) {
                    messageDao.upsert(message.toEntity())
                    val cachedConv = conversationDao.getById(conversationId)
                    if (cachedConv != null) {
                        conversationDao.upsert(
                            cachedConv.copy(
                                lastMessage = message.content,
                                lastMessageType = message.type.name,
                                lastMessageAt = message.timestamp,
                            )
                        )
                    }
                }
            }
    }

    override suspend fun disconnectWebSocket() {
        chatWebSocketService.disconnect()
    }
}

// Mapper extensions for DTOs to Domain models

private fun ConversationResponse.toDomainModel(): Conversation =
    Conversation(
        id = id,
        otherUserId = otherUserId,
        otherUserName = otherUserName,
        otherUserAvatar = otherUserAvatar,
        serviceId = serviceId,
        lastMessage = lastMessage,
        lastMessageType = lastMessageType,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        createdAt = createdAt,
    )

private fun ConversationResponse.toEntity(): must.kdroiders.hustlehub.data.local.entity.ConversationEntity =
    must.kdroiders.hustlehub.data.local.entity.ConversationEntity(
        id = id,
        otherUserId = otherUserId,
        otherUserName = otherUserName,
        otherUserAvatar = otherUserAvatar,
        serviceId = serviceId,
        lastMessage = lastMessage,
        lastMessageType = lastMessageType,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        createdAt = createdAt,
    )

private fun MessageResponse.toDomainModel(): Message =
    Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        type = runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.TEXT),
        content = content ?: "",
        mediaUrl = mediaUrl,
        thumbnailUrl = thumbnailUrl,
        metadata = metadata,
        timestamp = timestamp,
        deliveredAt = deliveredAt,
        readAt = readAt,
    )

private fun MessageResponse.toEntity(): must.kdroiders.hustlehub.data.local.entity.MessageEntity =
    must.kdroiders.hustlehub.data.local.entity.MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        type = type,
        content = content,
        mediaUrl = mediaUrl,
        thumbnailUrl = thumbnailUrl,
        metadata = metadata,
        timestamp = timestamp,
        deliveredAt = deliveredAt,
        readAt = readAt,
    )
