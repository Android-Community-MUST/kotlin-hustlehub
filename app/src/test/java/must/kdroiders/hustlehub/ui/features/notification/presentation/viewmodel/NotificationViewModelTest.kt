package must.kdroiders.hustlehub.ui.features.notification.presentation.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.notification.domain.model.Notification
import must.kdroiders.hustlehub.ui.features.notification.domain.model.NotificationType
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import must.kdroiders.hustlehub.ui.features.notification.presentation.view.groupNotificationsByDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant

class NotificationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: NotificationRepository = mockk(relaxed = true)
    private lateinit var viewModel: NotificationViewModel

    private val sampleNotifications = listOf(
        Notification(
            id = "notif-1",
            userId = "user-1",
            type = NotificationType.NEW_MESSAGE,
            title = "New Message",
            body = "Jane sent you a message",
            data = null,
            isRead = false,
            sentAt = Instant.now().toString(),
        ),
        Notification(
            id = "notif-2",
            userId = "user-1",
            type = NotificationType.NEW_REVIEW,
            title = "New Review",
            body = "Mary left a 5-star review",
            data = null,
            isRead = true,
            sentAt = Instant.now().minusSeconds(86400).toString(),
        ),
    )

    @Before
    fun setup() {
        coEvery { repository.getNotifications(any(), any()) } returns Result.success(sampleNotifications)
        viewModel = NotificationViewModel(repository)
    }

    @Test
    fun `loadNotifications fetches notifications and calculates unread count`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(2, state.notifications.size)
        assertEquals(1, state.unreadCount) // notif-1 is unread
        coVerify(exactly = 1) { repository.getNotifications(0, 50) }
    }

    @Test
    fun `markAsRead updates target notification optimistically`() = runTest {
        coEvery { repository.markRead("notif-1") } returns Result.success(Unit)

        viewModel.markAsRead("notif-1")

        val state = viewModel.uiState.value
        assertTrue(state.notifications.first { it.id == "notif-1" }.isRead)
        assertEquals(0, state.unreadCount)
        coVerify(exactly = 1) { repository.markRead("notif-1") }
    }

    @Test
    fun `markAllAsRead marks all notifications as read optimistically`() = runTest {
        coEvery { repository.markAllRead() } returns Result.success(Unit)

        viewModel.markAllAsRead()

        val state = viewModel.uiState.value
        assertTrue(state.notifications.all { it.isRead })
        assertEquals(0, state.unreadCount)
        coVerify(exactly = 1) { repository.markAllRead() }
    }

    @Test
    fun `deleteNotification removes notification from state optimistically`() = runTest {
        coEvery { repository.deleteNotification("notif-1") } returns Result.success(Unit)

        viewModel.deleteNotification("notif-1")

        val state = viewModel.uiState.value
        assertEquals(1, state.notifications.size)
        assertEquals("notif-2", state.notifications.first().id)
        coVerify(exactly = 1) { repository.deleteNotification("notif-1") }
    }

    @Test
    fun `groupNotificationsByDate groups items into Today and Yesterday`() {
        val groups = groupNotificationsByDate(sampleNotifications)
        assertTrue(groups.containsKey("Today"))
        assertTrue(groups.containsKey("Yesterday"))
        assertEquals(1, groups["Today"]?.size)
        assertEquals(1, groups["Yesterday"]?.size)
    }
}
