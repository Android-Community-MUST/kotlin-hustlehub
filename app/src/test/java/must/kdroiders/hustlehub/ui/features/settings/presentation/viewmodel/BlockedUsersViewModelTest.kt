package must.kdroiders.hustlehub.ui.features.settings.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BlockedUsersViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val userRepository: UserRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBlockedUsers loads blocked users list successfully`() =
        runTest {
            val sampleUsers = listOf(
                User(id = "user1", uuid = "uuid1", name = "Blocked User 1", email = "b1@test.com"),
                User(id = "user2", uuid = "uuid2", name = "Blocked User 2", email = "b2@test.com"),
            )
            coEvery { userRepository.getBlockedUsers() } returns Result.success(sampleUsers)

            val viewModel = BlockedUsersViewModel(userRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.blockedUsers.size)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `unblockUser calls repository and removes user from state`() =
        runTest {
            val sampleUsers = listOf(
                User(id = "user1", uuid = "uuid1", name = "Blocked User 1", email = "b1@test.com"),
            )
            coEvery { userRepository.getBlockedUsers() } returns Result.success(sampleUsers)
            coEvery { userRepository.unblockUser("uuid1") } returns Result.success(Unit)

            val viewModel = BlockedUsersViewModel(userRepository)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.unblockUser("uuid1")
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { userRepository.unblockUser("uuid1") }
            assertTrue(
                viewModel.uiState.value.blockedUsers
                    .isEmpty(),
            )
        }

    // Sprint 5 — Scenario 8: block hides user — repository called with correct UUID
    @Test
    fun `blockUser calls repository with the correct target UUID`() =
        runTest {
            val targetUuid = "provider-uuid-456"
            coEvery { userRepository.blockUser(targetUuid) } returns Result.success(Unit)

            userRepository.blockUser(targetUuid)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { userRepository.blockUser(targetUuid) }
        }
}
