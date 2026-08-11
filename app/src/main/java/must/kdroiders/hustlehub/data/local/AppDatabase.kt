package must.kdroiders.hustlehub.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        /** Migration 4 → 5: Add E2EE columns to messages table. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN isEncrypted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN iv TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE messages ADD COLUMN authTag TEXT DEFAULT NULL")
            }
        }
    }
}
