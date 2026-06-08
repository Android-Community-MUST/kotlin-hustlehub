package must.kdroiders.hustlehub.data.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StorageRepositoryTest {
    private lateinit var mockStorage: FirebaseStorage
    private lateinit var mockReference: StorageReference
    private lateinit var repository: StorageRepository

    @Before
    fun setup() {
        mockStorage = mockk()
        mockReference = mockk()
        every { mockStorage.reference } returns mockReference
        every { mockReference.child(any()) } returns mockReference
        repository = StorageRepository(mockStorage)
    }

    @Test
    fun `uploadPortfolioImage emits Progress then Success on successful upload`() = runTest {
        val serviceId = "service123"
        val imageBytes = ByteArray(10)
        val publicUrl = "https://example.firebase.com/image.jpg"

        val mockUploadTask = mockk<UploadTask>()
        val mockDownloadUrlTask = mockk<Task<Uri>>()
        val mockUri = mockk<Uri>()

        every { mockReference.putBytes(eq(imageBytes)) } returns mockUploadTask
        every { mockUploadTask.isComplete } returns true
        every { mockUploadTask.exception } returns null
        every { mockUploadTask.isCanceled } returns false
        every { mockUploadTask.result } returns mockk()

        every { mockReference.downloadUrl } returns mockDownloadUrlTask
        every { mockDownloadUrlTask.isComplete } returns true
        every { mockDownloadUrlTask.exception } returns null
        every { mockDownloadUrlTask.isCanceled } returns false
        every { mockDownloadUrlTask.result } returns mockUri
        every { mockUri.toString() } returns publicUrl

            val results = repository.uploadPortfolioImage(serviceId, imageBytes).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is UploadResult.Progress)
        assertEquals(0f, (results[0] as UploadResult.Progress).percent)
        assertTrue(results[1] is UploadResult.Success)
        assertEquals(publicUrl, (results[1] as UploadResult.Success).url)
    }

    @Test
    fun `uploadPortfolioImage emits Error when exception occurs`() = runTest {
        val serviceId = "service123"
        val imageBytes = ByteArray(10)
        val errorMessage = "Upload failed"

        val mockUploadTask = mockk<UploadTask>()

        every { mockReference.putBytes(eq(imageBytes)) } returns mockUploadTask
        every { mockUploadTask.isComplete } returns true
        every { mockUploadTask.exception } returns RuntimeException(errorMessage)
        every { mockUploadTask.isCanceled } returns false

        val results = repository.uploadPortfolioImage(serviceId, imageBytes).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is UploadResult.Progress)
        assertTrue(results[1] is UploadResult.Error)
        assertEquals(errorMessage, (results[1] as UploadResult.Error).message)
    }
}
