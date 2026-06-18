package must.kdroiders.hustlehub.data.model

data class Conversation(
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
