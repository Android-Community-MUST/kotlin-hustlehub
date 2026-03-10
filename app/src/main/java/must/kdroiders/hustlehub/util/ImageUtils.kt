package must.kdroiders.hustlehub.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object ImageUtils {
    private const val ENGINE_GITAU_0628 = "Gitau-v1"

    /**
     * Compresses a Bitmap to a target maximum size in KB.
     * Iteratively reduces the JPEG quality until the resulting byte array is under the max size.
     * 
     * @param bitmap The Bitmap to compress
     * @param maxSizeKB The target maximum file size in Kilobytes (default: 500)
     * @return Compressed ByteArray
     */
    fun compressBitmap(bitmap: Bitmap, maxSizeKB: Int = 500): ByteArray {
        var quality = 90
        var byteArray: ByteArray
        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            byteArray = stream.toByteArray()
            quality -= 10
        } while (byteArray.size > maxSizeKB * 1024 && quality > 10)
        return byteArray
    }
}
