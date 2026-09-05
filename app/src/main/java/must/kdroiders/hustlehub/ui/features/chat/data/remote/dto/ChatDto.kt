package must.kdroiders.hustlehub.ui.features.chat.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ConversationResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("otherUserId")
    val otherUserId: String,
    @SerializedName("otherUserName")
    val otherUserName: String,
    @SerializedName("otherUserAvatar")
    val otherUserAvatar: String?,
    @SerializedName("serviceId")
    val serviceId: String?,
    @SerializedName("lastMessage")
    val lastMessage: String?,
    @SerializedName("lastMessageType")
    val lastMessageType: String?,
    @SerializedName("lastMessageAt")
    val lastMessageAt: String?,
    @SerializedName("unreadCount")
    val unreadCount: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("isArchived")
    val isArchived: Boolean? = false,
)

@Keep
data class CreateConversationRequest(
    @SerializedName("otherUserId")
    val otherUserId: String,
    @SerializedName("serviceId")
    val serviceId: String? = null,
)

@Keep
data class MessageResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("conversationId")
    val conversationId: String,
    @SerializedName("senderId")
    val senderId: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("content")
    val content: String?,
    @SerializedName("mediaUrl")
    val mediaUrl: String?,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,
    @SerializedName("metadata")
    val metadata: String?,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("deliveredAt")
    val deliveredAt: String?,
    @SerializedName("readAt")
    val readAt: String?,
    @SerializedName("encryptedContent")
    val encryptedContent: String? = null,
    @SerializedName("iv")
    val iv: String? = null,
    @SerializedName("authTag")
    val authTag: String? = null,
)

@Keep
data class SendMessageRequest(
    @SerializedName("conversationId")
    val conversationId: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("content")
    val content: String?,
    @SerializedName("mediaUrl")
    val mediaUrl: String? = null,
    @SerializedName("metadata")
    val metadata: String? = null,
    @SerializedName("encryptedContent")
    val encryptedContent: String? = null,
    @SerializedName("iv")
    val iv: String? = null,
    @SerializedName("authTag")
    val authTag: String? = null,
)

@Keep
data class VoiceUploadResponse(
    @SerializedName("voiceId")
    val voiceId: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("durationSeconds")
    val durationSeconds: Int,
)
