package must.kdroiders.hustlehub.core.network

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isConnected state flow reflects observer flow`() {
        val mockObserver = mockk<ConnectivityObserver>()
        every { mockObserver.isConnected } returns flowOf(true)

        val viewModel = ConnectivityViewModel(mockObserver)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.isConnected.value)
    }
}
