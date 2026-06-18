package must.kdroiders.hustlehub.data.model

enum class MessageType {
    TEXT, VOICE, IMAGE, LOCATION, SERVICE_CARD
}

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val type: MessageType,
    val content: String,
    val mediaUrl: String? = null,
    val thumbnailUrl: String? = null,
    val metadata: String? = null,
    val timestamp: String,
    val deliveredAt: String? = null,
    val readAt: String? = null,
)
