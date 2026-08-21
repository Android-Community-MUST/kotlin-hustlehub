package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateAvailabilityUseCaseTest {

    private val repository: ServiceRepository = mockk(relaxed = true)
    private lateinit var useCase: UpdateAvailabilityUseCase

    @Before
    fun setup() {
        useCase = UpdateAvailabilityUseCase(repository)
    }

    @Test
    fun `invoke delegates serviceId and availability to repository`() = runTest {
        val updated = Service(id = "srv-1", availability = ServiceAvailability.BUSY)
        coEvery { repository.updateAvailability("srv-1", ServiceAvailability.BUSY) } returns Result.success(updated)

        val result = useCase("srv-1", ServiceAvailability.BUSY)

        assertTrue(result.isSuccess)
        assertEquals(ServiceAvailability.BUSY, result.getOrNull()?.availability)
        coVerify(exactly = 1) { repository.updateAvailability("srv-1", ServiceAvailability.BUSY) }
    }
}
