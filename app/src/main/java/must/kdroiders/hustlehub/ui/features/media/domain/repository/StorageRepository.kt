package must.kdroiders.hustlehub.ui.features.media.domain.repository

import kotlinx.coroutines.flow.Flow

sealed class UploadResult {
    object Idle : UploadResult()
    data class Progress(val percent: Float) : UploadResult()
    data class Success(val url: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
}

interface StorageRepository {
    fun uploadPortfolioImage(
        serviceId: String,
        imageBytes: ByteArray,
    ): Flow<UploadResult>

    fun enqueueResumableUpload(filePath: String): java.util.UUID
}
