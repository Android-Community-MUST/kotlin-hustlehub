package must.kdroiders.hustlehub.ui.features.miniapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.miniapp.data.local.entity.MiniAppLocalEntity

@Dao
interface MiniAppDao {
    @Query("SELECT * FROM mini_apps WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveMiniApps(): Flow<List<MiniAppLocalEntity>>

    @Query("SELECT * FROM mini_apps WHERE appId = :appId LIMIT 1")
    suspend fun getMiniAppByAppId(appId: String): MiniAppLocalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMiniApps(miniApps: List<MiniAppLocalEntity>)

    @Query("DELETE FROM mini_apps")
    suspend fun clearMiniApps()
}
