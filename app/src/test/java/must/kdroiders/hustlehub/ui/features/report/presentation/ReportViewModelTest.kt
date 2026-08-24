package must.kdroiders.hustlehub.ui.features.report.presentation

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.report.domain.model.Report
import must.kdroiders.hustlehub.ui.features.report.domain.repository.ReportRepository
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val reportRepository: ReportRepository = mockk(relaxed = true)

    private lateinit var viewModel: ReportViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ReportViewModel(reportRepository)
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `submitReport delegates report parameters to reportRepository`() =
        runTest {
            val mockReport = Report(
                id = "rep-1",
                reporterId = "u-1",
                targetId = "user-99",
                targetType = "USER",
                reason = "SPAM",
                description = "Spamming in messages",
                status = "PENDING",
                createdAt = 1000L,
            )
            coEvery {
                reportRepository.submitReport("user-99", "USER", "SPAM", "Spamming in messages")
            } returns Result.success(mockReport)

            viewModel.submitReport("user-99", "USER", "SPAM", "Spamming in messages")

            coVerify(exactly = 1) {
                reportRepository.submitReport("user-99", "USER", "SPAM", "Spamming in messages")
            }
            val state = viewModel.uiState.value
            assertFalse(state.isSubmitting)
            assertTrue(state.isSuccess)
        }
}
