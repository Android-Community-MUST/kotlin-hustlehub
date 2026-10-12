package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import android.content.Context
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.core.telemetry.HustleAnalytics
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.media.domain.repository.StorageRepository
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.LocationDto
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetServiceByIdUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateServiceViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val serviceRepository: ServiceRepository = mockk(relaxed = true)
    private val storageRepository: StorageRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val getServiceById: GetServiceByIdUseCase = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val hustleAnalytics: HustleAnalytics = mockk(relaxed = true)
    private val hustleCrashlytics: HustleCrashlytics = mockk(relaxed = true)

    private lateinit var viewModel: CreateServiceViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { userPreferences.cachedUser } returns flowOf(User(isVerifiedPro = false))
        coEvery { getServiceById(any()) } returns Result.success(Service(id = "default"))

        viewModel = CreateServiceViewModel(
            serviceRepository = serviceRepository,
            storageRepository = storageRepository,
            context = context,
            getServiceById = getServiceById,
            userPreferences = userPreferences,
            hustleAnalytics = hustleAnalytics,
            hustleCrashlytics = hustleCrashlytics,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has default empty form values`() {
        val state = viewModel.uiState.value
        assertFalse(state.isEditMode)
        assertFalse(state.isProUser)
        assertEquals("", state.title)
        assertNull(state.category)
        assertEquals("", state.description)
        assertTrue(state.tags.isEmpty())
    }

    @Test
    fun `onTitleChange updates title and clears titleError`() {
        viewModel.onTitleChange("Laptop Repair")
        assertEquals("Laptop Repair", viewModel.uiState.value.title)
        assertNull(viewModel.uiState.value.titleError)
    }

    @Test
    fun `onCategoryChange updates selected category`() {
        viewModel.onCategoryChange(ServiceCategory.TECH)
        assertEquals(ServiceCategory.TECH, viewModel.uiState.value.category)
        assertNull(viewModel.uiState.value.categoryError)
    }

    @Test
    fun `addTag appends new tag to list`() {
        viewModel.onTagInputChange("hardware")
        viewModel.addTag()

        assertEquals(listOf("hardware"), viewModel.uiState.value.tags)
        assertEquals("", viewModel.uiState.value.tagInput)
    }

    @Test
    fun `loadForEdit pre-fills form fields with existing service data`() =
        runTest {
            val existingService = Service(
                id = "srv-100",
                title = "Existing Service",
                category = ServiceCategory.SALON,
                description = "Quality haircuts",
                priceRange = "150 - 300",
                tags = listOf("salon", "haircut"),
                availability = ServiceAvailability.AVAILABLE,
            )

            coEvery { getServiceById("srv-100") } returns Result.success(existingService)

            viewModel.loadForEdit("srv-100")

            val state = viewModel.uiState.value
            assertTrue(state.isEditMode)
            assertEquals("Existing Service", state.title)
            assertEquals(ServiceCategory.SALON, state.category)
            assertEquals("150", state.minPrice)
            assertEquals("300", state.maxPrice)
            assertEquals(listOf("salon", "haircut"), state.tags)
        }

    @Test
    fun `loadForEdit pre-fills location data from existing service`() =
        runTest {
            val existingService = Service(
                id = "srv-101",
                title = "Service with Custom Location",
                category = ServiceCategory.CLEANING,
                location = LocationDto(
                    lat = 0.1500,
                    lng = 37.7500,
                    label = "West Gate Apartment",
                ),
            )

            coEvery { getServiceById("srv-101") } returns Result.success(existingService)

            viewModel.loadForEdit("srv-101")

            val state = viewModel.uiState.value
            assertEquals(0.1500, state.selectedLat)
            assertEquals(37.7500, state.selectedLng)
            assertEquals("West Gate Apartment", state.locationLabel)
            assertEquals(LocationSelectionMode.MAP_PICKER, state.locationMode)
        }

    @Test
    fun `publish with valid data in create mode calls createService and emits Success`() =
        runTest {
            val created = Service(id = "srv-new", title = "Valid Title", category = ServiceCategory.TECH)
            coEvery {
                serviceRepository.createService(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
                )
            } returns Result.success(created)

            var emittedEvent: CreateServiceEvent? = null
            val job = backgroundScope.launch {
                viewModel.events.collect { emittedEvent = it }
            }

            viewModel.onTitleChange("Valid Title")
            viewModel.onCategoryChange(ServiceCategory.TECH)
            viewModel.onDescriptionChange("Professional tech services")
            viewModel.onMinPriceChange("200")
            viewModel.onMaxPriceChange("500")

            viewModel.publish()
            testScheduler.advanceUntilIdle()

            println("DEBUG TEST STATE: error=${viewModel.uiState.value.error}, loading=${viewModel.uiState.value.isLoading}, titleError=${viewModel.uiState.value.titleError}, event=$emittedEvent")
            assertTrue(emittedEvent is CreateServiceEvent.Success)
            assertEquals("Service created successfully!", (emittedEvent as CreateServiceEvent.Success).message)
            assertFalse(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.error)
            job.cancel()
        }

    @Test
    fun `publish with invalid data does not trigger network call and sets validation error`() =
        runTest {
            viewModel.onTitleChange("") // Blank title
            viewModel.publish()

            io.mockk.coVerify(exactly = 0) { serviceRepository.createService(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
            assertEquals("Title is required", viewModel.uiState.value.titleError)
        }

    @Test
    fun `publish failure sets userFriendly error in uiState`() =
        runTest {
            coEvery {
                serviceRepository.createService(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } returns Result.failure(RuntimeException("Network timeout"))

            viewModel.onTitleChange("Valid Title")
            viewModel.onCategoryChange(ServiceCategory.TECH)
            viewModel.onDescriptionChange("Professional tech services")
            viewModel.onMinPriceChange("200")
            viewModel.onMaxPriceChange("500")

            viewModel.publish()

            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals("Network timeout", viewModel.uiState.value.error)
        }
}
