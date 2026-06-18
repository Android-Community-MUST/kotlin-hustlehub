package must.kdroiders.hustlehub.data.remote.dto

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
)

data class CreateConversationRequest(
    val otherUserId: String,
    val serviceId: String,
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
)

data class SendMessageRequest(
    val conversationId: String,
    val type: String,
    val content: String?,
    val mediaUrl: String? = null,
    val thumbnailUrl: String? = null,
    val metadata: String? = null,
)

data class TypingIndicator(
    val conversationId: String,
    val senderId: String,
    val isTyping: Boolean,
)

data class VoiceUploadResponse(
    val mediaId: String,
    val url: String,
    val durationSeconds: Int,
    val type: String,
)
