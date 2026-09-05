package must.kdroiders.hustlehub.ui.features.chat.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Response from the backend containing the peer's
 * ECDH public key for a specific conversation.
 */
@Keep
data class PeerKeyResponse(
    @SerializedName("publicKey")
    val publicKey: String? = null,
    @SerializedName("userId")
    val userId: String? = null,
)
