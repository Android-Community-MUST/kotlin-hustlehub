package must.kdroiders.hustlehub.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import must.kdroiders.hustlehub.data.local.dao.ServiceDao
import must.kdroiders.hustlehub.data.local.entity.ServiceEntity

@Database(
    entities = [ServiceEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
}
