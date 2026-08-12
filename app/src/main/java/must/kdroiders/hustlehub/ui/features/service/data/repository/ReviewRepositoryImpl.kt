package must.kdroiders.hustlehub.ui.features.service.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.ui.features.service.data.remote.ServiceApiService
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.CreateReviewRequest
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.ReviewResponse
import must.kdroiders.hustlehub.ui.features.service.domain.model.Review
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import retrofit2.HttpException
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

import must.kdroiders.hustlehub.ui.features.service.data.local.dao.ReviewDao
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.toDomain
import must.kdroiders.hustlehub.ui.features.service.data.local.entity.toEntity

@Singleton
class ReviewRepositoryImpl
    @Inject
    constructor(
        private val apiService: ServiceApiService,
        private val userPreferences: UserPreferences,
        private val reviewDao: ReviewDao,
    ) : ReviewRepository {
        override suspend fun submitReview(
            serviceId: String,
            rating: Int,
            comment: String?,
            isAnonymous: Boolean,
        ): Result<Review> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val request = CreateReviewRequest(
                        rating = rating,
                        comment = comment,
                        isAnonymous = isAnonymous,
                    )
                    val response = apiService.submitReview(serviceId, request)
                    check(response.success && response.data != null) { response.message }
                    response.data.toDomain()
                }.recoverCatching { e ->
                    if (e is CancellationException) throw e
                    if (e is HttpException && (e.code() == 409 || e.code() == 400)) {
                        throw Exception("You have already reviewed this service.")
                    } else {
                        Timber.w(e, "ReviewRepositoryImpl.submitReview failed for serviceId='$serviceId'")
                        throw e
                    }
                }
            }

        override suspend fun getReviewsForService(
            serviceId: String,
            page: Int,
            size: Int,
        ): Result<PageResponse<Review>> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = apiService.getServiceReviews(serviceId, page, size)
                    check(response.success && response.data != null) { response.message ?: "Failed to fetch reviews" }
                    val pageData = response.data
                    val reviews = pageData.content.map { it.toDomain() }
                    reviewDao.upsertAll(reviews.map { it.toEntity() })
                    PageResponse(
                        content = reviews,
                        page = pageData.page,
                        size = pageData.size,
                        totalElements = pageData.totalElements,
                        totalPages = pageData.totalPages,
                    )
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.w(e, "ReviewRepositoryImpl.getReviewsForService network miss for serviceId='$serviceId'")
                }
            }

        override suspend fun checkDuplicateReview(serviceId: String): Result<Boolean> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val currentUser = userPreferences.cachedUser.firstOrNull()
                    if (currentUser == null || currentUser.id.isBlank()) {
                        return@runCatching false
                    }
                    val response = apiService.getServiceReviews(serviceId, page = 0, size = 50)
                    check(response.success && response.data != null) { response.message ?: "Failed to fetch reviews" }
                    response.data.content.any { it.customerId == currentUser.id }
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    Timber.w(e, "ReviewRepositoryImpl.checkDuplicateReview failed for serviceId='$serviceId'")
                }
            }
    }

private fun ReviewResponse.toDomain(): Review =
    Review(
        id = id,
        serviceId = serviceId,
        providerId = providerId ?: "",
        customerId = customerId ?: "",
        customerName = if (isAnonymous) "Anonymous" else (customerName ?: "Student"),
        customerAvatarUrl = if (isAnonymous) "" else (customerAvatarUrl ?: ""),
        rating = rating,
        comment = comment,
        isAnonymous = isAnonymous,
        createdAt = runCatching {
            Instant.parse(createdAt).toEpochMilli()
        }.getOrDefault(0L),
    )
