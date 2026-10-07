package must.kdroiders.hustlehub.splash

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.core.deeplink.InstallReferrerManager
import must.kdroiders.hustlehub.core.security.KeyExchangeHandler
import must.kdroiders.hustlehub.data.local.AppDatabase
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val appDatabase: AppDatabase = mockk(relaxed = true)
    private val keyExchangeHandler: KeyExchangeHandler = mockk(relaxed = true)
    private val installReferrerManager: InstallReferrerManager = mockk(relaxed = true)

    private val firebaseUser: FirebaseUser = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferences.isFirstLaunch } returns flowOf(false)
        every { userPreferences.hasPendingDeletion } returns flowOf(false)
        every { userPreferences.hasProcessedInstallReferrer } returns flowOf(true)
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "test-uid-1"
        every { firebaseUser.email } returns "student@must.ac.ke"
        every { firebaseUser.isEmailVerified } returns true
        every { firebaseUser.reload() } returns com.google.android.gms.tasks.Tasks.forResult(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when user is verified and profile exists, routes to Home`() =
        runTest {
            val user = User(id = "test-uid-1", name = "Test Hustler", email = "student@must.ac.ke")
            coEvery { userRepository.getUserProfile("test-uid-1") } returns Result.success(user)

            val viewModel = SplashViewModel(
                firebaseAuth = firebaseAuth,
                userPreferences = userPreferences,
                userRepository = userRepository,
                appDatabase = appDatabase,
                keyExchangeHandler = keyExchangeHandler,
                installReferrerManager = installReferrerManager,
            )
            testScheduler.advanceUntilIdle()

            assertEquals(SplashDestination.Home, viewModel.destination.value)
        }

    @Test
    fun `when user is verified but backend profile returns 404, routes to ProfileSetup`() =
        runTest {
            val responseBody = "{\"message\":\"User not found\"}".toResponseBody("application/json".toMediaType())
            val http404 = HttpException(Response.error<Any>(404, responseBody))
            coEvery { userRepository.getUserProfile("test-uid-1") } returns Result.failure(http404)

            val viewModel = SplashViewModel(
                firebaseAuth = firebaseAuth,
                userPreferences = userPreferences,
                userRepository = userRepository,
                appDatabase = appDatabase,
                keyExchangeHandler = keyExchangeHandler,
                installReferrerManager = installReferrerManager,
            )
            testScheduler.advanceUntilIdle()

            assertEquals(SplashDestination.ProfileSetup, viewModel.destination.value)
        }

    @Test
    fun `when user is verified but backend profile is null, routes to ProfileSetup`() =
        runTest {
            coEvery { userRepository.getUserProfile("test-uid-1") } returns Result.success(null)

            val viewModel = SplashViewModel(
                firebaseAuth = firebaseAuth,
                userPreferences = userPreferences,
                userRepository = userRepository,
                appDatabase = appDatabase,
                keyExchangeHandler = keyExchangeHandler,
                installReferrerManager = installReferrerManager,
            )
            testScheduler.advanceUntilIdle()

            assertEquals(SplashDestination.ProfileSetup, viewModel.destination.value)
        }

    @Test
    fun `when user is not verified, routes to Login`() =
        runTest {
            every { firebaseUser.isEmailVerified } returns false

            val viewModel = SplashViewModel(
                firebaseAuth = firebaseAuth,
                userPreferences = userPreferences,
                userRepository = userRepository,
                appDatabase = appDatabase,
                keyExchangeHandler = keyExchangeHandler,
                installReferrerManager = installReferrerManager,
            )
            testScheduler.advanceUntilIdle()

            assertEquals(SplashDestination.Login, viewModel.destination.value)
        }
}
