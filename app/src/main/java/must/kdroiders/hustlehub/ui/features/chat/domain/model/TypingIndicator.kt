package must.kdroiders.hustlehub.ui.features.chat.domain.model

import com.google.gson.annotations.SerializedName

data class TypingIndicator(
    @SerializedName("conversationId") val conversationId: String,
    @SerializedName("senderId") val senderId: String,
    @SerializedName("isTyping") val isTyping: Boolean,
) {
    val userId: String get() = senderId
}
