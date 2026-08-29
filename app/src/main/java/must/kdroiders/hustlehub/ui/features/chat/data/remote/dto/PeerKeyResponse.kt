package must.kdroiders.hustlehub.ui.features.chat.data.remote.dto

/**
 * Response from the backend containing the peer's
 * ECDH public key for a specific conversation.
 */
data class PeerKeyResponse(
    val publicKey: String? = null,
    val userId: String? = null,
)
