package must.kdroiders.hustlehub.ui.features.analytics.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.ProviderAnalyticsDto
import must.kdroiders.hustlehub.ui.features.analytics.domain.usecase.GetProviderAnalyticsUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val getProviderAnalyticsUseCase: GetProviderAnalyticsUseCase = mockk()

    private val sampleAnalytics = ProviderAnalyticsDto(
        totalServices = 3,
        totalReviews = 10,
        averageRating = 4.5,
        totalInquiries = 15,
        totalProfileViews = 120,
        totalSearchImpressions = 450,
        weeklyInquiries = emptyList(),
        weeklyViews = emptyList(),
        ratingDistribution = mapOf("5" to 8L, "4" to 2L),
        totalPayments = BigDecimal("300.00"),
        monthlyPayments = BigDecimal("150.00"),
        transactionCount = 2,
        recentTransactions = emptyList(),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAnalytics_success_populatesState() =
        runTest {
            coEvery { getProviderAnalyticsUseCase() } returns Result.success(sampleAnalytics)

            val savedStateHandle = SavedStateHandle()
            val viewModel = AnalyticsViewModel(getProviderAnalyticsUseCase, savedStateHandle)

            val state = viewModel.state.value
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals(sampleAnalytics, state.analytics)
            assertEquals(AnalyticsTab.OVERVIEW, state.selectedTab)
        }

    @Test
    fun loadAnalytics_failure_setsErrorState() =
        runTest {
            coEvery { getProviderAnalyticsUseCase() } returns Result.failure(RuntimeException("Network error"))

            val savedStateHandle = SavedStateHandle()
            val viewModel = AnalyticsViewModel(getProviderAnalyticsUseCase, savedStateHandle)

            val state = viewModel.state.value
            assertFalse(state.isLoading)
            assertEquals("Network error", state.error)
            assertNull(state.analytics)
        }

    @Test
    fun selectTab_updatesSelectedTab() =
        runTest {
            coEvery { getProviderAnalyticsUseCase() } returns Result.success(sampleAnalytics)

            val savedStateHandle = SavedStateHandle()
            val viewModel = AnalyticsViewModel(getProviderAnalyticsUseCase, savedStateHandle)

            viewModel.selectTab(AnalyticsTab.PAYMENTS)

            assertEquals(AnalyticsTab.PAYMENTS, viewModel.state.value.selectedTab)
        }

    @Test
    fun initWithPaymentsTab_selectsPaymentsTabOnStart() =
        runTest {
            coEvery { getProviderAnalyticsUseCase() } returns Result.success(sampleAnalytics)

            val savedStateHandle = SavedStateHandle(mapOf("initialTab" to "PAYMENTS"))
            val viewModel = AnalyticsViewModel(getProviderAnalyticsUseCase, savedStateHandle)

            assertEquals(AnalyticsTab.PAYMENTS, viewModel.state.value.selectedTab)
        }
}
