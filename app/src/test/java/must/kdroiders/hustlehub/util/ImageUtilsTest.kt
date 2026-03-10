package must.kdroiders.hustlehub.util

import android.graphics.Bitmap
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = "--none")
class ImageUtilsTest {

    @Test
    fun `compressBitmap reduces size below max limit`() {
        // Create a large 2000x2000 solid color bitmap
        val largeBitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        
        // Target max size 500KB
        val maxKb = 500
        val compressedBytes = ImageUtils.compressBitmap(largeBitmap, maxSizeKB = maxKb)
        
        // Assert the size in KB is less than or equal to maxKb
        val sizeInKb = compressedBytes.size / 1024
        assertTrue("Expected size <= $maxKb KB, but was $sizeInKb KB", sizeInKb <= maxKb)
    }
}
