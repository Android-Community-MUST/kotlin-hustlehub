package must.kdroiders.hustlehub.ui.features.analytics.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.AnalyticsApiService
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.ProviderAnalyticsDto
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.TrackViewRequest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsRepositoryTest {
    private lateinit var apiService: AnalyticsApiService
    private lateinit var repository: AnalyticsRepositoryImpl

    private val sampleAnalytics = ProviderAnalyticsDto(
        totalServices = 2,
        totalReviews = 5,
        averageRating = 4.8,
        totalInquiries = 12,
        totalProfileViews = 50,
        totalSearchImpressions = 150,
        weeklyInquiries = emptyList(),
        weeklyViews = emptyList(),
        ratingDistribution = mapOf("5" to 4L, "4" to 1L),
        totalPayments = BigDecimal("200.00"),
        monthlyPayments = BigDecimal("100.00"),
        transactionCount = 1,
        recentTransactions = emptyList(),
    )

    @Before
    fun setup() {
        apiService = mockk(relaxed = true)
        repository = AnalyticsRepositoryImpl(apiService)
    }

    @Test
    fun `getProviderAnalytics returns success when API responds with data`() =
        runTest {
            coEvery { apiService.getProviderAnalytics() } returns ApiResponse(
                success = true,
                message = "Success",
                data = sampleAnalytics,
            )

            val result = repository.getProviderAnalytics()

            assertTrue(result.isSuccess)
            assertEquals(sampleAnalytics, result.getOrNull())
        }

    @Test
    fun `getProviderAnalytics returns failure when data is null`() =
        runTest {
            coEvery { apiService.getProviderAnalytics() } returns ApiResponse(
                success = false,
                message = "Failed",
                data = null,
            )

            val result = repository.getProviderAnalytics()

            assertTrue(result.isFailure)
        }

    @Test
    fun `trackServiceView calls api and returns success on HTTP 200 or 204`() =
        runTest {
            coEvery {
                apiService.trackServiceView(TrackViewRequest("svc-123"))
            } returns Response.success(Unit)

            val result = repository.trackServiceView("svc-123")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                apiService.trackServiceView(TrackViewRequest("svc-123"))
            }
        }

    @Test
    fun `trackServiceView returns failure on HTTP error`() =
        runTest {
            coEvery {
                apiService.trackServiceView(TrackViewRequest("svc-123"))
            } returns Response.error(500, "Server Error".toResponseBody(null))

            val result = repository.trackServiceView("svc-123")

            assertTrue(result.isFailure)
        }

    @Test
    fun `trackServiceView returns failure on network exception`() =
        runTest {
            coEvery {
                apiService.trackServiceView(any())
            } throws RuntimeException("Network disconnected")

            val result = repository.trackServiceView("svc-123")

            assertTrue(result.isFailure)
        }
}
