package must.kdroiders.hustlehub.ui.features.chat.presentation.viewmodel

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UnreadCountViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val conversationDao: ConversationDao = mockk(relaxed = true)
    private val notificationRepository: NotificationRepository = mockk(relaxed = true)

    private lateinit var viewModel: UnreadCountViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { conversationDao.getTotalUnreadCount() } returns flowOf(5)
        coEvery { notificationRepository.getUnreadCount() } returns Result.success(3)

        viewModel = UnreadCountViewModel(
            context = context,
            conversationDao = conversationDao,
            notificationRepository = notificationRepository,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `unreadMessageCount reflects conversationDao flow`() =
        runTest {
            assertEquals(5, viewModel.unreadMessageCount.value)
        }

    @Test
    fun `unreadNotificationCount reflects notificationRepository getUnreadCount`() =
        runTest {
            assertEquals(3, viewModel.unreadNotificationCount.value)
        }

    @Test
    fun `totalUnreadCount combines messages and notifications`() =
        runTest {
            assertEquals(8, viewModel.totalUnreadCount.value)
        }

    @Test
    fun `clearNotificationsBadge clears unreadNotificationCount to zero`() =
        runTest {
            coEvery { notificationRepository.markAllRead() } returns Result.success(Unit)

            viewModel.clearNotificationsBadge()

            assertEquals(0, viewModel.unreadNotificationCount.value)
            assertEquals(5, viewModel.totalUnreadCount.value)
            coVerify(exactly = 1) { notificationRepository.markAllRead() }
        }
}
