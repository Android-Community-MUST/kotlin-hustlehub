package must.kdroiders.hustlehub.ui.features.service.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.core.auth.AuthManager
import must.kdroiders.hustlehub.ui.features.service.data.local.dao.ServiceDao
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.toDomain
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.toEntity
import must.kdroiders.hustlehub.ui.features.service.data.remote.ServiceApiService
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.AvailabilityRequest
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.CreateReviewRequest
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.CreateServiceRequest
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.LocationDto
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.ReviewResponse
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.ServiceResponse
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.UpdateServiceRequest
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** 30-minute cache TTL before cache entries are considered stale. */
private const val CACHE_TTL_MS = 30 * 60 * 1000L

@Singleton
class ServiceRepositoryImpl
    @Inject
    constructor(
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
            portfolioUrls: List<String>,
            lat: Double?,
            lng: Double?,
            locationLabel: String?,
        ): Result<Service> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val locationDto = if (lat != null && lng != null) {
                        LocationDto(lat = lat, lng = lng, label = locationLabel)
                    } else {
                        null
                    }

                    val request = CreateServiceRequest(
                        title = title,
                        category = category.name,
                        description = description,
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        openToBarter = openToBarter,
                        tags = tags,
                        location = locationDto,
                        portfolioUrls = portfolioUrls,
                    )
                    val response = apiService.createService(request)
                    check(response.success && response.data != null) { response.message ?: "Failed to create service" }
                    val service = response.data.toDomainModel()
                    serviceDao.upsert(service.toEntity())
                    service
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                }
            }

        override suspend fun getServiceById(serviceId: String): Result<Service> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = apiService.getServiceById(serviceId)
                    check(response.success && response.data != null) { response.message ?: "Failed to fetch service" }
                    val service = response.data.toDomainModel()
                    serviceDao.upsert(service.toEntity())
                    service
                }.recoverCatching { e ->
                    if (e is CancellationException) throw e
                    Timber.w(e, "getServiceById network miss, checking cache")
                    val cached = serviceDao.getServiceById(serviceId)
                    cached?.toDomain() ?: throw e
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
            portfolioUrls: List<String>?,
            lat: Double?,
            lng: Double?,
            locationLabel: String?,
        ): Result<Service> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val locationDto = if (lat != null && lng != null) {
                        LocationDto(lat = lat, lng = lng, label = locationLabel)
                    } else {
                        null
                    }

                    val request = UpdateServiceRequest(
                        title = title,
                        category = category?.name,
                        description = description,
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        openToBarter = openToBarter,
                        tags = tags,
                        location = locationDto,
                        portfolioUrls = portfolioUrls,
                    )
                    val response = apiService.updateService(serviceId, request)
                    check(response.success && response.data != null) { response.message ?: "Failed to update service" }
                    val service = response.data.toDomainModel()
                    serviceDao.upsert(service.toEntity())
                    service
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                }
            }

        override suspend fun deleteService(serviceId: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = apiService.deleteService(serviceId)
                    check(response.success) { response.message ?: "Failed to delete service" }
                    serviceDao.deleteById(serviceId)
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                }
            }

        override suspend fun updateAvailability(
            serviceId: String,
            availability: ServiceAvailability,
        ): Result<Service> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val request = AvailabilityRequest(availability = availability.name)
                    val response = apiService.updateAvailability(serviceId, request)
                    check(response.success && response.data != null) { response.message ?: "Failed to update availability" }
                    val service = response.data.toDomainModel()
                    serviceDao.upsert(service.toEntity())
                    service
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                }
            }

        override suspend fun getMyServices(): Result<List<Service>> =
            withContext(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                serviceDao.deleteStaleEntries(now - CACHE_TTL_MS)

                runCatching {
                    val response = apiService.getMyServices()
                    check(response.success && response.data != null) { response.message ?: "Failed to fetch my services" }
                    val services = response.data.map { it.toDomainModel() }
                    serviceDao.upsertAll(services.map { it.toEntity(now) })
                    services
                }.recoverCatching { e ->
                    if (e is CancellationException) throw e
                    Timber.w(e, "getMyServices network miss, serving cache")
                    val currentUser = authManager.currentUser()?.uid ?: ""
                    val cached = serviceDao.getServicesByProvider(currentUser)
                    if (cached.isNotEmpty()) {
                        cached.map { it.toDomain() }
                    } else {
                        throw e
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

                runCatching {
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
                    check(response.success && response.data != null) { response.message ?: "Failed to browse services" }
                    val pageData = response.data
                    val services = pageData.content.map { it.toDomainModel() }
                    if (page == 0) serviceDao.upsertAll(services.map { it.toEntity(now) })
                    PageResponse(
                        content = services,
                        page = pageData.page,
                        size = pageData.size,
                        totalElements = pageData.totalElements,
                        totalPages = pageData.totalPages,
                    )
                }.recoverCatching { e ->
                    if (e is CancellationException) throw e
                    Timber.w(e, "browseServices network miss, serving cache (page $page)")
                    if (page == 0) {
                        val cached = serviceDao.getAllServices()
                        if (cached.isNotEmpty()) {
                            val services = cached.map { it.toDomain() }
                            PageResponse(
                                content = services,
                                page = 0,
                                size = services.size,
                                totalElements = services.size.toLong(),
                                totalPages = 1,
                            )
                        } else {
                            throw e
                        }
                    } else {
                        throw e
                    }
                }
            }

        override suspend fun searchServices(
            query: String,
            page: Int,
            size: Int,
        ): Result<PageResponse<Service>> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = apiService.searchServices(query = query, page = page, size = size)
                    check(response.success && response.data != null) { response.message ?: "Failed to search services" }
                    val pageData = response.data
                    val services = pageData.content.map { it.toDomainModel() }
                    PageResponse(
                        content = services,
                        page = pageData.page,
                        size = pageData.size,
                        totalElements = pageData.totalElements,
                        totalPages = pageData.totalPages,
                    )
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.w(e, "searchServices failed for query='$query'")
                }
            }

        override suspend fun getServiceReviews(
            serviceId: String,
            page: Int,
            size: Int,
        ): Result<PageResponse<Review>> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = apiService.getServiceReviews(serviceId, page, size)
                    check(response.success && response.data != null) { response.message ?: "Failed to fetch reviews" }
                    val pageData = response.data
                    PageResponse(
                        content = pageData.content.map { it.toDomain() },
                        page = pageData.page,
                        size = pageData.size,
                        totalElements = pageData.totalElements,
                        totalPages = pageData.totalPages,
                    )
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.w(e, "getServiceReviews failed for serviceId='$serviceId'")
                }
            }

        override suspend fun submitReview(
            serviceId: String,
            rating: Int,
            comment: String?,
            isAnonymous: Boolean,
        ): Result<Review> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val request = CreateReviewRequest(
                        serviceId = serviceId,
                        rating = rating,
                        comment = comment,
                        isAnonymous = isAnonymous,
                    )
                    val response = apiService.submitReview(request)
                    check(response.success && response.data != null) { response.message }
                    response.data.toDomain()
                }.recoverCatching { e ->
                    if (e is CancellationException) throw e
                    if (e is retrofit2.HttpException && e.code() == 409) {
                        throw Exception("You have already reviewed this service.")
                    } else {
                        Timber.w(e, "submitReview failed for serviceId='$serviceId'")
                        throw e
                    }
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
        location = location,
    )

private fun ReviewResponse.toDomain(): Review =
    Review(
        id = id,
        serviceId = serviceId,
        providerId = providerId,
        customerId = customerId,
        customerName = if (isAnonymous) "Anonymous" else customerName,
        customerAvatarUrl = if (isAnonymous) "" else (customerAvatarUrl ?: ""),
        rating = rating,
        comment = comment,
        isAnonymous = isAnonymous,
        createdAt = runCatching {
            Instant.parse(createdAt).toEpochMilli()
        }.getOrDefault(0L),
    )
