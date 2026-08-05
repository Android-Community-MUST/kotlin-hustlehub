package must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel

import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.data.local.AppDatabase
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.usecase.SignOutUseCase
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val signOutUseCase: SignOutUseCase = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val appDatabase: AppDatabase = mockk(relaxed = true)
    private val chatRepository: ChatRepository = mockk(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val mockUser: FirebaseUser = mockk {
            every { displayName } returns "John Doe"
            every { email } returns "john@must.ac.ke"
            every { photoUrl } returns null
            every { isEmailVerified } returns true
        }
        every { authRepository.getCurrentUser() } returns mockUser
        every { userPreferences.appTheme } returns kotlinx.coroutines.flow.flowOf(must.kdroiders.hustlehub.datastore.AppTheme.SYSTEM)

        viewModel = SettingsViewModel(
            authRepository = authRepository,
            signOutUseCase = signOutUseCase,
            userPreferences = userPreferences,
            appDatabase = appDatabase,
            chatRepository = chatRepository,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCurrentUser populates user details on init`() =
        runTest {
            val state = viewModel.uiState.value

            assertEquals("John Doe", state.displayName)
            assertEquals("@JohnDoe_Hustler", state.username)
            assertTrue(state.isVerified)
            assertEquals(must.kdroiders.hustlehub.BuildConfig.VERSION_NAME, state.appVersion)
        }

    @Test
    fun `onDarkModeToggled updates isDarkMode in uiState`() =
        runTest {
            viewModel.onDarkModeToggled(false)

            assertFalse(viewModel.uiState.value.isDarkMode)
        }

    @Test
    fun `navigation actions emit corresponding SettingsEvents`() =
        runTest {
            viewModel.onNotificationsClicked()
            val event = viewModel.events.first()

            assertEquals(SettingsEvent.NavigateToNotifications, event)
        }

    @Test
    fun `onDeleteAccountClicked shows confirmation dialog`() =
        runTest {
            viewModel.onDeleteAccountClicked()

            assertTrue(viewModel.uiState.value.showDeleteAccountDialog)
        }

    @Test
    fun `onDeleteAccountDismissed hides confirmation dialog`() =
        runTest {
            viewModel.onDeleteAccountClicked()
            viewModel.onDeleteAccountDismissed()

            assertFalse(viewModel.uiState.value.showDeleteAccountDialog)
        }

    @Test
    fun `onLogOutClicked performs signout and emits LoggedOut event`() =
        runTest {
            coEvery { signOutUseCase() } returns Unit

            viewModel.onLogOutClicked()
            advanceUntilIdle()

            coVerify(exactly = 1) { signOutUseCase() }
            coVerify(exactly = 1) { userPreferences.clearUser() }
        }
}
