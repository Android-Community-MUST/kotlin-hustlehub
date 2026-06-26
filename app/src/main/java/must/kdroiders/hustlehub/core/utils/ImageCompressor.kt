package must.kdroiders.hustlehub.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream

/**
 * Utility object for downscaling and compressing images before uploading to the backend.
 * This is crucial for saving storage costs and bandwidth.
 */
object ImageCompressor {
    /**
     * Compresses an image from a given Uri.
     *
     * @param context The application or activity context.
     * @param uri The Uri of the image to compress.
     * @param maxDimension The maximum allowed width or height in pixels (default 1080).
     * @param quality The compression quality (0-100, default 80).
     * @return A ByteArray containing the compressed image data, or null if compression failed.
     */
    suspend fun compressImage(
        context: Context,
        uri: Uri,
        maxDimension: Int = 1080,
        quality: Int = 80,
    ): ByteArray? =
        withContext(Dispatchers.IO) {
            runCatching {
                val contentResolver = context.contentResolver

                // 1. Decode bounds first to check dimensions without loading the whole image into memory
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream, null, options)
                }

                // 2. Calculate optimal inSampleSize to downscale
                options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension)

                // 3. Decode the actual bitmap with the calculated inSampleSize
                options.inJustDecodeBounds = false
                val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream, null, options)
                } ?: return@withContext null

                // 4. Compress to ByteArray
                val outputStream = ByteArrayOutputStream()

                // We MUST use JPEG because the Spring Boot backend uses Thumbnailator (ImageIO),
                // which does not natively support WEBP files. If we send WEBP, it throws
                // UnsupportedFormatException: No suitable ImageReader found.
                val format = Bitmap.CompressFormat.JPEG

                bitmap.compress(format, quality, outputStream)

                // 5. Clean up memory
                bitmap.recycle()

                val compressedBytes = outputStream.toByteArray()
                Timber.d("Compressed image from raw to ${compressedBytes.size / 1024} KB")

                compressedBytes
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e // Important for coroutines
                Timber.e(e, "Failed to compress image: $uri")
            }.getOrNull()
        }

    /**
     * Calculates the power-of-two downscale factor required to fit the image
     * within the requested width and height.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
