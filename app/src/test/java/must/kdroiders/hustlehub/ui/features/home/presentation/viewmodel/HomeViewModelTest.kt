package must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.core.auth.AuthManager
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.home.domain.usecase.BrowseServicesUseCase
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetMyServicesUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val browseServices: BrowseServicesUseCase = mockk(relaxed = true)
    private val authManager: AuthManager = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val notificationRepository: NotificationRepository = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val getMyServicesUseCase: GetMyServicesUseCase = mockk(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { userPreferences.cachedUser } returns flowOf(mockk(relaxed = true))
        every { userPreferences.isProviderBannerDismissed } returns flowOf(true)

        coEvery { userRepository.getUserProfile(any()) } returns Result.success(User(name = "John Doe"))
        coEvery { notificationRepository.getNotifications(any(), any()) } returns Result.success(emptyList())
        coEvery { getMyServicesUseCase() } returns Result.success(emptyList())

        val page = PageResponse(
            content = listOf(
                Service(id = "s-1", title = "Laptop Repair", category = ServiceCategory.TECH),
                Service(id = "s-2", title = "Haircut", category = ServiceCategory.SALON),
            ),
            page = 0,
            size = 10,
            totalElements = 2,
            totalPages = 1,
        )
        coEvery { browseServices(page = 0, size = 10, category = null, query = null) } returns Result.success(page)

        viewModel = HomeViewModel(
            browseServices = browseServices,
            authManager = authManager,
            userRepository = userRepository,
            notificationRepository = notificationRepository,
            userPreferences = userPreferences,
            getMyServicesUseCase = getMyServicesUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads services for category ALL`() =
        runTest {
            val state = viewModel.uiState.value
            assertFalse(state.isLoadingServices)
            assertEquals(2, state.services.size)
            assertEquals(ServiceCategory.ALL, state.selectedCategory)
        }

    @Test
    fun `onCategorySelected updates selectedCategory and re-fetches services`() =
        runTest {
            val techPage = PageResponse(
                content = listOf(Service(id = "s-1", title = "Laptop Repair", category = ServiceCategory.TECH)),
                page = 0,
                size = 10,
                totalElements = 1,
                totalPages = 1,
            )
            coEvery {
                browseServices(page = 0, size = 10, category = ServiceCategory.TECH, query = null)
            } returns Result.success(techPage)

            viewModel.onCategorySelected(ServiceCategory.TECH)

            assertEquals(ServiceCategory.TECH, viewModel.uiState.value.selectedCategory)
            assertEquals(1, viewModel.uiState.value.services.size)
            coVerify { browseServices(page = 0, size = 10, category = ServiceCategory.TECH, query = null) }
        }

    @Test
    fun `initialization leaves featuredServices empty when no services are featured`() =
        runTest {
            val state = viewModel.uiState.value
            // Since s-1 and s-2 have isFeatured = false, featuredServices is empty.
            assertTrue(state.featuredServices.isEmpty())
        }

    @Test
    fun `featuredServices strictly includes only active paid boosted services`() =
        runTest {
            val mixedPage = PageResponse(
                content = listOf(
                    Service(
                        id = "unrated-1",
                        title = "Unrated 1",
                        averageRating = 0f,
                        isFeatured = false,
                        createdAt = 100L,
                    ),
                    Service(
                        id = "rated-1",
                        title = "Rated 1",
                        averageRating = 4.8f,
                        reviewCount = 10,
                        isFeatured = false,
                        createdAt = 200L,
                    ),
                    Service(
                        id = "paid-1",
                        title = "Paid 1",
                        averageRating = 0f,
                        isFeatured = true,
                        createdAt = 300L,
                    ),
                    Service(
                        id = "rated-2",
                        title = "Rated 2",
                        averageRating = 4.2f,
                        reviewCount = 5,
                        isFeatured = false,
                        createdAt = 150L,
                    ),
                    Service(
                        id = "paid-2",
                        title = "Paid 2",
                        averageRating = 5.0f,
                        isFeatured = true,
                        createdAt = 400L,
                    ),
                    Service(
                        id = "unrated-2",
                        title = "Unrated 2",
                        averageRating = 0f,
                        isFeatured = false,
                        createdAt = 50L,
                    ),
                ),
                page = 0,
                size = 10,
                totalElements = 6,
                totalPages = 1,
            )
            coEvery {
                browseServices(page = 0, size = 10, category = ServiceCategory.TECH, query = null)
            } returns Result.success(mixedPage)

            viewModel.onCategorySelected(ServiceCategory.TECH)

            val featured = viewModel.uiState.value.featuredServices
            // Only paid-1 and paid-2 are featured
            assertEquals(2, featured.size)
            assertEquals("paid-2", featured[0].id)
            assertEquals("paid-1", featured[1].id)
        }

    @Test
    fun `showProviderBanner is false when user is blank on fresh install`() =
        runTest {
            every { userPreferences.cachedUser } returns flowOf(User(id = "", role = UserRole.ROLE_CUSTOMER))
            every { userPreferences.isProviderBannerDismissed } returns flowOf(false)

            val vm = HomeViewModel(
                browseServices = browseServices,
                authManager = authManager,
                userRepository = userRepository,
                notificationRepository = notificationRepository,
                userPreferences = userPreferences,
                getMyServicesUseCase = getMyServicesUseCase,
            )

            assertFalse(vm.uiState.value.showProviderBanner)
        }

    @Test
    fun `showProviderBanner is false when user is provider`() =
        runTest {
            every { userPreferences.cachedUser } returns flowOf(User(id = "uid-1", role = UserRole.ROLE_PROVIDER))
            every { userPreferences.isProviderBannerDismissed } returns flowOf(false)

            val vm = HomeViewModel(
                browseServices = browseServices,
                authManager = authManager,
                userRepository = userRepository,
                notificationRepository = notificationRepository,
                userPreferences = userPreferences,
                getMyServicesUseCase = getMyServicesUseCase,
            )

            assertFalse(vm.uiState.value.showProviderBanner)
            coVerify { userPreferences.dismissProviderBanner() }
        }

    @Test
    fun `showProviderBanner is false and dismissed when user has listed services`() =
        runTest {
            every { userPreferences.cachedUser } returns flowOf(User(id = "uid-1", role = UserRole.ROLE_CUSTOMER))
            every { userPreferences.isProviderBannerDismissed } returns flowOf(false)
            coEvery { getMyServicesUseCase() } returns Result.success(
                listOf(Service(id = "s-1", title = "Repair", category = ServiceCategory.TECH)),
            )
            val mockFirebaseUser: com.google.firebase.auth.FirebaseUser = mockk {
                every { uid } returns "uid-1"
            }
            every { authManager.currentUser() } returns mockFirebaseUser
            coEvery { userRepository.getUserProfile("uid-1") } returns Result.success(
                User(id = "uid-1", name = "John Doe", role = UserRole.ROLE_CUSTOMER),
            )

            val vm = HomeViewModel(
                browseServices = browseServices,
                authManager = authManager,
                userRepository = userRepository,
                notificationRepository = notificationRepository,
                userPreferences = userPreferences,
                getMyServicesUseCase = getMyServicesUseCase,
            )

            assertFalse(vm.uiState.value.showProviderBanner)
            coVerify { userPreferences.dismissProviderBanner() }
            coVerify { userPreferences.writeUser(match { it.role == UserRole.ROLE_PROVIDER }) }
        }

    @Test
    fun `showProviderBanner is true for customer with no services and banner not dismissed`() =
        runTest {
            every { userPreferences.cachedUser } returns flowOf(User(id = "uid-cust", role = UserRole.ROLE_CUSTOMER))
            every { userPreferences.isProviderBannerDismissed } returns flowOf(false)
            coEvery { getMyServicesUseCase() } returns Result.success(emptyList())

            val vm = HomeViewModel(
                browseServices = browseServices,
                authManager = authManager,
                userRepository = userRepository,
                notificationRepository = notificationRepository,
                userPreferences = userPreferences,
                getMyServicesUseCase = getMyServicesUseCase,
            )

            assertTrue(vm.uiState.value.showProviderBanner)
        }
}
