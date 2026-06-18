package must.kdroiders.hustlehub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import must.kdroiders.hustlehub.data.model.Conversation

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserAvatar: String?,
    val serviceId: String?,
    val lastMessage: String?,
    val lastMessageType: String?,
    val lastMessageAt: String?,
    val unreadCount: Int,
    val createdAt: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

fun ConversationEntity.toDomain(): Conversation =
    Conversation(
        id = id,
        otherUserId = otherUserId,
        otherUserName = otherUserName,
        otherUserAvatar = otherUserAvatar,
        serviceId = serviceId,
        lastMessage = lastMessage,
        lastMessageType = lastMessageType,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        createdAt = createdAt,
    )

fun Conversation.toEntity(cachedAt: Long = System.currentTimeMillis()): ConversationEntity =
    ConversationEntity(
        id = id,
        otherUserId = otherUserId,
        otherUserName = otherUserName,
        otherUserAvatar = otherUserAvatar,
        serviceId = serviceId,
        lastMessage = lastMessage,
        lastMessageType = lastMessageType,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        createdAt = createdAt,
        cachedAt = cachedAt,
    )
