package must.kdroiders.hustlehub.ui.features.chat.data.remote.dto

/**
 * Request body sent to upload our ECDH public key
 * to the backend for a specific conversation.
 */
data class PublicKeyRequest(
    val publicKey: String, // Base64-encoded ECDH P-256 public key
)
