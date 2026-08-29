package must.kdroiders.hustlehub.ui.features.service.presentation.viewmodel

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import must.kdroiders.hustlehub.ui.features.media.domain.repository.StorageRepository
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.DeleteServiceUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.GetMyServicesUseCase
import must.kdroiders.hustlehub.ui.features.service.domain.usecase.UpdateAvailabilityUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyServicesViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val getMyServices: GetMyServicesUseCase = mockk(relaxed = true)
    private val deleteServiceUseCase: DeleteServiceUseCase = mockk(relaxed = true)
    private val updateAvailability: UpdateAvailabilityUseCase = mockk(relaxed = true)
    private val storageRepository: StorageRepository = mockk(relaxed = true)
    private val serviceRepository: ServiceRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var viewModel: MyServicesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val mockServices = listOf(
            Service(id = "srv-1", title = "Laptop Repair", availability = ServiceAvailability.AVAILABLE),
            Service(id = "srv-2", title = "Haircut", availability = ServiceAvailability.BUSY),
        )
        coEvery { getMyServices() } returns Result.success(mockServices)

        viewModel = MyServicesViewModel(
            getMyServices = getMyServices,
            deleteService = deleteServiceUseCase,
            updateAvailability = updateAvailability,
            storageRepository = storageRepository,
            serviceRepository = serviceRepository,
            context = context,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadServices updates uiState with provider services`() =
        runTest {
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(2, state.services.size)
            assertEquals("Laptop Repair", state.services[0].title)
        }

    @Test
    fun `onAvailabilityChange updates service availability optimistically`() =
        runTest {
            val updatedService = Service(id = "srv-1", title = "Laptop Repair", availability = ServiceAvailability.BUSY)
            coEvery { updateAvailability("srv-1", ServiceAvailability.BUSY) } returns Result.success(updatedService)

            viewModel.onAvailabilityChange("srv-1", ServiceAvailability.BUSY)

            coVerify(exactly = 1) { updateAvailability("srv-1", ServiceAvailability.BUSY) }
            val serviceInState = viewModel.uiState.value.services
                .find { it.id == "srv-1" }
            assertEquals(ServiceAvailability.BUSY, serviceInState?.availability)
        }

    @Test
    fun `confirmDelete removes service from list on success`() =
        runTest {
            coEvery { deleteServiceUseCase.invoke("srv-1") } returns Result.success(Unit)

            viewModel.requestDelete("srv-1")
            viewModel.confirmDelete()

            coVerify(exactly = 1) { deleteServiceUseCase.invoke("srv-1") }
            val remaining = viewModel.uiState.value.services
            assertEquals(1, remaining.size)
            assertEquals("srv-2", remaining[0].id)
        }
}
