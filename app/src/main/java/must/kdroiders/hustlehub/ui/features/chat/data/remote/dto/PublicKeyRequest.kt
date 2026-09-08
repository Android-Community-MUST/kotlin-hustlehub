package must.kdroiders.hustlehub.ui.features.chat.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Request body sent to upload our ECDH public key
 * to the backend for a specific conversation.
 */
@Keep
data class PublicKeyRequest(
    @SerializedName("publicKey")
    val publicKey: String, // Base64-encoded ECDH P-256 public key
)
