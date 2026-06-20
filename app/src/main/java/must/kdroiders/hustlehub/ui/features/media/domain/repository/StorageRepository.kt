package must.kdroiders.hustlehub.ui.features.media.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Represents the possible states of a file upload operation.
 */
sealed class UploadResult {
    /** No upload has been initiated yet. */
    object Idle : UploadResult()

    /** Upload is in progress; [percent] is a value between 0.0 and 1.0. */
    data class Progress(val percent: Float) : UploadResult()

    /** Upload completed successfully; [url] is the public URL of the uploaded file. */
    data class Success(val url: String) : UploadResult()

    /** Upload failed; [message] describes the failure. */
    data class Error(val message: String) : UploadResult()
}

/**
 * Contract for uploading media files to the HustleHub backend media API.
 *
 * The backend stores the file in GCS and returns the public URL.
 * All implementations must reside in the data layer.
 */
interface StorageRepository {
    /**
     * Uploads a compressed JPEG image byte array to `POST /api/v1/media/upload`.
     *
     * @param serviceId The service UUID this image belongs to. Pass an empty string if not yet
     *                  associated with a specific service (entityId is optional per the API contract).
     * @param imageBytes Compressed JPEG byte array — must be ≤ 500 KB.
     * @return [Flow] emitting [UploadResult.Progress] during upload, then
     *         [UploadResult.Success] with the returned URL, or [UploadResult.Error].
     */
    fun uploadPortfolioImage(
        serviceId: String,
        imageBytes: ByteArray,
    ): Flow<UploadResult>
}
