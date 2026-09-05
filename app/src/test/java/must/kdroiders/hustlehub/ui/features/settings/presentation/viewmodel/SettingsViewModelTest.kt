package must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel

import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
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
    private val testDispatcher = UnconfinedTestDispatcher()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val signOutUseCase: SignOutUseCase = mockk(relaxed = true)
    private val deleteAccountUseCase: must.kdroiders.hustlehub.ui.features.auth.domain.usecase.DeleteAccountUseCase = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val appDatabase: AppDatabase = mockk(relaxed = true)
    private val chatRepository: ChatRepository = mockk(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val mockUserInfo: com.google.firebase.auth.UserInfo = mockk {
            every { providerId } returns "password"
        }
        val mockUser: FirebaseUser = mockk {
            every { displayName } returns "John Doe"
            every { email } returns "john@must.ac.ke"
            every { photoUrl } returns null
            every { isEmailVerified } returns true
            every { providerData } returns listOf(mockUserInfo)
        }
        every { authRepository.getCurrentUser() } returns mockUser
        every { userPreferences.appTheme } returns kotlinx.coroutines.flow.flowOf(must.kdroiders.hustlehub.datastore.AppTheme.SYSTEM)

        viewModel = SettingsViewModel(
            authRepository = authRepository,
            signOutUseCase = signOutUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            userPreferences = userPreferences,
            appDatabase = appDatabase,
            chatRepository = chatRepository,
            hustleCrashlytics = HustleCrashlytics(null),
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
            val eventDeferred = async { viewModel.events.first() }
            viewModel.onNotificationsClicked()
            assertEquals(SettingsEvent.NavigateToNotifications, eventDeferred.await())
        }

    @Test
    fun `onDeleteAccountClicked sets deleteAccountStep to WARNING`() =
        runTest {
            viewModel.onDeleteAccountClicked()

            assertEquals(
                must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.DeleteAccountStep.WARNING,
                viewModel.uiState.value.deleteAccountStep,
            )
            assertTrue(viewModel.uiState.value.showDeleteAccountDialog)
        }

    @Test
    fun `onDeleteWarningConfirmed advances deleteAccountStep to PASSWORD_INPUT`() =
        runTest {
            viewModel.onDeleteAccountClicked()
            viewModel.onDeleteWarningConfirmed()

            assertEquals(
                must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.DeleteAccountStep.PASSWORD_INPUT,
                viewModel.uiState.value.deleteAccountStep,
            )
        }

    @Test
    fun `onDeleteAccountDismissed resets deleteAccountStep to NONE`() =
        runTest {
            viewModel.onDeleteAccountClicked()
            viewModel.onDeleteAccountDismissed()

            assertEquals(
                must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel.DeleteAccountStep.NONE,
                viewModel.uiState.value.deleteAccountStep,
            )
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

    // Sprint 5 — Scenario 11: delete account removes all data
    @Test
    fun `onDeleteAccountConfirmed executes DeleteAccountUseCase and emits AccountDeleted`() =
        runTest {
            coEvery { deleteAccountUseCase("secret123") } returns Result.success(Unit)

            val eventDeferred = async { viewModel.events.first() }

            viewModel.onDeleteAccountClicked()
            viewModel.onDeleteWarningConfirmed()
            viewModel.onDeletePasswordChanged("secret123")
            viewModel.onDeleteAccountConfirmed()
            advanceUntilIdle()

            coVerify(exactly = 1) { deleteAccountUseCase("secret123") }
            assertEquals(SettingsEvent.AccountDeleted, eventDeferred.await())
        }

    @Test
    fun `onDeleteWarningConfirmed for Google Auth user executes DeleteAccountUseCase directly without password`() =
        runTest {
            val googleUserInfo: com.google.firebase.auth.UserInfo = mockk {
                every { providerId } returns "google.com"
            }
            val googleUser: FirebaseUser = mockk {
                every { displayName } returns "Google User"
                every { email } returns "google@must.ac.ke"
                every { photoUrl } returns null
                every { isEmailVerified } returns true
                every { providerData } returns listOf(googleUserInfo)
            }
            every { authRepository.getCurrentUser() } returns googleUser
            coEvery { deleteAccountUseCase(null) } returns Result.success(Unit)

            val eventDeferred = async { viewModel.events.first() }

            viewModel.onDeleteAccountClicked()
            viewModel.onDeleteWarningConfirmed()
            advanceUntilIdle()

            coVerify(exactly = 1) { deleteAccountUseCase(null) }
            assertEquals(SettingsEvent.AccountDeleted, eventDeferred.await())
        }

    // Sprint 5 — Scenario 7: dark mode toggles and persists
    @Test
    fun `onDarkModeToggled true saves DARK theme to DataStore`() =
        runTest {
            val themeFlow = kotlinx.coroutines.flow.MutableStateFlow(must.kdroiders.hustlehub.datastore.AppTheme.SYSTEM)
            every { userPreferences.appTheme } returns themeFlow
            coEvery { userPreferences.saveTheme(any()) } answers {
                themeFlow.value = firstArg()
            }

            viewModel.onDarkModeToggled(true)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isDarkMode)
            assertEquals(
                must.kdroiders.hustlehub.datastore.AppTheme.DARK,
                viewModel.uiState.value.selectedTheme,
            )
            coVerify(exactly = 1) {
                userPreferences.saveTheme(must.kdroiders.hustlehub.datastore.AppTheme.DARK)
            }
        }
}
