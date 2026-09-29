package must.kdroiders.hustlehub.ui.features.chat.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.core.notification.AppBadgeHelper
import must.kdroiders.hustlehub.core.notification.NotificationHelper
import must.kdroiders.hustlehub.core.security.CryptoManager
import must.kdroiders.hustlehub.core.security.KeyExchangeHandler
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.MessageDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.ConversationEntity
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.MessageEntity
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.toDecryptedDomain
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.toDomain
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.toEncryptedEntity
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
import must.kdroiders.hustlehub.ui.features.chat.domain.model.UserPresence
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val conversationApiService: ConversationApiService,
        private val conversationDao: ConversationDao,
        private val messageDao: MessageDao,
        private val chatWebSocketService: ChatWebSocketService,
        private val firebaseAuth: FirebaseAuth?,
        private val keyExchangeHandler: KeyExchangeHandler,
        private val cryptoManager: CryptoManager,
    ) : ChatRepository {
        @Volatile
        private var activeConversationId: String? = null

        private val messageSendAttempts = ConcurrentHashMap<String, AtomicInteger>()
        private val serverMessageArrivalCounts = ConcurrentHashMap<String, AtomicInteger>()

        // Tracks IDs dispatched to the current WS session but not yet ACK'd by the server.
        // Prevents resendUnsyncedMessages() from re-sending a message that was just sent
        // by the normal send path in the same session.
        private val inFlightIds: MutableSet<String> = ConcurrentHashMap.newKeySet()

        private val decryptionCache = androidx.collection.LruCache<String, Message>(500)

        override fun clearInFlightIds() = inFlightIds.clear()

        override fun setActiveConversation(conversationId: String?) {
            activeConversationId = conversationId
        }

        override fun getConversations(): Flow<List<Conversation>> {
            return conversationDao.getAll().map { entities ->
                entities.map { it.toDomain() }
            }
        }

        override suspend fun refreshConversations(): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = conversationApiService.getConversations(0, 50)
                    check(response.success && response.data != null) { response.message ?: "Failed to refresh conversations" }
                    val entities = response.data.content.map { dto ->
                        val existing = conversationDao.getById(dto.id)
                        dto.toEntity(keyExchangeHandler, cryptoManager, existing)
                    }
                    conversationDao.upsertAll(entities)
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.e(e, "Failed to refresh conversations")
                }
            }

        override suspend fun getOrCreateConversation(
            otherUserId: String,
            serviceId: String?,
        ): Result<Conversation> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val request = CreateConversationRequest(otherUserId, serviceId)
                    val response = conversationApiService.getOrCreateConversation(request)
                    check(response.success && response.data != null) { response.message ?: "Failed to get or create conversation" }
                    val conversation = response.data.toDomainModel()
                    conversationDao.upsert(conversation.toEntity())
                    conversation
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.e(e, "Failed to get or create conversation")
                }
            }

        override fun getMessages(conversationId: String): Flow<List<Message>> =
            messageDao
                .getByConversation(conversationId)
                .map { entities ->
                    entities.map { entity ->
                        decryptionCache.get(entity.id)
                            ?: entity
                                .toDecryptedDomain(keyExchangeHandler, cryptoManager)
                                .also { decryptionCache.put(entity.id, it) }
                    }
                }.flowOn(Dispatchers.Default)

        override suspend fun loadMessageHistory(
            conversationId: String,
            page: Int,
        ): Result<Boolean> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val cachedConv = conversationDao.getById(conversationId)
                    Timber.tag("CHAT_HISTORY").d(
                        "[CHAT_HISTORY] loadMessageHistory requested: convId=%s, page=%d, otherUserId=%s",
                        conversationId,
                        page,
                        cachedConv?.otherUserId,
                    )
                    keyExchangeHandler.ensureKeysExchanged(conversationId, cachedConv?.otherUserId)
                    val response = conversationApiService.getMessages(conversationId, page, 50)
                    check(response.success && response.data != null) { response.message ?: "Failed to load message history" }
                    Timber.tag("CHAT_HISTORY").d(
                        "[CHAT_HISTORY] Received %d messages from backend for convId=%s",
                        response.data.content.size,
                        conversationId,
                    )
                    val gson = Gson()
                    val localIdsToDelete = response.data.content.mapNotNull { msg ->
                        if (msg.metadata != null) {
                            runCatching {
                                val metaObj = gson.fromJson(msg.metadata, JsonObject::class.java)
                                if (metaObj.has("localId")) {
                                    metaObj.get("localId").asString
                                } else {
                                    null
                                }
                            }.getOrNull()
                        } else {
                            null
                        }
                    }
                    if (localIdsToDelete.isNotEmpty()) {
                        Timber.tag("CHAT_HISTORY").d("[CHAT_HISTORY] Deleting local optimistic messages: %s", localIdsToDelete)
                        localIdsToDelete.forEach { messageDao.deleteById(it) }
                    }
                    var hadDecryptionFailure = false
                    val entities = response.data.content.mapNotNull { msgDto ->
                        val roomEntity = msgDto.toRoomEntity()
                        val msgDomain = roomEntity.toDecryptedDomain(keyExchangeHandler, cryptoManager)
                        if (roomEntity.isEncrypted && msgDomain.content == roomEntity.content) {
                            hadDecryptionFailure = true
                        }
                        Timber.tag("CHAT_HISTORY").d(
                            "[CHAT_HISTORY] History msg %s: isEncrypted=%b, rawContent='%s', decryptedContent='%s'",
                            msgDto.id,
                            roomEntity.isEncrypted,
                            roomEntity.content,
                            msgDomain.content,
                        )
                        val processed = applyDeletionStatusToMessage(msgDomain)
                        if (processed != null) roomEntity else null
                    }
                    if (hadDecryptionFailure) {
                        Timber.tag("CHAT_HISTORY").w("[CHAT_HISTORY] One or more messages failed local decryption; force-refreshing peer keys from backend...")
                        runCatching {
                            keyExchangeHandler.ensureKeysExchanged(conversationId, cachedConv?.otherUserId, forceRefresh = true)
                        }
                    }
                    messageDao.upsertAll(entities)
                    Timber.tag("CHAT_HISTORY").d("[CHAT_HISTORY] Upserted %d history messages into Room for convId=%s", entities.size, conversationId)
                    response.data.content.isNotEmpty()
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.tag("CHAT_HISTORY").e(e, "[CHAT_HISTORY] Failed to load message history for convId=%s", conversationId)
                    if (e is retrofit2.HttpException && e.code() == 404) {
                        conversationDao.deleteById(conversationId)
                        messageDao.deleteByConversation(conversationId)
                    }
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
                runCatching {
                    val tempId = "temp_${UUID.randomUUID()}"
                    val currentUserId = firebaseAuth?.currentUser?.uid ?: ""
                    val currentTimestamp = runCatching {
                        java.time.Instant
                            .now()
                            .toString()
                    }.getOrDefault(System.currentTimeMillis().toString())

                    val sendAttempt = messageSendAttempts.computeIfAbsent(tempId) { AtomicInteger(0) }.incrementAndGet()
                    Timber.tag("CHAT_SEND").d(
                        "[CHAT_SEND_ATTEMPT] Send attempt #%d for message tempId=%s (convId=%s, sender=%s, type=%s, content='%s')",
                        sendAttempt,
                        tempId,
                        conversationId,
                        currentUserId,
                        type,
                        content,
                    )

                    val gson = Gson()
                    val metadataJson = if (metadata != null) {
                        runCatching {
                            gson.fromJson(metadata, JsonObject::class.java).apply {
                                addProperty("localId", tempId)
                            }
                        }.getOrElse {
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
                        isSynced = false,
                        isFailed = false,
                    )

                    val encryptedEntity = tempMessage.toEncryptedEntity(conversationId, keyExchangeHandler, cryptoManager)
                    Timber.tag("CHAT_ROOM").d(
                        "[CHAT_ROOM_SAVE] Saving temp optimistic message to Room: id=%s, isEncrypted=%b, iv=%s, tag=%s, content='%s'",
                        encryptedEntity.id,
                        encryptedEntity.isEncrypted,
                        encryptedEntity.iv,
                        encryptedEntity.authTag,
                        encryptedEntity.content,
                    )
                    messageDao.upsert(encryptedEntity)

                    val cachedConv = conversationDao.getById(conversationId)
                    val request = SendMessageRequest(
                        conversationId = conversationId,
                        type = type.name,
                        content = content,
                        mediaUrl = mediaUrl,
                        metadata = newMetadataString,
                    )
                    Timber.tag("CHAT_SEND").d(
                        "[CHAT_SEND] Connecting and sending message via WebSocket for convId=%s, otherUserId=%s",
                        conversationId,
                        cachedConv?.otherUserId,
                    )
                    chatWebSocketService.connect()
                    chatWebSocketService.sendMessage(request, cachedConv?.otherUserId)
                    inFlightIds.add(tempId)
                    Unit
                }.recover { e ->
                    if (e is CancellationException) throw e
                    Timber.tag("CHAT_SEND").d(e, "[CHAT_SEND] Message queued in local database for auto-sync: convId=%s", conversationId)
                    Unit
                }
            }

        override suspend fun markAsRead(conversationId: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    Timber.tag("CHAT_READ").d("[CHAT_READ] markAsRead initiated for convId=%s", conversationId)
                    val response = conversationApiService.markAsRead(conversationId)
                    check(response.success) { response.message ?: "Failed to mark conversation as read" }
                    val cached = conversationDao.getById(conversationId)
                    if (cached != null) {
                        conversationDao.upsert(cached.copy(unreadCount = 0))
                    }
                    val remaining = conversationDao.getTotalUnreadCountSync()
                    AppBadgeHelper.applyBadgeCount(context, remaining)
                    Timber.tag("CHAT_READ").d("[CHAT_READ] markAsRead succeeded for convId=%s, unreadCount reset to 0 in Room, remaining=%d", conversationId, remaining)
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.tag("CHAT_READ").e(e, "[CHAT_READ] Failed to mark conversation as read for convId=%s", conversationId)
                    if (e is retrofit2.HttpException && e.code() == 404) {
                        conversationDao.deleteById(conversationId)
                        messageDao.deleteByConversation(conversationId)
                    }
                }
            }

        override suspend fun connectWebSocket(conversationId: String): Flow<Message> =
            flow {
                try {
                    val cachedConv = conversationDao.getById(conversationId)
                    Timber.tag("CHAT_WS").d(
                        "[CHAT_WS] connectWebSocket for convId=%s, otherUserId=%s. Ensuring keys exchanged...",
                        conversationId,
                        cachedConv?.otherUserId,
                    )
                    keyExchangeHandler.ensureKeysExchanged(conversationId, cachedConv?.otherUserId)
                    chatWebSocketService.connect()
                    resendUnsyncedMessages()

                    chatWebSocketService
                        .subscribeToConversation(conversationId)
                        .collect { msgDto ->
                            val arrivalCount = serverMessageArrivalCounts.computeIfAbsent(msgDto.id) { AtomicInteger(0) }.incrementAndGet()
                            if (arrivalCount > 1) {
                                Timber.tag("CHAT_RECEIVE").w(
                                    "[CHAT_DUPLICATE_RECEIVED] Message with serverId=%s has arrived %d times over WebSocket! (convId=%s, senderId=%s)",
                                    msgDto.id,
                                    arrivalCount,
                                    conversationId,
                                    msgDto.senderId,
                                )
                            } else {
                                Timber.tag("CHAT_RECEIVE").d(
                                    "[CHAT_RECEIVE_FIRST] First arrival of serverId=%s over WebSocket (convId=%s, senderId=%s)",
                                    msgDto.id,
                                    conversationId,
                                    msgDto.senderId,
                                )
                            }
                            Timber.tag("CHAT_RECEIVE").d(
                                "[CHAT_RECEIVE] Incoming message from WebSocket frame: id=%s, sender=%s, type=%s, encryptedContent=%s, content='%s', iv=%s, tag=%s",
                                msgDto.id,
                                msgDto.senderId,
                                msgDto.type,
                                msgDto.encryptedContent,
                                msgDto.content,
                                msgDto.iv,
                                msgDto.authTag,
                            )
                            val entity = msgDto.toRoomEntity()
                            Timber.tag("CHAT_RECEIVE").d(
                                "[CHAT_RECEIVE] toRoomEntity: id=%s, isEncrypted=%b, iv=%s, tag=%s, content='%s'",
                                entity.id,
                                entity.isEncrypted,
                                entity.iv,
                                entity.authTag,
                                entity.content,
                            )
                            var message = entity.toDecryptedDomain(keyExchangeHandler, cryptoManager)
                            if (entity.isEncrypted && message.content == entity.content) {
                                Timber.tag("CHAT_RECEIVE").w("[CHAT_RECEIVE] Live message %s failed local decryption; force-refreshing peer keys...", msgDto.id)
                                val cachedConv = conversationDao.getById(conversationId)
                                runCatching {
                                    keyExchangeHandler.ensureKeysExchanged(conversationId, cachedConv?.otherUserId, forceRefresh = true)
                                }
                                message = entity.toDecryptedDomain(keyExchangeHandler, cryptoManager)
                            }
                            Timber.tag("CHAT_RECEIVE").d(
                                "[CHAT_RECEIVE] Decrypted domain result: id=%s, content='%s'",
                                message.id,
                                message.content,
                            )
                            val processed = withContext(Dispatchers.IO) {
                                val gson = Gson()

                                val cachedConv = conversationDao.getById(conversationId)
                                val isFromOtherUser = cachedConv?.let { message.senderId == it.otherUserId } ?: false
                                val isFromSelf = !isFromOtherUser

                                var finalMessage = message
                                if (isFromSelf) {
                                    var existingLocalContent: String? = null
                                    if (message.metadata != null) {
                                        runCatching {
                                            val metaObj = gson.fromJson(message.metadata, JsonObject::class.java)
                                            if (metaObj.has("localId")) {
                                                val localId = metaObj.get("localId").asString
                                                val totalAttempts = messageSendAttempts[localId]?.get() ?: 1
                                                Timber.tag("CHAT_ACK").d(
                                                    "[CHAT_ACK] Server ACK matched for localId=%s -> serverId=%s (was sent %d time(s)). Deleting optimistic Room entry.",
                                                    localId,
                                                    message.id,
                                                    totalAttempts,
                                                )
                                                val localMsg = messageDao.getById(localId)
                                                if (localMsg != null && !localMsg.content.isNullOrBlank() && localMsg.content != "[Encrypted message]") {
                                                    existingLocalContent = localMsg.content
                                                }
                                                messageDao.deleteById(localId)
                                                messageSendAttempts.remove(localId)
                                                inFlightIds.remove(localId)
                                            }
                                        }.onFailure { e ->
                                            if (e is CancellationException) throw e
                                            Timber.e(e, "Error processing localId from metadata")
                                        }
                                    }
                                    val unsynced = messageDao.getUnsyncedMessages().filter { it.conversationId == conversationId }
                                    unsynced.forEach {
                                        messageDao.deleteById(it.id)
                                        inFlightIds.remove(it.id)
                                    }

                                    if (existingLocalContent != null && (message.content.isBlank() || message.content == "[Encrypted message]")) {
                                        finalMessage = message.copy(content = existingLocalContent)
                                    }
                                }

                                val proc = applyDeletionStatusToMessage(finalMessage)
                                if (proc != null) {
                                    messageDao.upsert(entity)
                                    if (cachedConv != null) {
                                        val isActive = conversationId == activeConversationId

                                        conversationDao.upsert(
                                            cachedConv.copy(
                                                lastMessage = proc.content,
                                                lastMessageType = proc.type.name,
                                                lastMessageAt = proc.timestamp,
                                                unreadCount = if (isFromOtherUser && !isActive) {
                                                    cachedConv.unreadCount + 1
                                                } else {
                                                    0
                                                },
                                            ),
                                        )

                                        if (isFromOtherUser && !isActive) {
                                            val senderName = cachedConv.otherUserName
                                            val preview = when (proc.type.name) {
                                                "VOICE" -> "Sent a voice note"
                                                "IMAGE" -> "Sent an image"
                                                "LOCATION" -> "Shared a location"
                                                else -> proc.content.take(80)
                                            }
                                            val totalUnread = conversationDao.getTotalUnreadCountSync()
                                            NotificationHelper.postMessageNotification(
                                                context = context,
                                                conversationId = conversationId,
                                                senderName = senderName,
                                                messagePreview = preview,
                                                unreadCount = if (totalUnread > 0) totalUnread else 1,
                                            )
                                        } else if (isFromOtherUser && isActive) {
                                            markAsRead(conversationId)
                                        }
                                    }
                                } else {
                                    messageDao.deleteById(message.id)
                                    if (cachedConv != null && cachedConv.lastMessageAt == message.timestamp) {
                                        val latest = messageDao.getLatestMessage(conversationId)
                                        if (latest != null) {
                                            val decryptedLatest = latest.toDecryptedDomain(keyExchangeHandler, cryptoManager)
                                            conversationDao.upsert(
                                                cachedConv.copy(
                                                    lastMessage = decryptedLatest.content,
                                                    lastMessageType = decryptedLatest.type.name,
                                                    lastMessageAt = decryptedLatest.timestamp,
                                                ),
                                            )
                                        }
                                    }
                                }
                                proc
                            }
                            if (processed != null) {
                                emit(processed)
                            }
                        }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Timber.e(e, "Error in connectWebSocket flow")
                }
            }

        override suspend fun subscribeToPresence(otherUserId: String): Flow<UserPresence> {
            return chatWebSocketService.subscribeToPresence(otherUserId)
        }

        override suspend fun disconnectWebSocket() {
            chatWebSocketService.disconnect()
        }

        override suspend fun deleteConversation(conversationId: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = conversationApiService.deleteConversation(conversationId)
                    check(response.success) { response.message ?: "Failed to delete conversation" }
                    conversationDao.deleteById(conversationId)
                    messageDao.deleteByConversation(conversationId)
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.e(e, "Failed to delete conversation")
                }
            }

        override suspend fun completeService(conversationId: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = conversationApiService.completeService(conversationId)
                    check(response.success) { response.message ?: "Failed to complete service" }
                    Unit
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.e(e, "Failed to complete service for conversation $conversationId")
                }
            }

        override suspend fun deleteMessageForMe(messageId: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    markMessageDeletedForMe(messageId)

                    runCatching {
                        conversationApiService.deleteMessageForMe(messageId)
                    }.onFailure { e ->
                        if (e is CancellationException) throw e
                        Timber.w(e, "Remote deleteMessageForMe failed, treating as local-only success")
                    }

                    val cached = messageDao.getById(messageId)
                    messageDao.deleteById(messageId)
                    if (cached != null) {
                        val conv = conversationDao.getById(cached.conversationId)
                        if (conv != null && conv.lastMessageAt == cached.timestamp) {
                            val latest = messageDao.getLatestMessage(cached.conversationId)
                            if (latest != null) {
                                val decryptedLatest = latest.toDecryptedDomain(keyExchangeHandler, cryptoManager)
                                conversationDao.upsert(
                                    conv.copy(
                                        lastMessage = decryptedLatest.content,
                                        lastMessageType = decryptedLatest.type.name,
                                        lastMessageAt = decryptedLatest.timestamp,
                                    ),
                                )
                            }
                        }
                    }
                    Unit
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                }
            }

        override suspend fun deleteMessageForEveryone(messageId: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    markMessageDeletedForEveryone(messageId)

                    runCatching {
                        conversationApiService.deleteMessageForEveryone(messageId)
                    }.onFailure { e ->
                        if (e is CancellationException) throw e
                        Timber.w(e, "Remote deleteMessageForEveryone failed, treating as local-only success")
                    }

                    val cached = messageDao.getById(messageId)
                    if (cached != null) {
                        val gson = Gson()
                        val metaObj = runCatching {
                            if (!cached.metadata.isNullOrBlank()) {
                                gson.fromJson(cached.metadata, JsonObject::class.java)
                            } else {
                                JsonObject()
                            }
                        }.getOrElse { JsonObject() }

                        metaObj.addProperty("isDeleted", true)
                        val updatedDomain = cached.toDecryptedDomain(keyExchangeHandler, cryptoManager).copy(
                            content = "This message was deleted",
                            mediaUrl = null,
                            thumbnailUrl = null,
                            metadata = gson.toJson(metaObj),
                        )
                        messageDao.upsert(updatedDomain.toEncryptedEntity(cached.conversationId, keyExchangeHandler, cryptoManager))

                        val conv = conversationDao.getById(cached.conversationId)
                        if (conv != null && conv.lastMessageAt == cached.timestamp) {
                            conversationDao.upsert(
                                conv.copy(
                                    lastMessage = "This message was deleted",
                                    lastMessageType = "TEXT",
                                ),
                            )
                        }
                    }
                    Unit
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                }
            }

        private val sharedPrefs by lazy {
            context.getSharedPreferences("hustlehub_chat_deletions", Context.MODE_PRIVATE)
        }

        private fun markMessageDeletedForMe(messageId: String) {
            sharedPrefs.edit().putString(messageId, "for_me").apply()
        }

        private fun markMessageDeletedForEveryone(messageId: String) {
            sharedPrefs.edit().putString(messageId, "for_everyone").apply()
        }

        private fun getDeletionStatus(messageId: String): String? {
            return sharedPrefs.getString(messageId, null)
        }

        private fun applyDeletionStatusToMessage(message: Message): Message? {
            val status = getDeletionStatus(message.id) ?: return message
            if (status == "for_me") return null

            val gson = Gson()
            val metaObj = runCatching {
                if (!message.metadata.isNullOrBlank()) {
                    gson.fromJson(message.metadata, JsonObject::class.java)
                } else {
                    JsonObject()
                }
            }.getOrElse { JsonObject() }

            metaObj.addProperty("isDeleted", true)
            return message.copy(
                content = "This message was deleted",
                mediaUrl = null,
                thumbnailUrl = null,
                metadata = gson.toJson(metaObj),
            )
        }

        override suspend fun resendUnsyncedMessages(): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val unsynced = messageDao.getUnsyncedMessages()
                    if (unsynced.isEmpty()) return@runCatching

                    chatWebSocketService.connect()
                    unsynced.forEach { entity ->
                        if (inFlightIds.contains(entity.id)) {
                            Timber.tag("CHAT_SEND").d(
                                "[CHAT_RESEND_SKIP] Skipping msgId=%s — already in-flight in current session",
                                entity.id,
                            )
                            return@forEach
                        }
                        val resendAttempt = messageSendAttempts.computeIfAbsent(entity.id) { AtomicInteger(0) }.incrementAndGet()
                        if (resendAttempt > 3) {
                            Timber.tag("CHAT_SEND").w(
                                "[CHAT_RESEND_GIVE_UP] msgId=%s exceeded max retries — marking failed",
                                entity.id,
                            )
                            messageDao.markFailed(entity.id)
                            messageSendAttempts.remove(entity.id)
                            return@forEach
                        }
                        val decryptedDomain = entity.toDecryptedDomain(keyExchangeHandler, cryptoManager)
                        Timber.tag("CHAT_SEND").w(
                            "[CHAT_RESEND_ATTEMPT] Resending unsynced message attempt #%d for msgId=%s (convId=%s)",
                            resendAttempt,
                            entity.id,
                            entity.conversationId,
                        )
                        val request = SendMessageRequest(
                            conversationId = entity.conversationId,
                            type = entity.type,
                            content = decryptedDomain.content,
                            mediaUrl = entity.mediaUrl,
                            metadata = entity.metadata,
                        )
                        runCatching {
                            chatWebSocketService.sendMessage(request)
                            inFlightIds.add(entity.id)
                        }.onFailure { e ->
                            Timber.tag("CHAT_SEND").e(e, "[CHAT_RESEND_FAILED] Failed to sync pending message %s on attempt #%d", entity.id, resendAttempt)
                            if (resendAttempt >= 3) {
                                messageDao.markFailed(entity.id)
                                messageSendAttempts.remove(entity.id)
                            }
                        }
                    }
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.tag("CHAT_SEND").e(e, "[CHAT_RESEND_FAILED] Failed to resend unsynced messages")
                }
            }

        override suspend fun retryMessage(messageId: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val entity = messageDao.getById(messageId) ?: return@runCatching
                    messageDao.markPendingRetry(messageId)
                    messageSendAttempts.remove(messageId)
                    inFlightIds.remove(messageId)
                    Timber.tag("CHAT_SEND").d("[CHAT_RETRY] Manual retry triggered for msgId=%s", messageId)
                    sendMessage(
                        conversationId = entity.conversationId,
                        type = runCatching { MessageType.valueOf(entity.type) }.getOrDefault(MessageType.TEXT),
                        content = entity.toDecryptedDomain(keyExchangeHandler, cryptoManager).content,
                        mediaUrl = entity.mediaUrl,
                        metadata = entity.metadata,
                    )
                    Unit
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.tag("CHAT_SEND").e(e, "[CHAT_RETRY] Manual retry failed for msgId=%s", messageId)
                }
            }
    }

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
        isArchived = isArchived ?: false,
    )

