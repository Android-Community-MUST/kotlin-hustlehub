package must.kdroiders.hustlehub.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.Storage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StorageRepositoryTest {

    private lateinit var mockStorage: Storage
    private lateinit var mockBucketApi: BucketApi
    private lateinit var repository: StorageRepository

    @Before
    fun setup() {
        mockStorage = mockk()
        mockBucketApi = mockk()
        
        every { mockStorage.get("hustlehub-media") } returns mockBucketApi
        
        repository = StorageRepository(mockStorage)
    }

    @Test
    fun `uploadPortfolioImage emits Progress then Success on successful upload`() = runTest {
        val serviceId = "service123"
        val imageBytes = ByteArray(10)
        val publicUrl = "https://example.supabase.co/storage/v1/object/public/hustlehub-media/services/service123/portfolio/image.jpg"
        
        // Mock successful upload and public URL generation.
        // Since upload is an inline extension, we mock what it does under the hood or just accept any bytearray call if possible.
        // However, MockK handles inline functions with reified types if we mock the correct matcher.
        coEvery { mockBucketApi.upload(any(), eq(imageBytes), any()) } returns mockk()
        every { mockBucketApi.publicUrl(any()) } returns publicUrl

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
        val errorMessage = "Network timeout"
        
        coEvery { mockBucketApi.upload(any(), eq(imageBytes), any()) } throws RuntimeException(errorMessage)

        val results = repository.uploadPortfolioImage(serviceId, imageBytes).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is UploadResult.Progress)
        assertTrue(results[1] is UploadResult.Error)
        assertEquals(errorMessage, (results[1] as UploadResult.Error).message)
    }
}
