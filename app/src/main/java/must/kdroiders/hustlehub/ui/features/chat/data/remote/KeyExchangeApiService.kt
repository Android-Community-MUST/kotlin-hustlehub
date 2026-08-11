package must.kdroiders.hustlehub.ui.features.chat.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.PeerKeyResponse
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.PublicKeyRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit interface for the ECDH key exchange endpoints.
 *
 * Flow:
 * 1. When Alice opens a chat, she uploads her public key via [uploadPublicKey].
 * 2. She fetches Bob's public key via [getPeerPublicKey].
 * 3. Both sides do ECDH locally to derive the same shared AES key.
 */
interface KeyExchangeApiService {
    /**
     * Uploads this device's ECDH public key for a conversation.
     * Called once per conversation (or on re-install to re-key).
     */
    @POST("conversations/{conversationId}/keys")
    suspend fun uploadPublicKey(
        @Path("conversationId") conversationId: String,
        @Body request: PublicKeyRequest,
    ): ApiResponse<Unit>

    /**
     * Fetches the peer's ECDH public key for a conversation.
     * Returns 404 / null data if the peer hasn't uploaded their key yet.
     */
    @GET("conversations/{conversationId}/keys")
    suspend fun getPeerPublicKey(
        @Path("conversationId") conversationId: String,
    ): ApiResponse<PeerKeyResponse>
}
