package must.kdroiders.hustlehub.ui.features.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.data.local.AppDatabase
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteAccountUseCaseTest {
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val chatRepository: ChatRepository = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val appDatabase: AppDatabase = mockk(relaxed = true)

    private lateinit var useCase: DeleteAccountUseCase

    @Before
    fun setUp() {
        useCase = DeleteAccountUseCase(
            authRepository = authRepository,
            chatRepository = chatRepository,
            userPreferences = userPreferences,
            appDatabase = appDatabase,
        )
    }

    @Test
    fun `invoke with null or blank password passes null to authRepository for OAuth users`() =
        runTest {
            coEvery { authRepository.deleteAccount(null) } returns Result.success(Unit)

            val result = useCase("")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { authRepository.deleteAccount(null) }
        }

    @Test
    fun `invoke with correct password executes delete pipeline successfully`() =
        runTest {
            coEvery { authRepository.deleteAccount("password123") } returns Result.success(Unit)

            val result = useCase("password123")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { authRepository.deleteAccount("password123") }
            coVerify(exactly = 1) { chatRepository.disconnectWebSocket() }
            coVerify(exactly = 1) { userPreferences.clearUser() }
            coVerify(exactly = 1) { appDatabase.clearAllTables() }
        }

    @Test
    fun `invoke fails when authRepository deleteAccount fails`() =
        runTest {
            coEvery { authRepository.deleteAccount("wrongpass") } returns Result.failure(Exception("Incorrect password"))

            val result = useCase("wrongpass")

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { userPreferences.clearUser() }
            coVerify(exactly = 0) { appDatabase.clearAllTables() }
        }
}
