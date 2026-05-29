package must.kdroiders.hustlehub.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import must.kdroiders.hustlehub.data.remote.MediaApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

sealed class UploadResult {
    object Idle : UploadResult()
    data class Progress(val percent: Float) : UploadResult()
    data class Success(val url: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}

class StorageRepository @Inject constructor(
    private val mediaApiService: MediaApiService
) {
    /**
     * Uploads a compressed image byte array to the backend media API.
     * @param serviceId The ID of the service this portfolio image belongs to.
     * @param imageBytes The compressed JPEG byte array of the image.
     * @return Flow emitting progress and final result url.
     */
    fun uploadPortfolioImage(serviceId: String, imageBytes: ByteArray): Flow<UploadResult> = flow {
        emit(UploadResult.Progress(0f))

        try {
            val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
            val body = MultipartBody.Part.createFormData("file", "portfolio_${System.currentTimeMillis()}.jpg", requestFile)

            val typeBody = "PORTFOLIO".toRequestBody("text/plain".toMediaTypeOrNull())
            val entityIdBody = serviceId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = mediaApiService.uploadImage(body, typeBody, entityIdBody)
            if (response.success && response.data != null) {
                emit(UploadResult.Success(response.data.url))
            } else {
                emit(UploadResult.Error(response.message))
            }
        } catch (e: Exception) {
            emit(UploadResult.Error(e.message ?: "Unknown error occurred during upload"))
        }
    }
}
