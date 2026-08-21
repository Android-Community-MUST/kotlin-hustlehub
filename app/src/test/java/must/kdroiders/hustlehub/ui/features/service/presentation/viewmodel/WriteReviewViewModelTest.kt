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
import must.kdroiders.hustlehub.core.telemetry.HustleAnalytics
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.usecase.GetProviderProfileUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.CheckDuplicateReviewUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.SubmitReviewUseCase
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WriteReviewViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val submitReviewUseCase: SubmitReviewUseCase = mockk(relaxed = true)
    private val getServiceByIdUseCase: GetServiceByIdUseCase = mockk(relaxed = true)
    private val getProviderProfileUseCase: GetProviderProfileUseCase = mockk(relaxed = true)
    private val checkDuplicateReviewUseCase: CheckDuplicateReviewUseCase = mockk(relaxed = true)
    private val hustleAnalytics: HustleAnalytics = mockk(relaxed = true)
    private val hustleCrashlytics: HustleCrashlytics = mockk(relaxed = true)

    private lateinit var viewModel: WriteReviewViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        viewModel = WriteReviewViewModel(
            submitReviewUseCase = submitReviewUseCase,
            getServiceByIdUseCase = getServiceByIdUseCase,
            getProviderProfileUseCase = getProviderProfileUseCase,
            checkDuplicateReviewUseCase = checkDuplicateReviewUseCase,
            hustleAnalytics = hustleAnalytics,
            hustleCrashlytics = hustleCrashlytics,
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
}
