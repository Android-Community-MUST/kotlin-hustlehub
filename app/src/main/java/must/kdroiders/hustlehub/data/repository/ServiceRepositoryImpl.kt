package must.kdroiders.hustlehub.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.ServiceAvailability
import must.kdroiders.hustlehub.data.model.ServiceCategory
import must.kdroiders.hustlehub.data.remote.ServiceApiService
import must.kdroiders.hustlehub.data.remote.dto.CreateServiceRequest
import must.kdroiders.hustlehub.data.remote.dto.ServiceResponse
import must.kdroiders.hustlehub.data.remote.dto.AvailabilityRequest
import must.kdroiders.hustlehub.data.remote.dto.UpdateServiceRequest
import must.kdroiders.hustlehub.domain.repository.ServiceRepository

class ServiceRepositoryImpl(
    private val apiService: ServiceApiService
) : ServiceRepository {

    override suspend fun createService(
        title: String,
        category: ServiceCategory,
        description: String?,
        minPrice: Int,
        maxPrice: Int,
        openToBarter: Boolean,
        tags: List<String>
    ): Result<Service> = withContext(Dispatchers.IO) {
        try {
            val request = CreateServiceRequest(
                title = title,
                category = category.name,
                description = description,
                minPrice = minPrice,
                maxPrice = maxPrice,
                openToBarter = openToBarter,
                tags = tags
            )
            val response = apiService.createService(request)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomainModel())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServiceById(serviceId: String): Result<Service> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getServiceById(serviceId)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomainModel())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateService(
        serviceId: String,
        title: String?,
        category: ServiceCategory?,
        description: String?,
        minPrice: Int?,
        maxPrice: Int?,
        openToBarter: Boolean?,
        tags: List<String>?
    ): Result<Service> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateServiceRequest(
                title = title,
                category = category?.name,
                description = description,
                minPrice = minPrice,
                maxPrice = maxPrice,
                openToBarter = openToBarter,
                tags = tags
            )
            val response = apiService.updateService(serviceId, request)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomainModel())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteService(serviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteService(serviceId)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAvailability(
        serviceId: String,
        availability: ServiceAvailability
    ): Result<Service> = withContext(Dispatchers.IO) {
        try {
            val request = AvailabilityRequest(availability = availability.name)
            val response = apiService.updateAvailability(serviceId, request)
            if (response.success && response.data != null) {
                Result.success(response.data.toDomainModel())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyServices(): Result<List<Service>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyServices()
            if (response.success && response.data != null) {
                Result.success(response.data.map { it.toDomainModel() })
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun browseServices(
        page: Int,
        size: Int,
        category: ServiceCategory?,
        query: String?
    ): Result<PageResponse<Service>> = withContext(Dispatchers.IO) {
        try {
            // ALL means no category filter
            val categoryStr = if (category == ServiceCategory.ALL) null else category?.name
            val response = apiService.browseServices(
                page = page,
                size = size,
                category = categoryStr,
                query = query
            )
            if (response.success && response.data != null) {
                val pageData = response.data
                val mappedPage = PageResponse(
                    content = pageData.content.map { it.toDomainModel() },
                    page = pageData.page,
                    size = pageData.size,
                    totalElements = pageData.totalElements,
                    totalPages = pageData.totalPages
                )
                Result.success(mappedPage)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun ServiceResponse.toDomainModel(): Service {
    return Service(
        id = this.serviceId,
        providerId = this.providerId,
        title = this.title,
        category = parseCategory(this.category),
        description = this.description ?: "",
        priceRange = this.priceRange,
        portfolio = this.portfolioImages ?: emptyList(),
        availability = parseAvailability(this.availability),
        averageRating = this.avgRating.toFloat(),
        reviewCount = this.reviewCount,
        openToBarter = this.openToBarter,
        tags = this.tags ?: emptyList(),
        // Simple mapping, proper date parsing could be added later
        createdAt = 0L, 
        updatedAt = 0L,
        iconUrl = this.portfolioImages?.firstOrNull() ?: ""
    )
}

private fun parseCategory(name: String): ServiceCategory {
    return try {
        ServiceCategory.valueOf(name)
    } catch (e: IllegalArgumentException) {
        ServiceCategory.OTHER
    }
}

private fun parseAvailability(name: String): ServiceAvailability {
    return try {
        ServiceAvailability.valueOf(name)
    } catch (e: IllegalArgumentException) {
        ServiceAvailability.AVAILABLE
    }
}
