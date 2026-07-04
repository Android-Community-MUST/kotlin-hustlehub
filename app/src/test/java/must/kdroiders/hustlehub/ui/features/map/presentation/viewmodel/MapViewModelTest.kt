package must.kdroiders.hustlehub.ui.features.map.presentation.viewmodel

import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.map.domain.usecase.GetMapPinsUseCase
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
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
    private lateinit var userPreferences: UserPreferences
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMapPinsUseCase = mockk()
        userPreferences = mockk(relaxed = true) {
            every { lastSelectedCategory } returns flowOf(null)
        }
        notificationRepository = mockk(relaxed = true) {
            coEvery { getNotifications(any(), any()) } returns Result.success(emptyList())
        }

        // Always stub the use case before initializing the ViewModel to avoid crashes in the init polling loop
        coEvery {
            getMapPinsUseCase(any(), any(), any(), any(), any())
        } returns Result.success(emptyList())

        viewModel = MapViewModel(getMapPinsUseCase, userPreferences, notificationRepository, startPollingImmediately = false)
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
    fun `selectCategory updates category filter, persists selection, and fetches pins`() =
        runTest {
            viewModel.selectCategory(ServiceCategory.SALON)

            assertEquals(ServiceCategory.SALON, viewModel.uiState.value.selectedCategory)
            coVerify {
                userPreferences.saveLastSelectedCategory(ServiceCategory.SALON.name)
                getMapPinsUseCase(
                    lat = null,
                    lng = null,
                    radiusKm = null,
                    category = ServiceCategory.SALON,
                    availability = ServiceAvailability.AVAILABLE,
                )
            }
        }

    @Test
    fun `initializes selectedCategory from persisted filter`() =
        runTest {
            val persistedCategoryFlow = flowOf(ServiceCategory.TECH.name)
            val mockPrefs = mockk<UserPreferences>(relaxed = true) {
                every { lastSelectedCategory } returns persistedCategoryFlow
            }
            val mockUseCase = mockk<GetMapPinsUseCase>()
            coEvery {
                mockUseCase(any(), any(), any(), any(), any())
            } returns Result.success(emptyList())

            val testViewModel = MapViewModel(mockUseCase, mockPrefs, notificationRepository, startPollingImmediately = false)

            assertEquals(ServiceCategory.TECH, testViewModel.uiState.value.selectedCategory)
        }

    @Test
    fun `selectAvailability updates availability filter and fetches pins`() =
        runTest {
            viewModel.selectAvailability(null)

            assertNull(viewModel.uiState.value.availability)
            coVerify {
                getMapPinsUseCase(
                    lat = null,
                    lng = null,
                    radiusKm = null,
                    category = null,
                    availability = null,
                )
            }
        }

    @Test
    fun `updateUserLocation updates coordinates in UI state`() {
        val latLng = LatLng(0.0515, 37.6456)
        viewModel.updateUserLocation(latLng)

        assertEquals(latLng, viewModel.uiState.value.userLocation)
    }

    @Test
    fun `pins are sorted by distance ascending when user location is set`() =
        runTest {
            val userLat = 0.0515
            val userLng = 37.6456

            // farPin is ~2 km north, nearPin is ~0.1 km north
            val nearPin = MapPin(
                serviceId = "1",
                providerId = "p1",
                providerName = "Near",
                providerPhotoUrl = null,
                serviceTitle = "Haircut",
                category = must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory.SALON,
                availability = must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability.AVAILABLE,
                averageRating = 4.5,
                lat = 0.0524,
                lng = 37.6456, // ~100 m away
            )
            val farPin = MapPin(
                serviceId = "2",
                providerId = "p2",
                providerName = "Far",
                providerPhotoUrl = null,
                serviceTitle = "Laundry",
                category = must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory.LAUNDRY,
                availability = must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability.AVAILABLE,
                averageRating = 4.0,
                lat = 0.0695,
                lng = 37.6456, // ~2 km away
            )

            // API returns far pin first, near pin second
            coEvery {
                getMapPinsUseCase(any(), any(), any(), any(), any())
            } returns Result.success(listOf(farPin, nearPin))

            viewModel.updateUserLocation(LatLng(userLat, userLng))
            viewModel.refreshPins()

            val pins = viewModel.uiState.value.pins
            assertEquals(2, pins.size)
            // After sorting, nearPin should be first
            assertEquals("1", pins[0].serviceId)
            assertEquals("2", pins[1].serviceId)
            // Distance values must be populated
            assertTrue(pins[0].distanceMeters != null)
            assertTrue(pins[0].distanceMeters!! < pins[1].distanceMeters!!)
        }

    @Test
    fun `fetchPins failure sets error state and clears loading`() =
        runTest {
            coEvery {
                getMapPinsUseCase(any(), any(), any(), any(), any())
            } returns Result.failure(RuntimeException("Network error"))

            viewModel.refreshPins()

            val state = viewModel.uiState.value
            assertEquals("Network error", state.error)
            assertEquals(false, state.isLoading)
        }

    @Test
    fun `haversineDistance returns zero for same point`() {
        val dist = viewModel.haversineDistance(0.0515, 37.6456, 0.0515, 37.6456)
        assertEquals(0.0, dist, 0.001)
    }
}