private fun ConversationResponse.toEntity(
    keyExchangeHandler: KeyExchangeHandler? = null,
    cryptoManager: CryptoManager? = null,
    existingEntity: ConversationEntity? = null,
): ConversationEntity {
    val rawLastMessage = lastMessage
    val resolvedLastMessage = if (!rawLastMessage.isNullOrBlank()) {
        existingEntity?.lastMessage ?: rawLastMessage
    } else {
        existingEntity?.lastMessage
    }

    return ConversationEntity(
        id = id,
        otherUserId = otherUserId,
        otherUserName = otherUserName,
        otherUserAvatar = otherUserAvatar,
        serviceId = serviceId,
        lastMessage = resolvedLastMessage,
        lastMessageType = lastMessageType ?: existingEntity?.lastMessageType,
        lastMessageAt = lastMessageAt ?: existingEntity?.lastMessageAt,
        unreadCount = unreadCount,
        createdAt = createdAt,
        isArchived = isArchived ?: false,
    )
}

private fun MessageResponse.toRoomEntity(): MessageEntity {
    val gson = Gson()
    var parsedIv: String? = iv
    var parsedAuthTag: String? = authTag
    var isEncryptedMsg = false

    if (!metadata.isNullOrBlank()) {
        runCatching {
            val metaObj = gson.fromJson(metadata, JsonObject::class.java)
            if (metaObj.has("iv")) parsedIv = metaObj.get("iv").asString
            if (metaObj.has("authTag")) parsedAuthTag = metaObj.get("authTag").asString
            if (metaObj.has("encrypted")) isEncryptedMsg = metaObj.get("encrypted").asBoolean
        }
    }

    val ciphertext = encryptedContent ?: if (isEncryptedMsg || !parsedIv.isNullOrBlank()) content else null
    val effectiveIsEncrypted = !ciphertext.isNullOrBlank() && !parsedIv.isNullOrBlank()

    val plaintextContent = content?.takeIf {
        it.isNotBlank() && it != "[Encrypted message]" && it != ciphertext && !it.trim().startsWith("{\"localId\"")
    }
    val finalContent = plaintextContent ?: (if (effectiveIsEncrypted) ciphertext else content)

    return MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        type = type,
        content = finalContent,
        mediaUrl = mediaUrl,
        thumbnailUrl = thumbnailUrl,
        metadata = metadata,
        timestamp = timestamp,
        deliveredAt = deliveredAt,
        readAt = readAt,
        isSynced = true,
        isFailed = false,
        isEncrypted = effectiveIsEncrypted,
        iv = parsedIv,
        authTag = parsedAuthTag,
    )
}

private fun MessageResponse.toDomainModel(
    keyExchangeHandler: KeyExchangeHandler? = null,
    cryptoManager: CryptoManager? = null,
): Message {
    val entity = this.toRoomEntity()
    return if (keyExchangeHandler != null && cryptoManager != null) {
        val decrypted = entity.toDecryptedDomain(keyExchangeHandler, cryptoManager)
        if (decrypted.content.isBlank() && !this.content.isNullOrBlank()) {
            decrypted.copy(content = this.content)
        } else {
            decrypted
        }
    } else {
        @Suppress("DEPRECATION")
        entity.toDomain()
    }
}
