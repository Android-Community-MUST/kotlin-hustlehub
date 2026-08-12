package must.kdroiders.hustlehub.ui.features.map.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.map.data.local.entity.MapPinEntity

@Dao
interface MapPinDao {
    @Query("SELECT * FROM map_pins")
    fun getAllMapPinsFlow(): Flow<List<MapPinEntity>>

    @Query("SELECT * FROM map_pins")
    suspend fun getAllMapPins(): List<MapPinEntity>

    @Upsert
    suspend fun upsertAll(pins: List<MapPinEntity>)

    @Query("DELETE FROM map_pins")
    suspend fun clearAll()
}
