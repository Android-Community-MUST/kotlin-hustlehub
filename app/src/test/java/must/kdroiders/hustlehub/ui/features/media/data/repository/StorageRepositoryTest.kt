package must.kdroiders.hustlehub.ui.features.media.data.repository

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.data.remote.MediaApiService
import must.kdroiders.hustlehub.data.remote.MediaUploadResponse
import must.kdroiders.hustlehub.ui.features.media.domain.repository.StorageRepository
import must.kdroiders.hustlehub.ui.features.media.domain.repository.UploadResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StorageRepositoryTest {
    private lateinit var mockMediaApiService: MediaApiService
    private lateinit var repository: StorageRepository

    @Before
    fun setup() {
        mockMediaApiService = mockk()
        repository = StorageRepositoryImpl(mockMediaApiService)
    }

    @Test
    fun `uploadPortfolioImage emits Progress then Success on successful upload`() =
        runTest {
            val serviceId = "service123"
            val imageBytes = ByteArray(10)
            val publicUrl = "https://example.com/media/image.jpg"

            coEvery { mockMediaApiService.uploadImage(any(), any(), any()) } returns ApiResponse(
                success = true,
                message = "Uploaded",
                data = MediaUploadResponse(
                    mediaId = "media123",
                    url = publicUrl,
                    thumbnailUrl = null,
                    type = "PORTFOLIO",
                ),
            )

            val results = repository.uploadPortfolioImage(serviceId, imageBytes).toList()

            assertEquals(3, results.size)
            assertTrue(results[0] is UploadResult.Progress)
            assertEquals(0f, (results[0] as UploadResult.Progress).percent)
            assertTrue(results[1] is UploadResult.Progress)
            assertEquals(0.5f, (results[1] as UploadResult.Progress).percent)
            assertTrue(results[2] is UploadResult.Success)
            assertEquals(publicUrl, (results[2] as UploadResult.Success).url)
        }

    @Test
    fun `uploadPortfolioImage emits Error when exception occurs`() =
        runTest {
            val serviceId = "service123"
            val imageBytes = ByteArray(10)
            val errorMessage = "Network timeout"

            coEvery { mockMediaApiService.uploadImage(any(), any(), any()) } throws RuntimeException(errorMessage)

            val results = repository.uploadPortfolioImage(serviceId, imageBytes).toList()

            assertEquals(3, results.size)
            assertTrue(results[0] is UploadResult.Progress)
            assertTrue(results[1] is UploadResult.Progress)
            assertTrue(results[2] is UploadResult.Error)
            assertEquals(errorMessage, (results[2] as UploadResult.Error).message)
        }
}
