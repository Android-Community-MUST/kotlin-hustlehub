package must.kdroiders.hustlehub.ui.features.map.presentation.viewmodel

import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.map.domain.usecase.GetMapPinsUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var getMapPinsUseCase: GetMapPinsUseCase
    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMapPinsUseCase = mockk()
        // Always stub the use case before initializing the ViewModel to avoid crashes in the init polling loop
        coEvery {
            getMapPinsUseCase(any(), any(), any(), any(), any())
        } returns Result.success(emptyList())

        viewModel = MapViewModel(getMapPinsUseCase, startPollingImmediately = false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is set correctly`() {
        val state = viewModel.uiState.value
        assertTrue(state.pins.isEmpty())
        assertNull(state.selectedCategory)
        assertEquals(ServiceAvailability.AVAILABLE, state.availability)
        assertNull(state.userLocation)
        assertNull(state.error)
    }

    @Test
    fun `selectCategory updates category filter and fetches pins`() = runTest {
        viewModel.selectCategory(ServiceCategory.SALON)

        assertEquals(ServiceCategory.SALON, viewModel.uiState.value.selectedCategory)
        coVerify {
            getMapPinsUseCase(
                lat = null,
                lng = null,
                radiusKm = null,
                category = ServiceCategory.SALON,
                availability = ServiceAvailability.AVAILABLE
            )
        }
    }

    @Test
    fun `selectAvailability updates availability filter and fetches pins`() = runTest {
        viewModel.selectAvailability(null)

        assertNull(viewModel.uiState.value.availability)
        coVerify {
            getMapPinsUseCase(
                lat = null,
                lng = null,
                radiusKm = null,
                category = null,
                availability = null
            )
        }
    }

    @Test
    fun `updateUserLocation updates coordinates in UI state`() {
        val latLng = LatLng(0.0515, 37.6456)
        viewModel.updateUserLocation(latLng)

        assertEquals(latLng, viewModel.uiState.value.userLocation)
    }
}
