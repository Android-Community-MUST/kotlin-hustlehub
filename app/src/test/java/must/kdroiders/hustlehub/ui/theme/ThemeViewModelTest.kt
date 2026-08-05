package must.kdroiders.hustlehub.ui.theme

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.datastore.AppTheme
import must.kdroiders.hustlehub.datastore.UserPreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val userPreferences: UserPreferences = mockk(relaxed = true)

    private lateinit var viewModel: ThemeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferences.appTheme } returns flowOf(AppTheme.DARK)

        viewModel = ThemeViewModel(userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `theme stateFlow emits persisted theme from UserPreferences`() = runTest {
        backgroundScope.launch { viewModel.theme.collect {} }
        advanceUntilIdle()

        assertEquals(AppTheme.DARK, viewModel.theme.value)
    }

    @Test
    fun `setTheme calls saveTheme on UserPreferences`() = runTest {
        coEvery { userPreferences.saveTheme(any()) } returns Unit

        viewModel.setTheme(AppTheme.LIGHT)
        advanceUntilIdle()

        coVerify(exactly = 1) { userPreferences.saveTheme(AppTheme.LIGHT) }
    }
}
