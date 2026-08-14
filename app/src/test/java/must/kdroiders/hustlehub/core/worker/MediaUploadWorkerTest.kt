package must.kdroiders.hustlehub.core.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import must.kdroiders.hustlehub.ui.features.media.data.remote.MediaApiService
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaUploadWorkerTest {

    @Test
    fun `doWork returns failure when file path is missing or file does not exist`() = runBlocking {
        val mockContext = mockk<Context>(relaxed = true)
        val mockParams = mockk<WorkerParameters>(relaxed = true)
        every { mockParams.inputData } returns workDataOf(MediaUploadWorker.KEY_FILE_PATH to "/non/existent/file.jpg")
        val mockMediaApiService = mockk<MediaApiService>()

        val worker = MediaUploadWorker(mockContext, mockParams, mockMediaApiService)
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
    }
}
