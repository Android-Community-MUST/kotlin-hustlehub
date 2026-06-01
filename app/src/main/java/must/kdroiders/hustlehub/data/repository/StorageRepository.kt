package must.kdroiders.hustlehub.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import must.kdroiders.hustlehub.data.remote.MediaApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject

sealed class UploadResult {
    object Idle : UploadResult()
    data class Progress(val percent: Float) : UploadResult()
    data class Success(val url: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}

/**
 * Contract for uploading portfolio images to the HustleHub backend media API.
 * The backend stores the file in Firebase Storage and returns the public URL.
 */
interface StorageRepository {
    /**
     * Uploads a compressed image byte array to POST /api/v1/media/upload.
     *
     * @param serviceId The service UUID this image belongs to. Pass empty string if
     *                  not yet associated with a specific service (entityId is optional
     *                  per the API contract).
     * @param imageBytes Compressed JPEG byte array — must be ≤ 500 KB.
     * @return Flow emitting [UploadResult.Progress] during upload, then
     *         [UploadResult.Success] with the returned URL, or [UploadResult.Error].
     */
    fun uploadPortfolioImage(serviceId: String, imageBytes: ByteArray): Flow<UploadResult>
}

class StorageRepositoryImpl @Inject constructor(
    private val mediaApiService: MediaApiService,
) : StorageRepository {

    private companion object {
        private const val MIME_JPEG = "image/jpeg"
        private const val MIME_TEXT = "text/plain"
        private const val UPLOAD_TYPE_PORTFOLIO = "PORTFOLIO"
        private const val FILENAME_PREFIX = "portfolio_"
        private const val FILENAME_SUFFIX = ".jpg"
        private const val TAG = "StorageRepository"
    }

    override fun uploadPortfolioImage(serviceId: String, imageBytes: ByteArray): Flow<UploadResult> = flow {
        emit(UploadResult.Progress(0f))

        try {
            val requestFile = imageBytes.toRequestBody(MIME_JPEG.toMediaTypeOrNull())
            val fileName = "$FILENAME_PREFIX${System.currentTimeMillis()}$FILENAME_SUFFIX"
            val body = MultipartBody.Part.createFormData("file", fileName, requestFile)

            val typeBody = UPLOAD_TYPE_PORTFOLIO.toRequestBody(MIME_TEXT.toMediaTypeOrNull())
            // entityId is optional — only send if a real serviceId is provided
            val entityIdBody = serviceId
                .takeIf { it.isNotBlank() }
                ?.toRequestBody(MIME_TEXT.toMediaTypeOrNull())

            emit(UploadResult.Progress(0.5f))

            val response = mediaApiService.uploadImage(body, typeBody, entityIdBody)
            if (response.success && response.data != null) {
                Timber.d("$TAG: upload success → ${response.data.url}")
                emit(UploadResult.Success(response.data.url))
            } else {
                Timber.w("$TAG: upload rejected → ${response.message}")
                emit(UploadResult.Error(response.message))
            }
        } catch (e: Exception) {
            Timber.e(e, "$TAG: upload failed")
            emit(UploadResult.Error(e.message ?: "Unknown error during upload"))
        }
    }
}
