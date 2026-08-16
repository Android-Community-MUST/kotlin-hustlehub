package must.kdroiders.hustlehub.ui.features.home.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.home.domain.model.SearchFilters
import must.kdroiders.hustlehub.ui.features.home.domain.model.SortOrder
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchServicesUseCaseTest {

    private lateinit var repository: ServiceRepository
    private lateinit var useCase: SearchServicesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SearchServicesUseCase(repository)
    }

    @Test
    fun `non-blank query routes to repository searchServices`() = runTest {
        val emptyPage = PageResponse<Service>(
            content = emptyList(),
            page = 0,
            size = 20,
            totalElements = 0,
            totalPages = 0,
        )
        coEvery { repository.searchServices("plumbing", page = 0, size = 20) } returns Result.success(emptyPage)

        val filters = SearchFilters()
        val result = useCase("  plumbing  ", filters, page = 0, size = 20)

        assertTrue(result.isSuccess)
        assertEquals(emptyPage, result.getOrNull())
        coVerify(exactly = 1) { repository.searchServices("plumbing", page = 0, size = 20) }
        coVerify(exactly = 0) { repository.browseServices(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `blank query with category filter routes to repository browseServices`() = runTest {
        val emptyPage = PageResponse<Service>(
            content = emptyList(),
            page = 0,
            size = 20,
            totalElements = 0,
            totalPages = 0,
        )
        coEvery {
            repository.browseServices(
                page = 0,
                size = 20,
                category = ServiceCategory.SALON,
                query = null,
                availability = null,
                minRating = null,
                maxPrice = null,
                lat = null,
                lng = null,
                sortBy = "RATING",
            )
        } returns Result.success(emptyPage)

        val filters = SearchFilters(
            categories = setOf("SALON"),
            sortOrder = SortOrder.RATING,
        )
        val result = useCase("   ", filters, page = 0, size = 20)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            repository.browseServices(
                page = 0,
                size = 20,
                category = ServiceCategory.SALON,
                query = null,
                availability = null,
                minRating = null,
                maxPrice = null,
                lat = null,
                lng = null,
                sortBy = "RATING",
            )
        }
    }
}
