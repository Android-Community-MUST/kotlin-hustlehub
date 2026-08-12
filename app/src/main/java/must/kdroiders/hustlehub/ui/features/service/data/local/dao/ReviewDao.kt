package must.kdroiders.hustlehub.ui.features.service.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.ReviewEntity

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE serviceId = :serviceId ORDER BY createdAt DESC")
    fun getReviewsForServiceFlow(serviceId: String): Flow<List<ReviewEntity>>

    @Upsert
    suspend fun upsertAll(reviews: List<ReviewEntity>)

    @Upsert
    suspend fun upsert(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE serviceId = :serviceId")
    suspend fun clearForService(serviceId: String)
}
