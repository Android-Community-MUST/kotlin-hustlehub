package must.kdroiders.hustlehub.ui.features.chat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import must.kdroiders.hustlehub.core.security.CryptoManager
import must.kdroiders.hustlehub.core.security.EncryptedPayload
import must.kdroiders.hustlehub.core.security.KeyExchangeHandler
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

fun MessageEntity.toDecryptedDomain(
    keyExchangeHandler: KeyExchangeHandler,
    cryptoManager: CryptoManager,
): Message {
    val rawDomain = this.toDomain()
    val cipherText = this.content
    val ivStr = this.iv
    val tagStr = this.authTag
    if (!this.isEncrypted || ivStr.isNullOrBlank() || tagStr.isNullOrBlank() || cipherText.isNullOrBlank()) {
        return rawDomain
    }

    val secretKey = keyExchangeHandler.getOrGenerateLocalSecret(this.conversationId)
    val decryptedContent = runCatching {
        cryptoManager.decrypt(
            EncryptedPayload(
                ciphertext = cipherText,
                iv = ivStr,
                authTag = tagStr,
            ),
            secretKey,
        )
    }.getOrElse {
        cipherText
    }

    return rawDomain.copy(content = decryptedContent)
}

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

fun Message.toEncryptedEntity(
    conversationId: String,
    keyExchangeHandler: KeyExchangeHandler,
    cryptoManager: CryptoManager,
    cachedAt: Long = System.currentTimeMillis(),
): MessageEntity {
    if (this.content.isBlank() || this.type != MessageType.TEXT) {
        return this.toEntity(cachedAt = cachedAt)
    }

    val secretKey = keyExchangeHandler.getOrGenerateLocalSecret(conversationId)
    val encryptedPayload = cryptoManager.encrypt(this.content, secretKey)

    return this.toEntity(
        cachedAt = cachedAt,
        isEncrypted = true,
        iv = encryptedPayload.iv,
        authTag = encryptedPayload.authTag,
    ).copy(content = encryptedPayload.ciphertext)
}
