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
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { userPreferences.cachedUser } returns flowOf(mockk(relaxed = true))
        every { userPreferences.isProviderBannerDismissed } returns flowOf(true)

        coEvery { userRepository.getUserProfile(any()) } returns Result.success(User(name = "John Doe"))
        coEvery { notificationRepository.getNotifications(any(), any()) } returns Result.success(emptyList())

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
    fun `initialization populates featuredServices using unrated fallback so it is never blank`() =
        runTest {
            val state = viewModel.uiState.value
            // Even though s-1 and s-2 have 0 rating and isFeatured = false,
            // featuredServices is not empty because of the unrated fallback.
            assertEquals(2, state.featuredServices.size)
            assertEquals(listOf("s-1", "s-2"), state.featuredServices.map { it.id })
        }

    @Test
    fun `featuredServices prioritizes paid boosted services before high rated and unrated services`() =
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
            // Max 5 items
            assertEquals(5, featured.size)
            // Tier 1: paid-2 (400L), paid-1 (300L)
            assertEquals("paid-2", featured[0].id)
            assertEquals("paid-1", featured[1].id)
            // Tier 2: rated-1 (4.8), rated-2 (4.2)
            assertEquals("rated-1", featured[2].id)
            assertEquals("rated-2", featured[3].id)
            // Tier 3: unrated-1 (100L)
            assertEquals("unrated-1", featured[4].id)
        }
}
