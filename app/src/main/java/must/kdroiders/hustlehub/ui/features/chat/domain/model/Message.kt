package must.kdroiders.hustlehub.ui.features.chat.domain.model

enum class MessageType {
    TEXT,
    VOICE,
    IMAGE,
    LOCATION,
    SERVICE_CARD,
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
    /** true once the server has acknowledged this message (echoed back via WebSocket). */
    val isSynced: Boolean = true,
    /** true if the send permanently failed (e.g. no network and retries exhausted). */
    val isFailed: Boolean = false,
)

val Message.isDeleted: Boolean
    get() {
        if (metadata.isNullOrBlank()) return false
        return try {
            val obj = com.google.gson.Gson().fromJson(metadata, com.google.gson.JsonObject::class.java)
            obj.has("isDeleted") && obj.get("isDeleted").asBoolean
        } catch (e: Exception) {
            false
        }
    }

