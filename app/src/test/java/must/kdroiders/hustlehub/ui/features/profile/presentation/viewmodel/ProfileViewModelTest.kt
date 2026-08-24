package must.kdroiders.hustlehub.ui.features.profile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetMyServicesUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.UpdateAvailabilityUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val getMyServicesUseCase: GetMyServicesUseCase = mockk(relaxed = true)
    private val updateAvailabilityUseCase: UpdateAvailabilityUseCase = mockk(relaxed = true)

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "uid-100"
        }
        coEvery { authRepository.getCurrentUser() } returns firebaseUser

        val user = User(
            id = "uid-100",
            name = "John Hustler",
            email = "john@must.ac.ke",
            isVerified = true,
        )
        val services = listOf(
            Service(id = "srv-1", title = "Laptop Repair", availability = ServiceAvailability.AVAILABLE, reviewCount = 5, averageRating = 4.8f),
        )

        coEvery { userRepository.getUserProfile("uid-100") } returns Result.success(user)
        coEvery { getMyServicesUseCase() } returns Result.success(services)

        viewModel = ProfileViewModel(
            authRepository = authRepository,
            userRepository = userRepository,
            getMyServicesUseCase = getMyServicesUseCase,
            updateAvailabilityUseCase = updateAvailabilityUseCase,
        )
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile calculates stats badges and lists services`() =
        runTest {
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.user)
            assertEquals("John Hustler", state.user?.name)
            assertEquals(1, state.services.size)
            assertEquals(5, state.reviewCount)
        }

    @Test
    fun `toggleServiceActive updates service availability status`() =
        runTest {
            coEvery { updateAvailabilityUseCase("srv-1", ServiceAvailability.OFFLINE) } returns Result.success(Service(id = "srv-1", availability = ServiceAvailability.OFFLINE))

            viewModel.toggleServiceActive("srv-1")

            coVerify(exactly = 1) { updateAvailabilityUseCase("srv-1", ServiceAvailability.OFFLINE) }
            assertEquals(
                ServiceAvailability.OFFLINE,
                viewModel.uiState.value.services[0]
                    .availability,
            )
        }
}
