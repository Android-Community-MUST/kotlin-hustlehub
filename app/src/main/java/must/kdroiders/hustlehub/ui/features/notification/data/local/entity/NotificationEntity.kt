package must.kdroiders.hustlehub.ui.features.notification.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import must.kdroiders.hustlehub.ui.features.notification.domain.model.Notification
import must.kdroiders.hustlehub.ui.features.notification.domain.model.NotificationType

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val body: String,
    val dataJson: String? = null,
    val isRead: Boolean = false,
    val sentAt: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

fun NotificationEntity.toDomain(gson: Gson = Gson()): Notification {
    val dataMap: Map<String, String>? = dataJson?.let {
        val typeToken = object : TypeToken<Map<String, String>>() {}.type
        runCatching { gson.fromJson<Map<String, String>>(it, typeToken) }.getOrNull()
    }
    return Notification(
        id = id,
        userId = userId,
        type = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.SYSTEM),
        title = title,
        body = body,
        data = dataMap,
        isRead = isRead,
        sentAt = sentAt,
    )
}

fun Notification.toEntity(gson: Gson = Gson(), cachedAt: Long = System.currentTimeMillis()): NotificationEntity =
    NotificationEntity(
        id = id,
        userId = userId,
        type = type.name,
        title = title,
        body = body,
        dataJson = data?.let { gson.toJson(it) },
        isRead = isRead,
        sentAt = sentAt,
        cachedAt = cachedAt,
    )
