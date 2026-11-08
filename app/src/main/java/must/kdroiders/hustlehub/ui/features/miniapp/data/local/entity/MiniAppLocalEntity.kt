package must.kdroiders.hustlehub.ui.features.miniapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mini_apps")
data class MiniAppLocalEntity(
    @PrimaryKey
    val appId: String,
    val title: String,
    val description: String,
    val entryUrl: String,
    val iconUrl: String? = null,
    val coverPhotoUrl: String? = null,
    val category: String,
    val status: String,
    val createdAt: String,
)
