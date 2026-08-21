package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetServiceByIdUseCaseTest {

    private val repository: ServiceRepository = mockk(relaxed = true)
    private lateinit var useCase: GetServiceByIdUseCase

    @Before
    fun setup() {
        useCase = GetServiceByIdUseCase(repository)
    }

    @Test
    fun `invoke delegates serviceId to repository`() = runTest {
        val expected = Service(id = "srv-1", title = "Laptop Repair")
        coEvery { repository.getServiceById("srv-1") } returns Result.success(expected)

        val result = useCase("srv-1")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        coVerify(exactly = 1) { repository.getServiceById("srv-1") }
    }
}
