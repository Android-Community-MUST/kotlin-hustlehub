package must.kdroiders.hustlehub.ui.features.chat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val type: String,
    val content: String?,
    val mediaUrl: String?,
    val thumbnailUrl: String?,
    val metadata: String?,
    val timestamp: String,
    val deliveredAt: String?,
    val readAt: String?,
    val isSynced: Boolean = false,
    val isFailed: Boolean = false,
    // E2EE fields
    val isEncrypted: Boolean = false,
    val iv: String? = null,
    val authTag: String? = null,
    val cachedAt: Long = System.currentTimeMillis(),
)

fun MessageEntity.toDomain(): Message =
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
        isSynced = isSynced,
        isFailed = isFailed,
    )

fun Message.toEntity(
    cachedAt: Long = System.currentTimeMillis(),
    isEncrypted: Boolean = false,
    iv: String? = null,
    authTag: String? = null,
): MessageEntity =
    MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        type = type.name,
        content = content,
        mediaUrl = mediaUrl,
        thumbnailUrl = thumbnailUrl,
        metadata = metadata,
        timestamp = timestamp,
        deliveredAt = deliveredAt,
        readAt = readAt,
        isSynced = isSynced,
        isFailed = isFailed,
        isEncrypted = isEncrypted,
        iv = iv,
        authTag = authTag,
        cachedAt = cachedAt,
    )
