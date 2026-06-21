package must.kdroiders.hustlehub.ui.features.service.domain.repository

import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.ServiceAvailability
import must.kdroiders.hustlehub.data.model.ServiceCategory

/**
 * Contract for all service listing operations: create, read, update, delete,
 * availability toggling, and paginated browsing.
 */
interface ServiceRepository {
    /** Creates a new service listing for the currently authenticated provider. */
    suspend fun createService(
        title: String,
        category: ServiceCategory,
        description: String?,
        minPrice: Int,
        maxPrice: Int,
        openToBarter: Boolean,
        tags: List<String>,
    ): Result<Service>

    /**
     * Fetches a single service by its UUID.
     * Falls back to the Room cache if the network is unavailable.
     */
    suspend fun getServiceById(serviceId: String): Result<Service>

    /** Updates an existing service listing. Null parameters leave the field unchanged. */
    suspend fun updateService(
        serviceId: String,
        title: String? = null,
        category: ServiceCategory? = null,
        description: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        openToBarter: Boolean? = null,
        tags: List<String>? = null,
    ): Result<Service>

    /** Permanently deletes the service and removes it from the local cache. */
    suspend fun deleteService(serviceId: String): Result<Unit>

    /** Toggles the availability status of a service (AVAILABLE / UNAVAILABLE / BUSY). */
    suspend fun updateAvailability(
        serviceId: String,
        availability: ServiceAvailability,
    ): Result<Service>

    /**
     * Returns all services owned by the currently authenticated user.
     * Cache-first: stale entries (> 30 min) are evicted before the remote fetch.
     */
    suspend fun getMyServices(): Result<List<Service>>

    /**
     * Returns a paginated page of browsable services, optionally filtered by category,
     * availability, rating, price, and location. Falls back to the Room cache on page 0
     * if the network is unavailable.
     */
    suspend fun browseServices(
        page: Int = 0,
        size: Int = 20,
        category: ServiceCategory? = null,
        query: String? = null,
        availability: ServiceAvailability? = null,
        minRating: Double? = null,
        maxPrice: Int? = null,
        lat: Double? = null,
        lng: Double? = null,
        radiusKm: Double? = null,
        sortBy: String? = null,
    ): Result<PageResponse<Service>>

    /**
     * Full-text keyword search — hits GET /discovery/search?q=.
     * Results sorted by avgRating descending on the server.
     * No cache fallback (text search results are highly volatile).
     */
    suspend fun searchServices(
        query: String,
        page: Int = 0,
        size: Int = 20,
    ): Result<PageResponse<Service>>
}
