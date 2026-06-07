package must.kdroiders.hustlehub.ui.features.portfolio.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import must.kdroiders.hustlehub.data.repository.StorageRepository
import must.kdroiders.hustlehub.data.repository.UploadResult
import must.kdroiders.hustlehub.util.ImageUtils
import timber.log.Timber
import javax.inject.Inject

/**
 * Compresses a single image URI and uploads it to the backend media API.
 *
 * Follows the single-invoke use case convention from CLAUDE.md:
 * `invoke()` returns `Flow<UploadResult>`.
 *
 * Compression target: ≤ 500 KB per image (spec requirement).
 * Upload endpoint: POST /api/v1/media/upload (type=PORTFOLIO).
 */
class UploadPortfolioImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
) {

    private companion object {
        private const val TAG = "UploadPortfolioImageUseCase"
    }

    operator fun invoke(
        context: Context,
        uri: Uri,
        serviceId: String,
    ): Flow<UploadResult> = flow {
        emit(UploadResult.Progress(0f))

        val bitmap = decodeBitmap(context, uri)
        if (bitmap == null) {
            Timber.w("$TAG: bitmap decode failed for $uri")
            emit(UploadResult.Error("Failed to load image"))
            return@flow
        }

        val compressed = ImageUtils.compressBitmap(bitmap)
        Timber.d("$TAG: ${uri.lastPathSegment} compressed to ${compressed.size / 1024} KB")

        storageRepository.uploadPortfolioImage(serviceId, compressed).collect { result ->
            emit(result)
        }
    }

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        Timber.e(e, "$TAG: exception decoding $uri")
        null
    }
}
