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

@Deprecated(
    message = "Renders encrypted content as raw ciphertext. Use toDecryptedDomain() for display.",
    replaceWith = ReplaceWith("toDecryptedDomain(keyExchangeHandler, cryptoManager)"),
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

/** Decrypts content using the conversation ECDH shared secret if the entity is encrypted. */
@Suppress("DEPRECATION")
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

    val secretKey = keyExchangeHandler.getCachedSecret(this.conversationId)
        ?: return rawDomain.copy(content = "[Decryption key unavailable — open chat to restore]")

    val decryptedContent = runCatching {
        cryptoManager.decrypt(
            EncryptedPayload(
                ciphertext = cipherText,
                iv = ivStr,
                authTag = tagStr,
            ),
            secretKey,
        )
    }.getOrElse { e ->
        timber.log.Timber.w(e, "Failed to decrypt Room message %s", this.id)
        "[Encrypted message]"
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

/** Encrypts content with AES-256-GCM before writing to Room if shared secret is available. */
fun Message.toEncryptedEntity(
    conversationId: String,
    keyExchangeHandler: KeyExchangeHandler,
    cryptoManager: CryptoManager,
    cachedAt: Long = System.currentTimeMillis(),
): MessageEntity {
    val secretKey = keyExchangeHandler.getCachedSecret(conversationId)
    return if (secretKey != null && !this.content.isNullOrBlank()) {
        val encrypted =
            runCatching {
                cryptoManager.encrypt(this.content, secretKey)
            }.getOrNull()
        if (encrypted != null) {
            this
                .toEntity(
                    cachedAt = cachedAt,
                    isEncrypted = true,
                    iv = encrypted.iv,
                    authTag = encrypted.authTag,
                ).copy(content = encrypted.ciphertext)
        } else {
            timber.log.Timber.w("Encryption failed for message %s — storing as plaintext", this.id)
            this.toEntity(cachedAt = cachedAt)
        }
    } else {
        this.toEntity(cachedAt = cachedAt)
    }
}
