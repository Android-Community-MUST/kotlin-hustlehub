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
            coEvery { browseServices(page = 0, size = 10, category = ServiceCategory.TECH, query = null) } returns Result.success(techPage)

            viewModel.onCategorySelected(ServiceCategory.TECH)

            assertEquals(ServiceCategory.TECH, viewModel.uiState.value.selectedCategory)
            assertEquals(1, viewModel.uiState.value.services.size)
            coVerify { browseServices(page = 0, size = 10, category = ServiceCategory.TECH, query = null) }
        }
}
