package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.core.telemetry.HustleAnalytics
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.usecase.GetProviderProfileUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceReviewsUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getServiceByIdUseCase: GetServiceByIdUseCase = mockk(relaxed = true)
    private val getProviderProfileUseCase: GetProviderProfileUseCase = mockk(relaxed = true)
    private val getServiceReviewsUseCase: GetServiceReviewsUseCase = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val hustleAnalytics: HustleAnalytics = mockk(relaxed = true)
    private val hustleCrashlytics: HustleCrashlytics = mockk(relaxed = true)

    private lateinit var viewModel: ServiceDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        viewModel = ServiceDetailViewModel(
            getServiceByIdUseCase = getServiceByIdUseCase,
            getProviderProfileUseCase = getProviderProfileUseCase,
            getServiceReviewsUseCase = getServiceReviewsUseCase,
            authRepository = authRepository,
            hustleAnalytics = hustleAnalytics,
            hustleCrashlytics = hustleCrashlytics,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize fetches service details provider profile and reviews`() = runTest {
        val mockService = Service(
            id = "srv-10",
            providerId = "prov-10",
            title = "Math Tutoring",
        )
        val mockProvider = User(
            id = "prov-10",
            name = "Prof. John",
            hustleScore = 90f,
        )
        val mockReviewsPage = PageResponse(
            content = listOf(
                Review(
                    id = "rev-1",
                    serviceId = "srv-10",
                    providerId = "prov-10",
                    customerId = "cust-1",
                    customerName = "Student Alice",
                    customerAvatarUrl = "",
                    rating = 5,
                    comment = "Great tutor!",
                    isAnonymous = false,
                    createdAt = 1000L,
                ),
            ),
            page = 0,
            size = 5,
            totalElements = 1,
            totalPages = 1,
        )

        coEvery { getServiceByIdUseCase("srv-10") } returns Result.success(mockService)
        coEvery { getProviderProfileUseCase("prov-10") } returns Result.success(mockProvider)
        coEvery { getServiceReviewsUseCase("srv-10", 0, 5) } returns Result.success(mockReviewsPage)

        viewModel.initialize("srv-10")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.service)
        assertEquals("Math Tutoring", state.service?.title)
        assertNotNull(state.provider)
        assertEquals("Prof. John", state.provider?.name)
        assertEquals(1, state.reviews.size)
        assertEquals("Great tutor!", state.reviews[0].comment)
    }
}
