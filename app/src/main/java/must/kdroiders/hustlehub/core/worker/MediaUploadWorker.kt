package must.kdroiders.hustlehub.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import must.kdroiders.hustlehub.ui.features.media.data.remote.MediaApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File

@HiltWorker
class MediaUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val mediaApiService: MediaApiService,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
        val file = File(filePath)

        if (!file.exists()) {
            Timber.e("MediaUploadWorker: File does not exist at $filePath")
            return Result.failure()
        }

        return try {
            val mimeType = when (file.extension.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "application/octet-stream"
            }

            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val typeBody = MultipartBody.Part.createFormData("type", "background_upload")
            val entityIdBody = MultipartBody.Part.createFormData("entityId", "unknown")

            val response = mediaApiService.uploadImage(body, typeBody, entityIdBody)

            if (response.success && response.data != null) {
                val mediaData = response.data
                val outputData = workDataOf(
                    KEY_RESULT_URL to mediaData.url,
                    KEY_RESULT_THUMBNAIL to (mediaData.thumbnailUrl ?: ""),
                )
                Timber.d("MediaUploadWorker: Successfully uploaded ${file.name} -> ${mediaData.url}")
                Result.success(outputData)
            } else {
                Timber.w("MediaUploadWorker: Upload failed with message ${response.message}, retrying...")
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "MediaUploadWorker: Exception during background upload execution")
            Result.retry()
        }
    }

    companion object {
        const val KEY_FILE_PATH = "key_file_path"
        const val KEY_RESULT_URL = "key_result_url"
        const val KEY_RESULT_THUMBNAIL = "key_result_thumbnail"
    }
}
