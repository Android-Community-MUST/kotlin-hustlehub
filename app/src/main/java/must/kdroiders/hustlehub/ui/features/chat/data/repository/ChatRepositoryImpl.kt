package must.kdroiders.hustlehub.ui.features.chat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.MessageDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.toDomain
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.toEntity
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ChatWebSocketService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ConversationApiService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.ConversationResponse
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.CreateConversationRequest
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.MessageResponse
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.SendMessageRequest
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Conversation
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl
    @Inject
    constructor(
        private val conversationApiService: ConversationApiService,
        private val chatWebSocketService: ChatWebSocketService,
        private val conversationDao: ConversationDao,
        private val messageDao: MessageDao,
        private val firebaseAuth: FirebaseAuth?,
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
                    // Optimistic UI Update: Create a temporary message
                    val tempId = "temp_${UUID.randomUUID()}"
                    val currentUserId = firebaseAuth?.currentUser?.uid ?: ""
                    val currentTimestamp = java.time.Instant
                        .now()
                        .toString()

                    // Embed localId into metadata
                    val gson = Gson()
                    val metadataJson = if (metadata != null) {
                        try {
                            gson.fromJson(metadata, JsonObject::class.java).apply {
                                addProperty("localId", tempId)
                            }
                        } catch (e: Exception) {
                            JsonObject().apply { addProperty("localId", tempId) }
                        }
                    } else {
                        JsonObject().apply { addProperty("localId", tempId) }
                    }
                    val newMetadataString = gson.toJson(metadataJson)

                    val tempMessage = Message(
                        id = tempId,
                        conversationId = conversationId,
                        senderId = currentUserId,
                        type = type,
                        content = content,
                        mediaUrl = mediaUrl,
                        thumbnailUrl = null,
                        metadata = newMetadataString,
                        timestamp = currentTimestamp,
                        deliveredAt = null,
                        readAt = null,
                    )

                    messageDao.upsert(tempMessage.toEntity())

                    val request = SendMessageRequest(
                        conversationId = conversationId,
                        type = type.name,
                        content = content,
                        mediaUrl = mediaUrl,
                        metadata = newMetadataString,
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
            // Ensure WebSocket is connected
            chatWebSocketService.connect()

            // We connect the WebSocket session, subscribe to the conversation flow,
            // and whenever a new message is received, we:
            // 1. Map to domain model
            // 2. Insert into the local database
            // 3. Update the conversation unread count and last message in local database
            return chatWebSocketService
                .subscribeToConversation(conversationId)
                .map { it.toDomainModel() }
                .onEach { message ->
                    withContext(Dispatchers.IO) {
                        val gson = Gson()

                        // Remove optimistic message if this is the real one from the server
                        if (message.senderId == firebaseAuth?.currentUser?.uid) {
                            try {
                                if (message.metadata != null) {
                                    val metaObj = gson.fromJson(message.metadata, JsonObject::class.java)
                                    if (metaObj.has("localId")) {
                                        val localId = metaObj.get("localId").asString
                                        messageDao.deleteById(localId)
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error processing localId from metadata")
                            }
                        }

                        messageDao.upsert(message.toEntity())
                        val cachedConv = conversationDao.getById(conversationId)
                        if (cachedConv != null) {
                            conversationDao.upsert(
                                cachedConv.copy(
                                    lastMessage = message.content,
                                    lastMessageType = message.type.name,
                                    lastMessageAt = message.timestamp,
                                ),
                            )
                        }
                    }
                }
        }

        override suspend fun subscribeToPresence(
            otherUserId: String,
        ): Flow<must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.UserPresence> {
            return chatWebSocketService.subscribeToPresence(otherUserId)
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

private fun ConversationResponse.toEntity(): must.kdroiders.hustlehub.ui.features.chat.data.local.entity.ConversationEntity =
    must.kdroiders.hustlehub.ui.features.chat.data.local.entity.ConversationEntity(
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

private fun MessageResponse.toEntity(): must.kdroiders.hustlehub.ui.features.chat.data.local.entity.MessageEntity =
    must.kdroiders.hustlehub.ui.features.chat.data.local.entity.MessageEntity(
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
