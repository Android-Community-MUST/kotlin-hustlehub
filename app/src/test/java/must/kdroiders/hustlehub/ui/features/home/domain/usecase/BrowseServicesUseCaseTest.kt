package must.kdroiders.hustlehub.ui.features.home.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseServicesUseCaseTest {
    private val serviceRepository: ServiceRepository = mockk(relaxed = true)
    private lateinit var useCase: BrowseServicesUseCase

    @Before
    fun setup() {
        useCase = BrowseServicesUseCase(serviceRepository)
    }

    @Test
    fun `invoke passes category and query parameters to serviceRepository`() =
        runTest {
            val pageResponse = PageResponse(
                content = listOf(
                    Service(id = "srv-1", category = ServiceCategory.TECH, averageRating = 4.9f),
                ),
                page = 0,
                size = 20,
                totalElements = 1,
                totalPages = 1,
            )

            coEvery {
                serviceRepository.browseServices(
                    page = 0,
                    size = 20,
                    category = ServiceCategory.TECH,
                    query = "repair",
                )
            } returns Result.success(pageResponse)

            val result = useCase(
                page = 0,
                size = 20,
                category = ServiceCategory.TECH,
                query = "repair",
            )

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull()?.content?.size)
            coVerify(exactly = 1) {
                serviceRepository.browseServices(
                    page = 0,
                    size = 20,
                    category = ServiceCategory.TECH,
                    query = "repair",
                )
            }
        }
}
