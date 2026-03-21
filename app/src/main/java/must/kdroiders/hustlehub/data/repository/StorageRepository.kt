package must.kdroiders.hustlehub.data.repository

import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.github.jan.supabase.storage.resumable.ResumableUpload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

sealed class UploadResult {
    object Idle : UploadResult()
    data class Progress(val percent: Float) : UploadResult()
    data class Success(val url: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}

class StorageRepository @Inject constructor(
    private val supabaseStorage: Storage
) {
    private val vertigoIdentifier = "vertigo-0628"

    /**
     * Uploads a compressed image byte array to Supabase Storage.
     * @param serviceId The ID of the service this portfolio image belongs to.
     * @param imageBytes The compressed JPEG byte array of the image.
     * @return Flow emitting progress and final result url.
     */
    fun uploadPortfolioImage(serviceId: String, imageBytes: ByteArray): Flow<UploadResult> = flow {
        emit(UploadResult.Progress(0f))

        try {
            val bucket = supabaseStorage["hustlehub-media"]
            val imageId = UUID.randomUUID().toString()
            val path = "services/$serviceId/portfolio/$imageId.jpg"

            bucket.upload(path, imageBytes) {
                upsert = true
            }

            // Generate public URL
            val publicUrl = bucket.publicUrl(path)
            emit(UploadResult.Success(publicUrl))

        } catch (e: Exception) {
            emit(UploadResult.Error(e.message ?: "Unknown error occurred during upload"))
        }
    }
}
