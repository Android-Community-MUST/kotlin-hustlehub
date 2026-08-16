package must.kdroiders.hustlehub.ui.features.service.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.core.auth.AuthManager
import must.kdroiders.hustlehub.ui.features.service.data.local.dao.ServiceDao
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.ServiceEntity
import must.kdroiders.hustlehub.ui.features.service.data.remote.ServiceApiService
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.ServiceResponse
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceRepositoryTest {

    private lateinit var apiService: ServiceApiService
    private lateinit var serviceDao: ServiceDao
    private lateinit var authManager: AuthManager
    private lateinit var repository: ServiceRepositoryImpl

    @Before
    fun setup() {
        apiService = mockk(relaxed = true)
        serviceDao = mockk(relaxed = true)
        authManager = mockk(relaxed = true)
        repository = ServiceRepositoryImpl(apiService, serviceDao, authManager)
    }

    @Test
    fun `getServiceById returns cached local entity when network fails`() = runTest {
        val cachedEntity = ServiceEntity(
            id = "serv-101",
            providerId = "prov-1",
            title = "Car Wash",
            category = "SALON",
            description = "Quick wash",
            priceRange = "KSh 500 - KSh 1000",
            averageRating = 4.5f,
            reviewCount = 10,
            availability = "AVAILABLE",
            openToBarter = false,
            portfolioJson = "[]",
            tagsJson = "[]",
            iconUrl = "",
            lastUpdated = System.currentTimeMillis(),
        )

        coEvery { apiService.getServiceById("serv-101") } throws RuntimeException("Network error")
        coEvery { serviceDao.getServiceById("serv-101") } returns cachedEntity

        val result = repository.getServiceById("serv-101")

        assertTrue(result.isSuccess)
        assertEquals("serv-101", result.getOrNull()?.id)
        assertEquals("Car Wash", result.getOrNull()?.title)
        coVerify(exactly = 1) { serviceDao.getServiceById("serv-101") }
    }

    @Test
    fun `getServiceById fetches from API when available`() = runTest {
        val remoteResponse = ServiceResponse(
            serviceId = "serv-102",
            providerId = "prov-2",
            title = "Hair Styling",
            category = "SALON",
            description = "Braids & Locs",
            priceRange = "KSh 300 - KSh 800",
            availability = "AVAILABLE",
            openToBarter = true,
            avgRating = 4.8,
            reviewCount = 5,
            portfolioImages = emptyList(),
            tags = emptyList(),
            location = null,
            distanceMeters = null,
            createdAt = "2026-08-16T00:00:00Z",
            updatedAt = "2026-08-16T00:00:00Z",
        )
        val apiResponse = ApiResponse(success = true, message = "Success", data = remoteResponse)

        coEvery { apiService.getServiceById("serv-102") } returns apiResponse

        val result = repository.getServiceById("serv-102")

        assertTrue(result.isSuccess)
        assertEquals("serv-102", result.getOrNull()?.id)
        assertEquals("Hair Styling", result.getOrNull()?.title)
        coVerify(exactly = 1) { serviceDao.upsert(any()) }
    }

    @Test
    fun `deleteService calls apiService delete and removes local entity`() = runTest {
        val apiResponse = ApiResponse<Unit>(success = true, message = "Deleted")
        coEvery { apiService.deleteService("serv-103") } returns apiResponse

        val result = repository.deleteService("serv-103")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { apiService.deleteService("serv-103") }
        coVerify(exactly = 1) { serviceDao.deleteById("serv-103") }
    }

    @Test
    fun `updateAvailability calls apiService updateAvailability and updates local entity`() = runTest {
        val remoteResponse = ServiceResponse(
            serviceId = "serv-104",
            providerId = "prov-1",
            title = "Plumbing",
            category = "SALON",
            description = "Pipe repair",
            priceRange = "KSh 500",
            availability = "BUSY",
            openToBarter = false,
            avgRating = 4.0,
            reviewCount = 2,
            portfolioImages = emptyList(),
            tags = emptyList(),
            location = null,
            distanceMeters = null,
            createdAt = "2026-08-16T00:00:00Z",
            updatedAt = "2026-08-16T00:00:00Z",
        )
        val apiResponse = ApiResponse(success = true, message = "Updated", data = remoteResponse)
        coEvery { apiService.updateAvailability("serv-104", any()) } returns apiResponse

        val result = repository.updateAvailability("serv-104", ServiceAvailability.BUSY)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { apiService.updateAvailability("serv-104", any()) }
    }
}
