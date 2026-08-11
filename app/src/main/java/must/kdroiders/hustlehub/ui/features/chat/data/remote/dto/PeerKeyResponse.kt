package must.kdroiders.hustlehub.ui.features.chat.data.remote.dto

/**
 * Response from the backend containing the peer's
 * ECDH public key for a specific conversation.
 */
data class PeerKeyResponse(
    val publicKey: String, // Base64-encoded peer ECDH public key
    val userId: String, // The peer's user ID
)
