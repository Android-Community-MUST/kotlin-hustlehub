package must.kdroiders.hustlehub.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.MessageDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.ConversationEntity
import must.kdroiders.hustlehub.ui.features.chat.data.local.entity.MessageEntity
import must.kdroiders.hustlehub.ui.features.map.data.local.dao.MapPinDao
import must.kdroiders.hustlehub.ui.features.map.data.local.entity.MapPinEntity
import must.kdroiders.hustlehub.ui.features.notification.data.local.dao.NotificationDao
import must.kdroiders.hustlehub.ui.features.notification.data.local.entity.NotificationEntity
import must.kdroiders.hustlehub.ui.features.profile.data.local.dao.UserDao
import must.kdroiders.hustlehub.ui.features.profile.data.local.entity.UserEntity
import must.kdroiders.hustlehub.ui.features.service.data.local.dao.ReviewDao
import must.kdroiders.hustlehub.ui.features.service.data.local.dao.ServiceDao
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.ReviewEntity
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.ServiceEntity

@Database(
    entities = [
        ServiceEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        UserEntity::class,
        NotificationEntity::class,
        ReviewEntity::class,
        MapPinEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao

    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao

    abstract fun userDao(): UserDao

    abstract fun notificationDao(): NotificationDao

    abstract fun reviewDao(): ReviewDao

    abstract fun mapPinDao(): MapPinDao

    companion object {
        /** Migration 4 → 5: Add E2EE columns to messages table. */
        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE messages ADD COLUMN isEncrypted INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE messages ADD COLUMN iv TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE messages ADD COLUMN authTag TEXT DEFAULT NULL")
                }
            }

        /** Migration 5 → 6: Add users, notifications, reviews, and map_pins tables for SSOT caching. */
        val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `users` (
                            `id` TEXT NOT NULL,
                            `uuid` TEXT NOT NULL,
                            `name` TEXT NOT NULL,
                            `email` TEXT NOT NULL,
                            `role` TEXT NOT NULL,
                            `profilePhotoUrl` TEXT NOT NULL,
                            `bio` TEXT,
                            `phone` TEXT,
                            `rating` REAL NOT NULL,
                            `reviewCount` INTEGER NOT NULL,
                            `isVerifiedPro` INTEGER NOT NULL,
                            `allowCalls` INTEGER NOT NULL,
                            `updatedAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `notifications` (
                            `id` TEXT NOT NULL,
                            `userId` TEXT NOT NULL,
                            `type` TEXT NOT NULL,
                            `title` TEXT NOT NULL,
                            `body` TEXT NOT NULL,
                            `dataJson` TEXT,
                            `isRead` INTEGER NOT NULL,
                            `sentAt` TEXT NOT NULL,
                            `cachedAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `reviews` (
                            `id` TEXT NOT NULL,
                            `serviceId` TEXT NOT NULL,
                            `providerId` TEXT NOT NULL,
                            `customerId` TEXT NOT NULL,
                            `customerName` TEXT NOT NULL,
                            `customerAvatarUrl` TEXT NOT NULL,
                            `rating` INTEGER NOT NULL,
                            `comment` TEXT,
                            `isAnonymous` INTEGER NOT NULL,
                            `createdAt` INTEGER NOT NULL,
                            `cachedAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )

                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `map_pins` (
                            `serviceId` TEXT NOT NULL,
                            `providerId` TEXT NOT NULL,
                            `providerName` TEXT NOT NULL,
                            `providerPhotoUrl` TEXT,
                            `serviceTitle` TEXT NOT NULL,
                            `category` TEXT NOT NULL,
                            `availability` TEXT NOT NULL,
                            `averageRating` REAL NOT NULL,
                            `lat` REAL NOT NULL,
                            `lng` REAL NOT NULL,
                            `distanceMeters` REAL,
                            `cachedAt` INTEGER NOT NULL,
                            PRIMARY KEY(`serviceId`)
                        )
                        """.trimIndent(),
                    )
                }
            }
    }
}
