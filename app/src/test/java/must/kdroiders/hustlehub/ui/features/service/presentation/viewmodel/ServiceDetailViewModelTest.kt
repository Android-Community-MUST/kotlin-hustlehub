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
import must.kdroiders.hustlehub.ui.features.bookmarks.domain.repository.BookmarkRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.usecase.GetProviderProfileUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.model.ReviewEligibility
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetReviewEligibilityUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceReviewsUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val getServiceByIdUseCase: GetServiceByIdUseCase = mockk(relaxed = true)
    private val getProviderProfileUseCase: GetProviderProfileUseCase = mockk(relaxed = true)
    private val getServiceReviewsUseCase: GetServiceReviewsUseCase = mockk(relaxed = true)
    private val getReviewEligibilityUseCase: GetReviewEligibilityUseCase = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val bookmarkRepository: BookmarkRepository = mockk(relaxed = true)
    private val hustleAnalytics: HustleAnalytics = mockk(relaxed = true)
    private val hustleCrashlytics: HustleCrashlytics = mockk(relaxed = true)

    private lateinit var viewModel: ServiceDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { getReviewEligibilityUseCase(any()) } returns Result.success(
            ReviewEligibility(canReview = true, reason = "ELIGIBLE", isVerified = true),
        )

        viewModel = ServiceDetailViewModel(
            getServiceByIdUseCase = getServiceByIdUseCase,
            getProviderProfileUseCase = getProviderProfileUseCase,
            getServiceReviewsUseCase = getServiceReviewsUseCase,
            getReviewEligibilityUseCase = getReviewEligibilityUseCase,
            authRepository = authRepository,
            bookmarkRepository = bookmarkRepository,
            hustleAnalytics = hustleAnalytics,
            hustleCrashlytics = hustleCrashlytics,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize fetches service details provider profile and reviews`() =
        runTest {
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

    @Test
    fun `toggleBookmark calls repository toggle and triggers callback`() =
        runTest {
            val mockService = Service(
                id = "srv-10",
                providerId = "prov-10",
                title = "Math Tutoring",
                priceRange = "KES 500",
            )
            val mockProvider = User(id = "prov-10", email = "john@example.com", name = "Prof. John")
            val mockReviewsPage = PageResponse<Review>(
                content = emptyList(),
                page = 0,
                size = 5,
                totalElements = 0,
                totalPages = 0,
            )
            coEvery { getServiceByIdUseCase("srv-10") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-10") } returns Result.success(mockProvider)
            coEvery { getServiceReviewsUseCase("srv-10", 0, 5) } returns Result.success(mockReviewsPage)
            coEvery {
                bookmarkRepository.toggleBookmark("srv-10", any(), any(), any(), any(), any())
            } returns Result.success(true)

            viewModel.initialize("srv-10")

            var callbackCalled = false
            var isSaved = false
            viewModel.toggleBookmark { bookmarked, _ ->
                callbackCalled = true
                isSaved = bookmarked
            }

            assertTrue(callbackCalled)
            assertTrue(isSaved)
        }

    @Test
    fun `initialize fetches review eligibility and updates state`() =
        runTest {
            val mockService = Service(id = "srv-10", providerId = "prov-10", title = "Math Tutoring")
            val mockProvider = User(id = "prov-10", name = "Prof. John")
            val expectedEligibility = ReviewEligibility(
                canReview = false,
                reason = "NO_INTERACTION",
                isVerified = false,
            )

            coEvery { getServiceByIdUseCase("srv-10") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-10") } returns Result.success(mockProvider)
            coEvery { getServiceReviewsUseCase("srv-10", 0, 5) } returns Result.success(
                PageResponse(emptyList(), 0, 5, 0, 0),
            )
            coEvery { getReviewEligibilityUseCase("srv-10") } returns Result.success(expectedEligibility)

            viewModel.initialize("srv-10")

            val state = viewModel.uiState.value
            assertEquals(expectedEligibility, state.reviewEligibility)
            assertFalse(state.reviewEligibility?.canReview == true)
            assertTrue(state.reviewEligibility?.isNoInteraction == true)
        }
}
