package must.kdroiders.hustlehub.ui.features.service.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.core.auth.AuthManager
import must.kdroiders.hustlehub.data.model.Service
import must.kdroiders.hustlehub.data.model.ServiceAvailability
import must.kdroiders.hustlehub.data.model.ServiceCategory
import must.kdroiders.hustlehub.data.remote.ServiceApiService
import must.kdroiders.hustlehub.data.remote.dto.AvailabilityRequest
import must.kdroiders.hustlehub.data.remote.dto.CreateServiceRequest
import must.kdroiders.hustlehub.data.remote.dto.ServiceResponse
import must.kdroiders.hustlehub.data.remote.dto.UpdateServiceRequest
import must.kdroiders.hustlehub.ui.features.service.data.local.dao.ServiceDao
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.toDomain
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.toEntity
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import timber.log.Timber

// Cache TTL — entries older than this are evicted before each read
private const val CACHE_TTL_MS = 30 * 60 * 1_000L

/**
 * Concrete implementation of [ServiceRepository].
 *
 * Coordinates between the HustleHub REST backend ([ServiceApiService]) and
 * the local Room cache ([ServiceDao]). Room is the single source of truth for
 * the UI; data flows from cache first, refreshed from remote on each call.
 */
class ServiceRepositoryImpl(
    private val apiService: ServiceApiService,
    private val serviceDao: ServiceDao,
    private val authManager: AuthManager,
) : ServiceRepository {
    override suspend fun createService(
        title: String,
        category: ServiceCategory,
        description: String?,
        minPrice: Int,
        maxPrice: Int,
        openToBarter: Boolean,
        tags: List<String>,
    ): Result<Service> =
        withContext(Dispatchers.IO) {
            try {
                val request = CreateServiceRequest(
                    title = title,
                    category = category.name,
                    description = description,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    openToBarter = openToBarter,
                    tags = tags,
                )
                val response = apiService.createService(request)
                if (response.success && response.data != null) {
                    val service = response.data.toDomainModel()
                    // Write-through: cache the newly created service immediately
                    serviceDao.upsert(service.toEntity())
                    Result.success(service)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getServiceById(serviceId: String): Result<Service> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getServiceById(serviceId)
                if (response.success && response.data != null) {
                    val service = response.data.toDomainModel()
                    serviceDao.upsert(service.toEntity())
                    Result.success(service)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                // Network unavailable — try cache
                Timber.w(e, "getServiceById network miss, checking cache")
                val cached = serviceDao.getServiceById(serviceId)
                if (cached != null) {
                    Result.success(cached.toDomain())
                } else {
                    Result.failure(e)
                }
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
        tags: List<String>?,
    ): Result<Service> =
        withContext(Dispatchers.IO) {
            try {
                val request = UpdateServiceRequest(
                    title = title,
                    category = category?.name,
                    description = description,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    openToBarter = openToBarter,
                    tags = tags,
                )
                val response = apiService.updateService(serviceId, request)
                if (response.success && response.data != null) {
                    val service = response.data.toDomainModel()
                    serviceDao.upsert(service.toEntity())
                    Result.success(service)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteService(serviceId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteService(serviceId)
                if (response.success) {
                    // Remove from cache immediately on confirmed delete
                    serviceDao.deleteById(serviceId)
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
        availability: ServiceAvailability,
    ): Result<Service> =
        withContext(Dispatchers.IO) {
            try {
                val request = AvailabilityRequest(availability = availability.name)
                val response = apiService.updateAvailability(serviceId, request)
                if (response.success && response.data != null) {
                    val service = response.data.toDomainModel()
                    serviceDao.upsert(service.toEntity())
                    Result.success(service)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getMyServices(): Result<List<Service>> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            // Evict anything older than 30 minutes before serving cache
            serviceDao.deleteStaleEntries(now - CACHE_TTL_MS)

            try {
                val response = apiService.getMyServices()
                if (response.success && response.data != null) {
                    val services = response.data.map { it.toDomainModel() }
                    // Refresh cache with latest data
                    serviceDao.upsertAll(services.map { it.toEntity(now) })
                    Result.success(services)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                // Network unavailable — return only the current user's services from cache
                Timber.w(e, "getMyServices network miss, serving cache")
                val currentUser = authManager.currentUser()?.uid ?: ""
                val cached = serviceDao.getServicesByProvider(currentUser)
                if (cached.isNotEmpty()) {
                    Result.success(cached.map { it.toDomain() })
                } else {
                    Result.failure(e)
                }
            }
        }

    override suspend fun browseServices(
        page: Int,
        size: Int,
        category: ServiceCategory?,
        query: String?,
        availability: ServiceAvailability?,
        minRating: Double?,
        maxPrice: Int?,
        lat: Double?,
        lng: Double?,
        radiusKm: Double?,
        sortBy: String?,
    ): Result<PageResponse<Service>> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            serviceDao.deleteStaleEntries(now - CACHE_TTL_MS)

            try {
                val categoryStr = if (category == ServiceCategory.ALL) null else category?.name
                val response = apiService.browseServices(
                    page = page,
                    size = size,
                    category = categoryStr,
                    query = query,
                    availability = availability?.name,
                    minRating = minRating,
                    maxPrice = maxPrice,
                    lat = lat,
                    lng = lng,
                    radiusKm = radiusKm,
                    sortBy = sortBy,
                )
                if (response.success && response.data != null) {
                    val pageData = response.data
                    val services = pageData.content.map { it.toDomainModel() }
                    if (page == 0) serviceDao.upsertAll(services.map { it.toEntity(now) })
                    Result.success(
                        PageResponse(
                            content = services,
                            page = pageData.page,
                            size = pageData.size,
                            totalElements = pageData.totalElements,
                            totalPages = pageData.totalPages,
                        ),
                    )
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Timber.w(e, "browseServices network miss, serving cache (page $page)")
                if (page == 0) {
                    val cached = serviceDao.getAllServices()
                    if (cached.isNotEmpty()) {
                        val services = cached.map { it.toDomain() }
                        Result.success(
                            PageResponse(
                                content = services,
                                page = 0,
                                size = services.size,
                                totalElements = services.size.toLong(),
                                totalPages = 1,
                            ),
                        )
                    } else {
                        Result.failure(e)
                    }
                } else {
                    Result.failure(e)
                }
            }
        }

    override suspend fun searchServices(
        query: String,
        page: Int,
        size: Int,
    ): Result<PageResponse<Service>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchServices(query = query, page = page, size = size)
                if (response.success && response.data != null) {
                    val pageData = response.data
                    val services = pageData.content.map { it.toDomainModel() }
                    Result.success(
                        PageResponse(
                            content = services,
                            page = pageData.page,
                            size = pageData.size,
                            totalElements = pageData.totalElements,
                            totalPages = pageData.totalPages,
                        ),
                    )
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Timber.w(e, "searchServices failed for query='$query'")
                Result.failure(e)
            }
        }
}

// DTO → Domain mapper (private to this file)
private fun ServiceResponse.toDomainModel(): Service =
    Service(
        id = serviceId,
        providerId = providerId,
        title = title,
        category = runCatching { ServiceCategory.valueOf(category) }.getOrDefault(ServiceCategory.OTHER),
        description = description ?: "",
        priceRange = priceRange,
        portfolio = portfolioImages ?: emptyList(),
        availability = runCatching {
            ServiceAvailability.valueOf(availability)
        }.getOrDefault(ServiceAvailability.AVAILABLE),
        averageRating = avgRating.toFloat(),
        reviewCount = reviewCount,
        openToBarter = openToBarter,
        tags = tags ?: emptyList(),
        createdAt = 0L,
        updatedAt = 0L,
        iconUrl = portfolioImages?.firstOrNull() ?: "",
    )
