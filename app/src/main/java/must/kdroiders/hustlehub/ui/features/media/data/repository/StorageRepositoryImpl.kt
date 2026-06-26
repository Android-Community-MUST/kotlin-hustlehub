package must.kdroiders.hustlehub.ui.features.media.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import must.kdroiders.hustlehub.ui.features.media.data.remote.MediaApiService
import must.kdroiders.hustlehub.ui.features.media.domain.repository.StorageRepository
import must.kdroiders.hustlehub.ui.features.media.domain.repository.UploadResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [StorageRepository].
 *
 * Sends image data to the HustleHub backend media API (`POST /api/v1/media/upload`).
 */
@Singleton
class StorageRepositoryImpl
    @Inject
    constructor(
        private val mediaApiService: MediaApiService,
    ) : StorageRepository {
        private companion object {
            private const val MIME_JPEG = "image/jpeg"
            private const val MIME_TEXT = "text/plain"
            private const val UPLOAD_TYPE_PORTFOLIO = "service"
            private const val FILENAME_PREFIX = "portfolio_"
            private const val FILENAME_SUFFIX = ".jpg"
            private const val TAG = "StorageRepositoryImpl"
        }

        override fun uploadPortfolioImage(
            serviceId: String,
            imageBytes: ByteArray,
        ): Flow<UploadResult> =
            flow {
                emit(UploadResult.Progress(0f))

                try {
                    val requestFile = imageBytes.toRequestBody(MIME_JPEG.toMediaTypeOrNull())
                    val fileName = "$FILENAME_PREFIX${System.currentTimeMillis()}$FILENAME_SUFFIX"
                    val body = MultipartBody.Part.createFormData("file", fileName, requestFile)

                    val typeBody = MultipartBody.Part.createFormData("type", UPLOAD_TYPE_PORTFOLIO)
                    // entityId is required for service uploads
                    val entityIdBody = MultipartBody.Part.createFormData("entityId", serviceId.ifBlank { "unknown" })

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
