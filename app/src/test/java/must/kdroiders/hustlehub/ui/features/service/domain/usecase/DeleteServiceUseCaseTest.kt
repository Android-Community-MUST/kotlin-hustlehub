package must.kdroiders.hustlehub.ui.features.service.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteServiceUseCaseTest {

    private val repository: ServiceRepository = mockk(relaxed = true)
    private lateinit var useCase: DeleteServiceUseCase

    @Before
    fun setup() {
        useCase = DeleteServiceUseCase(repository)
    }

    @Test
    fun `invoke delegates serviceId deletion to repository`() = runTest {
        coEvery { repository.deleteService("srv-1") } returns Result.success(Unit)

        val result = useCase("srv-1")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deleteService("srv-1") }
    }
}
