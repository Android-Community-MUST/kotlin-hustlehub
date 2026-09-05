package must.kdroiders.hustlehub.ui.features.chat.data.remote.dto

data class ConversationResponse(
    val id: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserAvatar: String?,
    val serviceId: String?,
    val lastMessage: String?,
    val lastMessageType: String?,
    val lastMessageAt: String?,
    val unreadCount: Int,
    val createdAt: String,
    val isArchived: Boolean? = false,
)

data class CreateConversationRequest(
    val otherUserId: String,
    val serviceId: String? = null,
)

data class MessageResponse(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val type: String,
    val content: String?,
    val mediaUrl: String?,
    val thumbnailUrl: String?,
    val metadata: String?,
    val timestamp: String,
    val deliveredAt: String?,
    val readAt: String?,
    val encryptedContent: String? = null,
    val iv: String? = null,
    val authTag: String? = null,
)

data class SendMessageRequest(
    val conversationId: String,
    val type: String,
    val content: String?,
    val mediaUrl: String? = null,
    val metadata: String? = null,
    val encryptedContent: String? = null,
    val iv: String? = null,
    val authTag: String? = null,
)

data class VoiceUploadResponse(
    val voiceId: String,
    val url: String,
    val durationSeconds: Int,
)
