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
import must.kdroiders.hustlehub.ui.features.notification.domain.model.NotificationPreferences
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    private val testDispatcher = UnconfinedTestDispatcher()
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class NotificationPreferencesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: NotificationRepository = mockk(relaxed = true)
    private lateinit var viewModel: NotificationPreferencesViewModel

    @Before
    fun setup() {
        coEvery { repository.getPreferences() } returns Result.success(
            NotificationPreferences(
                newMessages = true,
                newReviews = true,
                serviceInquiries = true,
                marketing = false,
                soundEnabled = true,
                vibrationEnabled = true,
                quietHoursStart = 22,
                quietHoursEnd = 7,
            )
        )
        viewModel = NotificationPreferencesViewModel(repository)
    }

    @Test
    fun `init loads preferences from repository`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.preferences.newMessages)
        assertFalse(state.preferences.marketing)
        assertEquals(22, state.preferences.quietHoursStart)
        assertEquals(7, state.preferences.quietHoursEnd)
        coVerify(exactly = 1) { repository.getPreferences() }
    }

    @Test
    fun `onNewMessagesToggled updates state and calls updatePreferences`() = runTest {
        coEvery { repository.updatePreferences(any()) } returns Result.success(
            NotificationPreferences(newMessages = false)
        )

        viewModel.onNewMessagesToggled(false)

        assertFalse(viewModel.uiState.value.preferences.newMessages)
        coVerify { repository.updatePreferences(match { !it.newMessages }) }
    }

    @Test
    fun `onMarketingToggled updates state and calls updatePreferences`() = runTest {
        coEvery { repository.updatePreferences(any()) } returns Result.success(
            NotificationPreferences(marketing = true)
        )

        viewModel.onMarketingToggled(true)

        assertTrue(viewModel.uiState.value.preferences.marketing)
        coVerify { repository.updatePreferences(match { it.marketing }) }
    }

    @Test
    fun `onQuietHoursStartChanged updates state and saves`() = runTest {
        coEvery { repository.updatePreferences(any()) } returns Result.success(
            NotificationPreferences(quietHoursStart = 23)
        )

        viewModel.onQuietHoursStartChanged(23)

        assertEquals(23, viewModel.uiState.value.preferences.quietHoursStart)
        coVerify { repository.updatePreferences(match { it.quietHoursStart == 23 }) }
    }

    @Test
    fun `onQuietHoursEndChanged updates state and saves`() = runTest {
        coEvery { repository.updatePreferences(any()) } returns Result.success(
            NotificationPreferences(quietHoursEnd = 6)
        )

        viewModel.onQuietHoursEndChanged(6)

        assertEquals(6, viewModel.uiState.value.preferences.quietHoursEnd)
        coVerify { repository.updatePreferences(match { it.quietHoursEnd == 6 }) }
    }
}
