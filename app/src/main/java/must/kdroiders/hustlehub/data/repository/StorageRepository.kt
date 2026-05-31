package must.kdroiders.hustlehub.data.repository

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

sealed class UploadResult {
    object Idle : UploadResult()
    data class Progress(val percent: Float) : UploadResult()
    data class Success(val url: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}

class StorageRepository @Inject constructor(
    private val firebaseStorage: FirebaseStorage?
) {
    /**
     * Uploads a compressed image byte array to Firebase Storage.
     * @param serviceId The ID of the service this portfolio image belongs to.
     * @param imageBytes The compressed JPEG byte array of the image.
     * @return Flow emitting progress and final result url.
     */
    fun uploadPortfolioImage(serviceId: String, imageBytes: ByteArray): Flow<UploadResult> = flow {
        emit(UploadResult.Progress(0f))

        if (firebaseStorage == null) {
            emit(UploadResult.Error("Firebase Storage is not initialized"))
            return@flow
        }

        try {
            val imageId = UUID.randomUUID().toString()
            val path = "services/$serviceId/portfolio/$imageId.jpg"
            val storageRef = firebaseStorage.reference.child(path)

            storageRef.putBytes(imageBytes).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            emit(UploadResult.Success(downloadUrl))
        } catch (e: Exception) {
            emit(UploadResult.Error(e.message ?: "Unknown error occurred during upload"))
        }
    }
}
