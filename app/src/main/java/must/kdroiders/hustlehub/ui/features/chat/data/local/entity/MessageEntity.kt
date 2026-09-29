package must.kdroiders.hustlehub.ui.features.chat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import must.kdroiders.hustlehub.core.security.Base64Util
import must.kdroiders.hustlehub.core.security.CryptoManager
import must.kdroiders.hustlehub.core.security.EncryptedPayload
import must.kdroiders.hustlehub.core.security.KeyExchangeHandler
import must.kdroiders.hustlehub.ui.features.chat.domain.model.Message
import must.kdroiders.hustlehub.ui.features.chat.domain.model.MessageType
import timber.log.Timber
import javax.crypto.SecretKey

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
        content = if (content?.trim()?.startsWith("{\"localId\"") == true) "" else (content ?: ""),
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

    if (!this.isEncrypted) {
        return rawDomain
    }

    if (cipherText.isNullOrBlank() || cipherText == "[Encrypted message]") {
        Timber.tag("CHAT_DECRYPT").w(
            "[CHAT_DECRYPT_FAILURE] Message %s is marked encrypted, but content is blank or placeholder '[Encrypted message]'! convId=%s, sender=%s",
            this.id,
            this.conversationId,
            this.senderId,
        )
        return rawDomain
    }

    if (ivStr.isNullOrBlank() || tagStr.isNullOrBlank()) {
        Timber.tag("CHAT_DECRYPT").w(
            "[CHAT_DECRYPT_FAILURE] Message %s is marked encrypted, but missing IV or AuthTag! iv=%s, tag=%s, convId=%s",
            this.id,
            ivStr,
            tagStr,
            this.conversationId,
        )
        return rawDomain
    }

    val ourIdentityPubKey = runCatching { keyExchangeHandler.getOurIdentityPublicKey() }.getOrDefault("<unknown>")
    val ourConvPubKey = runCatching { keyExchangeHandler.getOurConversationPublicKey(this.conversationId) }.getOrDefault("<unknown>")
    val peerPubKey = keyExchangeHandler.getCachedPeerPublicKey(this.conversationId) ?: "<none cached>"

    val candidates = keyExchangeHandler.getCandidateSecrets(this.conversationId).ifEmpty {
        listOfNotNull(keyExchangeHandler.getCachedSecret(this.conversationId))
    }
    if (candidates.isEmpty()) {
        Timber.tag("CHAT_DECRYPT").e(
            "[CHAT_DECRYPT_FAILURE] NO CANDIDATE SECRET KEYS for convId=%s (msgId=%s)! OurIdentityPubKey: %s, OurConvPubKey: %s, PeerPubKey: %s",
            this.conversationId,
            this.id,
            ourIdentityPubKey,
            ourConvPubKey,
            peerPubKey,
        )
        return rawDomain
    }

    var decryptedContent: String? = null
    var workingKey: SecretKey? = null

    for (candidate in candidates) {
        val result = runCatching {
            cryptoManager.decrypt(
                EncryptedPayload(
                    ciphertext = cipherText,
                    iv = ivStr,
                    authTag = tagStr,
                ),
                candidate,
            )
        }
        if (result.isSuccess) {
            decryptedContent = result.getOrNull()
            workingKey = candidate
            break
        }
    }

    if (workingKey != null && decryptedContent != null) {
        val cached = keyExchangeHandler.getCachedSecret(this.conversationId)
        if (cached == null || !cached.encoded.contentEquals(workingKey.encoded)) {
            val keyBase64 = runCatching { Base64Util.encodeToString(workingKey.encoded) }.getOrDefault("<unknown>")
            Timber.tag("CHAT_DECRYPT").i(
                "[CHAT_DECRYPT_RECOVERED] Successfully decrypted msgId=%s using candidate key: %s! Updating cached secret for convId=%s",
                this.id,
                keyBase64,
                this.conversationId,
            )
            keyExchangeHandler.cacheSecret(this.conversationId, workingKey)
        }
    } else {
        val firstKeyBase64 = candidates.firstOrNull()?.let { runCatching { Base64Util.encodeToString(it.encoded) }.getOrDefault("<unknown>") } ?: "<none>"
        Timber.tag("CHAT_DECRYPT").e(
            "[CHAT_DECRYPT_FAILURE] All %d candidate secret keys failed decryption on msgId=%s!\n" +
                "  -> convId: %s\n" +
                "  -> senderId: %s\n" +
                "  -> TestedKeyCount: %d (first: %s)\n" +
                "  -> OurIdentityPubKey: %s\n" +
                "  -> OurConvPubKey: %s\n" +
                "  -> PeerPubKey: %s\n" +
                "  -> IV: %s\n" +
                "  -> AuthTag: %s\n" +
                "  -> Ciphertext: %s",
            candidates.size,
            this.id,
            this.conversationId,
            this.senderId,
            candidates.size,
            firstKeyBase64,
            ourIdentityPubKey,
            ourConvPubKey,
            peerPubKey,
            ivStr,
            tagStr,
            cipherText,
        )
        decryptedContent = cipherText
    }

    val cleanContent = if (decryptedContent.trim().startsWith("{\"localId\"")) {
        ""
    } else {
        decryptedContent
    }

    Timber.tag("CHAT_DECRYPT").d(
        "[CHAT_DECRYPT] Message %s decrypted result: '%s' (isDecrypted=%b)",
        this.id,
        cleanContent,
        cleanContent != cipherText,
    )

    return rawDomain.copy(content = cleanContent)
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
    val keyBase64 = secretKey?.let { runCatching { Base64Util.encodeToString(it.encoded) }.getOrNull() } ?: "<null>"
    Timber.tag("CHAT_ROOM").d(
        "[CHAT_ROOM_SAVE] toEncryptedEntity for msgId=%s, convId=%s, content='%s', SecretKey: %s",
        this.id,
        conversationId,
        this.content,
        keyBase64,
    )
    return if (secretKey != null && !this.content.isNullOrBlank() && this.content != "[Encrypted message]" && !this.content.trim().startsWith("{\"localId\"")) {
        val encrypted =
            runCatching {
                cryptoManager.encrypt(this.content, secretKey)
            }.getOrNull()
        if (encrypted != null) {
            Timber.tag("CHAT_ROOM").d(
                "[CHAT_ROOM_SAVE] Message %s encrypted successfully for Room: ciphertext=%s, iv=%s, tag=%s",
                this.id,
                encrypted.ciphertext,
                encrypted.iv,
                encrypted.authTag,
            )
            this
                .toEntity(
                    cachedAt = cachedAt,
                    isEncrypted = true,
                    iv = encrypted.iv,
                    authTag = encrypted.authTag,
                ).copy(content = encrypted.ciphertext)
        } else {
            Timber.tag("CHAT_ROOM").w("[CHAT_ROOM_SAVE] Encryption failed for message %s — storing as plaintext", this.id)
            this.toEntity(cachedAt = cachedAt)
        }
    } else {
        Timber.tag("CHAT_ROOM").d("[CHAT_ROOM_SAVE] No secretKey or content empty/placeholder for msg %s — storing as plaintext", this.id)
        this.toEntity(cachedAt = cachedAt)
    }
}
