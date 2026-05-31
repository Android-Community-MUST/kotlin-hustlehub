package must.kdroiders.hustlehub.domain.repository

import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.ServiceAvailability
import must.kdroiders.hustlehub.data.model.ServiceCategory

interface ServiceRepository {
    
    suspend fun createService(
        title: String,
        category: ServiceCategory,
        description: String?,
        priceRange: String?,
        portfolio: List<String>,
        openToBarter: Boolean,
        tags: List<String>
    ): Result<Service>

    suspend fun getServiceById(serviceId: String): Result<Service>

    suspend fun updateService(
        serviceId: String,
        title: String? = null,
        category: ServiceCategory? = null,
        description: String? = null,
        priceRange: String? = null,
        portfolio: List<String>? = null,
        openToBarter: Boolean? = null,
        tags: List<String>? = null
    ): Result<Service>

    suspend fun deleteService(serviceId: String): Result<Unit>

    suspend fun updateAvailability(
        serviceId: String,
        availability: ServiceAvailability
    ): Result<Service>

    suspend fun getMyServices(): Result<List<Service>>

    suspend fun browseServices(
        page: Int = 0,
        size: Int = 20,
        category: ServiceCategory? = null,
        query: String? = null
    ): Result<PageResponse<Service>>
}
