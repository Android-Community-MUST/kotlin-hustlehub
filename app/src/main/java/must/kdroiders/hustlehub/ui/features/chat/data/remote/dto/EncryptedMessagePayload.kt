package must.kdroiders.hustlehub.ui.features.chat.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Payload sent/received over STOMP when E2EE is active.
 * The backend stores this as an opaque blob — never sees plaintext.
 */
@Keep
data class EncryptedMessagePayload(
    @SerializedName("encryptedContent")
    val encryptedContent: String, // Base64-encoded AES-256-GCM ciphertext
    @SerializedName("iv")
    val iv: String, // Base64-encoded 12-byte initialization vector
    @SerializedName("authTag")
    val authTag: String, // Base64-encoded 128-bit authentication tag
    @SerializedName("type")
    val type: String, // Original message type (TEXT, IMAGE, etc.)
)
