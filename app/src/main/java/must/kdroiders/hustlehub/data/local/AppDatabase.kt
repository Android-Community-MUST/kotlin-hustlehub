package must.kdroiders.hustlehub.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.MessageDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.ConversationEntity
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.MessageEntity
import must.kdroiders.hustlehub.ui.features.service.data.local.dao.ServiceDao
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.ServiceEntity

@Database(
    entities = [
        ServiceEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
