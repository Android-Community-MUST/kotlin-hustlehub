package must.kdroiders.hustlehub.ui.features.chat.data.remote.dto

/**
 * Payload sent/received over STOMP when E2EE is active.
 * The backend stores this as an opaque blob — never sees plaintext.
 */
data class EncryptedMessagePayload(
    val encryptedContent: String, // Base64-encoded AES-256-GCM ciphertext
    val iv: String, // Base64-encoded 12-byte initialization vector
    val authTag: String, // Base64-encoded 128-bit authentication tag
    val type: String, // Original message type (TEXT, IMAGE, etc.)
)
