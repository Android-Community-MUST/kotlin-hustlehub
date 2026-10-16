package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.core.review.AppReviewManager
import must.kdroiders.hustlehub.core.telemetry.HustleAnalytics
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.usecase.GetProviderProfileUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.model.ReviewEligibility
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.CheckDuplicateReviewUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.DeleteReviewUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetMyReviewUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetReviewEligibilityUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.SubmitReviewUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.UpdateReviewUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WriteReviewViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val submitReviewUseCase: SubmitReviewUseCase = mockk(relaxed = true)
    private val updateReviewUseCase: UpdateReviewUseCase = mockk(relaxed = true)
    private val deleteReviewUseCase: DeleteReviewUseCase = mockk(relaxed = true)
    private val getMyReviewUseCase: GetMyReviewUseCase = mockk(relaxed = true)
    private val getServiceByIdUseCase: GetServiceByIdUseCase = mockk(relaxed = true)
    private val getProviderProfileUseCase: GetProviderProfileUseCase = mockk(relaxed = true)
    private val checkDuplicateReviewUseCase: CheckDuplicateReviewUseCase = mockk(relaxed = true)
    private val getReviewEligibilityUseCase: GetReviewEligibilityUseCase = mockk(relaxed = true)
    private val hustleAnalytics: HustleAnalytics = mockk(relaxed = true)
    private val hustleCrashlytics: HustleCrashlytics = mockk(relaxed = true)
    private val appReviewManager: AppReviewManager = mockk(relaxed = true)

    private lateinit var viewModel: WriteReviewViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { getMyReviewUseCase(any()) } returns Result.success(null)
        coEvery { checkDuplicateReviewUseCase(any()) } returns Result.success(false)
        coEvery { getReviewEligibilityUseCase(any()) } returns Result.success(
            ReviewEligibility(canReview = true, reason = "ELIGIBLE", isVerified = true),
        )

        viewModel = WriteReviewViewModel(
            submitReviewUseCase = submitReviewUseCase,
            updateReviewUseCase = updateReviewUseCase,
            deleteReviewUseCase = deleteReviewUseCase,
            getMyReviewUseCase = getMyReviewUseCase,
            getServiceByIdUseCase = getServiceByIdUseCase,
            getProviderProfileUseCase = getProviderProfileUseCase,
            checkDuplicateReviewUseCase = checkDuplicateReviewUseCase,
            getReviewEligibilityUseCase = getReviewEligibilityUseCase,
            hustleAnalytics = hustleAnalytics,
            hustleCrashlytics = hustleCrashlytics,
            appReviewManager = appReviewManager,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize checks duplicate review state`() =
        runTest {
            val mockService = Service(id = "srv-1", providerId = "prov-1", title = "Haircut")
            val mockProvider = User(id = "prov-1", name = "Barber Sam")

            coEvery { getServiceByIdUseCase("srv-1") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-1") } returns Result.success(mockProvider)
            coEvery { checkDuplicateReviewUseCase("srv-1") } returns Result.success(true)

            viewModel.initialize("srv-1")

            val state = viewModel.uiState.value
            assertFalse(state.isLoadingInfo)
            assertTrue(state.hasAlreadyReviewed)
        }

    @Test
    fun `submit delegates rating comment and isAnonymous to useCase`() =
        runTest {
            val mockService = Service(id = "srv-1", providerId = "prov-1", title = "Haircut")
            val mockProvider = User(id = "prov-1", name = "Barber Sam")
            val createdReview = Review(
                id = "rev-1",
                serviceId = "srv-1",
                providerId = "prov-1",
                customerId = "cust-1",
                customerName = "Customer",
                customerAvatarUrl = "",
                rating = 5,
                comment = "Awesome cut!",
                isAnonymous = false,
                createdAt = 1000L,
            )

            coEvery { getServiceByIdUseCase("srv-1") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-1") } returns Result.success(mockProvider)
            coEvery { checkDuplicateReviewUseCase("srv-1") } returns Result.success(false)
            coEvery { submitReviewUseCase("srv-1", 5, "Awesome cut!", false) } returns Result.success(createdReview)

            viewModel.initialize("srv-1")
            viewModel.onRatingChanged(5)
            viewModel.onCommentChanged("Awesome cut!")
            viewModel.submit()

            coVerify(exactly = 1) { submitReviewUseCase("srv-1", 5, "Awesome cut!", false) }
            assertTrue(viewModel.uiState.value.submitSuccess)
        }

    @Test
    fun `initialize loads existing review into edit mode when already reviewed`() =
        runTest {
            val mockService = Service(id = "srv-1", providerId = "prov-1", title = "Haircut")
            val mockProvider = User(id = "prov-1", name = "Barber Sam")
            val existingReview = Review(
                id = "rev-99",
                serviceId = "srv-1",
                providerId = "prov-1",
                customerId = "cust-1",
                customerName = "Customer",
                customerAvatarUrl = "",
                rating = 4,
                comment = "Initial review",
                isAnonymous = false,
                createdAt = 1000L,
            )

            coEvery { getServiceByIdUseCase("srv-1") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-1") } returns Result.success(mockProvider)
            coEvery { checkDuplicateReviewUseCase("srv-1") } returns Result.success(true)
            coEvery { getMyReviewUseCase("srv-1") } returns Result.success(existingReview)

            viewModel.initialize("srv-1")

            val state = viewModel.uiState.value
            assertTrue(state.hasAlreadyReviewed)
            assertTrue(state.isEditMode)
            assertEquals("rev-99", state.existingReviewId)
            assertEquals(4, state.rating)
            assertEquals("Initial review", state.comment)
        }

    @Test
    fun `initialize with initial review params enters edit mode and pre-fills values immediately`() =
        runTest {
            val mockService = Service(id = "srv-1", providerId = "prov-1", title = "Haircut")
            val mockProvider = User(id = "prov-1", name = "Barber Sam")

            coEvery { getServiceByIdUseCase("srv-1") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-1") } returns Result.success(mockProvider)
            coEvery { checkDuplicateReviewUseCase("srv-1") } returns Result.success(true)

            viewModel.initialize(
                id = "srv-1",
                reviewId = "rev-123",
                initialRating = 5,
                initialComment = "Great service!",
                initialIsAnonymous = true,
            )

            val state = viewModel.uiState.value
            assertTrue(state.isEditMode)
            assertTrue(state.hasAlreadyReviewed)
            assertEquals("rev-123", state.existingReviewId)
            assertEquals(5, state.rating)
            assertEquals("Great service!", state.comment)
            assertTrue(state.isAnonymous)
        }

    @Test
    fun `submit calls updateReviewUseCase when in edit mode`() =
        runTest {
            val mockService = Service(id = "srv-1", providerId = "prov-1", title = "Haircut")
            val mockProvider = User(id = "prov-1", name = "Barber Sam")
            val existingReview = Review(
                id = "rev-99",
                serviceId = "srv-1",
                providerId = "prov-1",
                customerId = "cust-1",
                customerName = "Customer",
                customerAvatarUrl = "",
                rating = 4,
                comment = "Initial review",
                isAnonymous = false,
                createdAt = 1000L,
            )
            val updatedReview = existingReview.copy(rating = 5, comment = "Updated review")

            coEvery { getServiceByIdUseCase("srv-1") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-1") } returns Result.success(mockProvider)
            coEvery { checkDuplicateReviewUseCase("srv-1") } returns Result.success(true)
            coEvery { getMyReviewUseCase("srv-1") } returns Result.success(existingReview)
            coEvery { updateReviewUseCase("rev-99", 5, "Updated review", false) } returns Result.success(updatedReview)

            viewModel.initialize("srv-1")
            viewModel.onRatingChanged(5)
            viewModel.onCommentChanged("Updated review")
            viewModel.submit()

            coVerify(exactly = 1) { updateReviewUseCase("rev-99", 5, "Updated review", false) }
            assertTrue(viewModel.uiState.value.submitSuccess)
        }

    @Test
    fun `deleteReview invokes deleteReviewUseCase and resets review state`() =
        runTest {
            val mockService = Service(id = "srv-1", providerId = "prov-1", title = "Haircut")
            val mockProvider = User(id = "prov-1", name = "Barber Sam")
            val existingReview = Review(
                id = "rev-99",
                serviceId = "srv-1",
                providerId = "prov-1",
                customerId = "cust-1",
                customerName = "Customer",
                customerAvatarUrl = "",
                rating = 4,
                comment = "Initial review",
                isAnonymous = false,
                createdAt = 1000L,
            )

            coEvery { getServiceByIdUseCase("srv-1") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-1") } returns Result.success(mockProvider)
            coEvery { checkDuplicateReviewUseCase("srv-1") } returns Result.success(true)
            coEvery { getMyReviewUseCase("srv-1") } returns Result.success(existingReview)
            coEvery { deleteReviewUseCase("rev-99") } returns Result.success(Unit)

            viewModel.initialize("srv-1")
            viewModel.deleteReview()

            coVerify(exactly = 1) { deleteReviewUseCase("rev-99") }
            val state = viewModel.uiState.value
            assertTrue(state.deleteSuccess)
            assertFalse(state.isEditMode)
            assertFalse(state.hasAlreadyReviewed)
            assertEquals(null, state.existingReviewId)
        }

    @Test
    fun `initialize marks isEligible false when eligibility is NO_INTERACTION`() =
        runTest {
            val mockService = Service(id = "srv-1", providerId = "prov-1", title = "Haircut")
            val mockProvider = User(id = "prov-1", name = "Barber Sam")

            coEvery { getServiceByIdUseCase("srv-1") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-1") } returns Result.success(mockProvider)
            coEvery { getReviewEligibilityUseCase("srv-1") } returns Result.success(
                ReviewEligibility(canReview = false, reason = "NO_INTERACTION", isVerified = false),
            )

            viewModel.initialize("srv-1")

            val state = viewModel.uiState.value
            assertFalse(state.isLoadingInfo)
            assertFalse(state.isEligible)
            assertEquals("NO_INTERACTION", state.ineligibilityReason)
            assertFalse(state.canSubmit)
        }

    @Test
    fun `submit does nothing when user is ineligible and not in edit mode`() =
        runTest {
            val mockService = Service(id = "srv-1", providerId = "prov-1", title = "Haircut")
            val mockProvider = User(id = "prov-1", name = "Barber Sam")

            coEvery { getServiceByIdUseCase("srv-1") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-1") } returns Result.success(mockProvider)
            coEvery { getReviewEligibilityUseCase("srv-1") } returns Result.success(
                ReviewEligibility(canReview = false, reason = "NO_INTERACTION", isVerified = false),
            )

            viewModel.initialize("srv-1")
            viewModel.onRatingChanged(5)
            viewModel.onCommentChanged("Awesome service!")
            viewModel.submit()

            coVerify(exactly = 0) { submitReviewUseCase(any(), any(), any(), any()) }
            assertFalse(viewModel.uiState.value.submitSuccess)
        }

    @Test
    fun `initialize with different serviceId resets submitSuccess and form state`() =
        runTest {
            val service1 = Service(id = "srv-1", providerId = "prov-1", title = "Haircut")
            val provider1 = User(id = "prov-1", name = "Barber Sam")
            val createdReview = Review(
                id = "rev-1",
                serviceId = "srv-1",
                providerId = "prov-1",
                customerId = "cust-1",
                customerName = "Customer",
                customerAvatarUrl = "",
                rating = 5,
                comment = "Awesome cut!",
                isAnonymous = false,
                createdAt = 1000L,
            )

            val service2 = Service(id = "srv-2", providerId = "prov-2", title = "Plumbing")
            val provider2 = User(id = "prov-2", name = "Plumber Bob")

            coEvery { getServiceByIdUseCase("srv-1") } returns Result.success(service1)
            coEvery { getProviderProfileUseCase("prov-1") } returns Result.success(provider1)
            coEvery { checkDuplicateReviewUseCase("srv-1") } returns Result.success(false)
            coEvery { submitReviewUseCase("srv-1", 5, "Awesome cut!", false) } returns Result.success(createdReview)

            coEvery { getServiceByIdUseCase("srv-2") } returns Result.success(service2)
            coEvery { getProviderProfileUseCase("prov-2") } returns Result.success(provider2)
            coEvery { checkDuplicateReviewUseCase("srv-2") } returns Result.success(false)

            viewModel.initialize("srv-1")
            viewModel.onRatingChanged(5)
            viewModel.onCommentChanged("Awesome cut!")
            viewModel.submit()
            assertTrue(viewModel.uiState.value.submitSuccess)

            viewModel.initialize("srv-2")
            val state2 = viewModel.uiState.value

            assertFalse("submitSuccess should be reset to false for new service", state2.submitSuccess)
            assertEquals("Rating should be reset to 0 for new service", 0, state2.rating)
            assertEquals("Comment should be reset to blank for new service", "", state2.comment)
            assertEquals("Service should be srv-2", "srv-2", state2.service?.id)
            assertEquals("Provider should be prov-2", "prov-2", state2.provider?.id)
        }

    @Test
    fun `resetState resets uiState to initial default and clears serviceId`() =
        runTest {
            val mockService = Service(id = "srv-1", providerId = "prov-1", title = "Haircut")
            val mockProvider = User(id = "prov-1", name = "Barber Sam")

            coEvery { getServiceByIdUseCase("srv-1") } returns Result.success(mockService)
            coEvery { getProviderProfileUseCase("prov-1") } returns Result.success(mockProvider)

            viewModel.initialize("srv-1")
            viewModel.onRatingChanged(4)
            viewModel.onCommentChanged("Good job")

            viewModel.resetState()

            val state = viewModel.uiState.value
            assertEquals(0, state.rating)
            assertEquals("", state.comment)
            assertFalse(state.submitSuccess)
            assertEquals(null, state.service)
            assertEquals(null, state.provider)
        }
}
