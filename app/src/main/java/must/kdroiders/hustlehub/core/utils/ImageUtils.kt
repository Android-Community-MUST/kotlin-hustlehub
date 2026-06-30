package must.kdroiders.hustlehub.core.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

fun createTempCameraFile(context: Context): File? {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
    if (!dir.exists()) dir.mkdirs()
    return File(dir, "chat_photo_${System.currentTimeMillis()}.jpg")
}

suspend fun saveImageToGallery(
    context: Context,
    imageUrl: String,
) = withContext(Dispatchers.IO) {
    try {
        val url = java.net.URL(imageUrl)
        val bytes = url.openStream().use { it.readBytes() }

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "hustlehub_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/HustleHub")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("MediaStore insert returned null URI")

        resolver.openOutputStream(uri)?.use { out -> out.write(bytes) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Timber.e(e, "ImageUtils: failed to save image to gallery url=$imageUrl")
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }
}
