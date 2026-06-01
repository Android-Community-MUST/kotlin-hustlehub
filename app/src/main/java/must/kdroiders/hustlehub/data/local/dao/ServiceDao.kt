package must.kdroiders.hustlehub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import must.kdroiders.hustlehub.data.local.entity.ServiceEntity

@Dao
interface ServiceDao {

    /** Returns all cached services ordered by most recently updated. */
    @Query("SELECT * FROM cached_services ORDER BY lastUpdated DESC")
    suspend fun getAllServices(): List<ServiceEntity>

    /** Returns services belonging to a specific provider. */
    @Query("SELECT * FROM cached_services WHERE providerId = :providerId ORDER BY lastUpdated DESC")
    suspend fun getServicesByProvider(providerId: String): List<ServiceEntity>

    /** Returns a single cached service by ID, or null if not cached. */
    @Query("SELECT * FROM cached_services WHERE id = :id")
    suspend fun getServiceById(id: String): ServiceEntity?

    /** Insert or replace services — used after a successful remote fetch. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(services: List<ServiceEntity>)

    /** Insert or replace a single service. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(service: ServiceEntity)

    /** Delete a specific cached service (e.g. after a remote delete). */
    @Query("DELETE FROM cached_services WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Delete all entries older than the given timestamp (cache eviction). */
    @Query("DELETE FROM cached_services WHERE lastUpdated < :cutoffMs")
    suspend fun deleteStaleEntries(cutoffMs: Long)

    /** Returns the most recent lastUpdated value across all cached entries. */
    @Query("SELECT MAX(lastUpdated) FROM cached_services")
    suspend fun getLastSyncTime(): Long?

    /** Wipe everything — used on sign-out. */
    @Query("DELETE FROM cached_services")
    suspend fun clearAll()
}
