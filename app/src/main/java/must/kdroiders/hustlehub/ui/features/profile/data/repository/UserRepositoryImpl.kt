package must.kdroiders.hustlehub.ui.features.profile.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import must.kdroiders.hustlehub.core.utils.ImageCompressor
import must.kdroiders.hustlehub.ui.features.auth.data.remote.AuthApiService
import must.kdroiders.hustlehub.ui.features.auth.data.remote.RegisterRequest
import must.kdroiders.hustlehub.ui.features.auth.data.remote.UserResponseDto
import must.kdroiders.hustlehub.ui.features.media.data.remote.MediaApiService
import must.kdroiders.hustlehub.ui.features.profile.data.local.dao.UserDao
import must.kdroiders.hustlehub.ui.features.profile.data.local.entity.toDomain
import must.kdroiders.hustlehub.ui.features.profile.data.local.entity.toEntity
import must.kdroiders.hustlehub.ui.features.profile.data.remote.FcmTokenRequest
import must.kdroiders.hustlehub.ui.features.profile.data.remote.LocationUpdateRequest
import must.kdroiders.hustlehub.ui.features.profile.data.remote.UpdateProfileRequest
import must.kdroiders.hustlehub.ui.features.profile.data.remote.UserApiService
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.service.data.remote.ServiceApiService
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.ServiceResponse
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [UserRepository].
 *
 * Coordinates profile reads/writes between the HustleHub REST backend
 * and the Supabase media service.
 */
@Singleton
class UserRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val authApiService: AuthApiService,
        private val userApiService: UserApiService,
        private val mediaApiService: MediaApiService,
        private val serviceApiService: ServiceApiService,
        private val userDao: UserDao,
    ) : UserRepository {
        override suspend fun uploadProfilePhoto(
            userId: String,
            imageUri: Uri,
        ): Result<String> =
            runCatching {
                val bytes = ImageCompressor.compressImage(context, imageUri)
                    ?: throw Exception("Failed to compress image for Uri: $imageUri")

                val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, bytes.size)
                val filePart = MultipartBody.Part.createFormData("file", "profile_$userId.jpg", requestFile)
                val typePart = MultipartBody.Part.createFormData("type", "profile")
                val entityIdPart = MultipartBody.Part.createFormData("entityId", userId)

                val response = mediaApiService.uploadImage(filePart, typePart, entityIdPart)
                if (response.success && response.data != null) {
                    response.data.url
                } else {
                    throw Exception(response.message)
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to upload profile photo")
            }

        override suspend fun saveUserProfile(user: User): Result<User> =
            runCatching {
                val request = RegisterRequest(
                    firebaseUid = user.id,
                    email = user.email,
                    name = user.name,
                    bio = user.bio.takeIf { it.isNotBlank() },
                    avatarUrl = user.profilePhotoUrl.takeIf { it.isNotBlank() },
                    phone = user.phone.takeIf { it.isNotBlank() },
                    campusLocation = user.campusLocation.takeIf { it.isNotBlank() },
                )
                val response = authApiService.register(request)
                if (response.success && response.data != null) {
                    response.data.toDomain()
                } else {
                    throw Exception(response.message)
                }
            }.recoverCatching { e ->
                if (e is retrofit2.HttpException) {
                    when (e.code()) {
                        // 409 Conflict = user already exists → treat as success
                        409 -> {
                            Timber.d("UserRepositoryImpl: user already registered (HTTP 409) — treating as success")
                            user
                        }
                        else -> {
                            Timber.e(e, "UserRepositoryImpl: failed to save profile (HTTP ${e.code()})")
                            throw e
                        }
                    }
                } else {
                    Timber.e(e, "UserRepositoryImpl: failed to save user profile")
                    throw e
                }
            }

        override suspend fun getUserProfile(userId: String): Result<User?> =
            runCatching {
                val response = userApiService.getMe()
                if (response.success && response.data != null) {
                    val user = response.data.toDomain()
                    userDao.upsert(user.toEntity())
                    user
                } else {
                    userDao.getUserById(userId)?.toDomain()
                }
            }.recoverCatching { e ->
                Timber.w(e, "UserRepositoryImpl: network miss, checking Room cache for user: %s", userId)
                userDao.getUserById(userId)?.toDomain() ?: throw e
            }

        override suspend fun hasUserProfile(userId: String): Result<Boolean> =
            runCatching {
                val response = userApiService.getMe()
                response.success && response.data != null
            }.recoverCatching { e ->
                if (e is retrofit2.HttpException && (e.code() == 403 || e.code() == 404)) {
                    Timber.d("UserRepositoryImpl: no backend profile found (HTTP ${e.code()}) — assuming new user")
                    false
                } else {
                    Timber.e(e, "UserRepositoryImpl: failed to check user profile")
                    throw e
                }
            }

        override suspend fun getProviderProfile(providerId: String): Result<User?> =
            runCatching {
                val response = userApiService.getById(providerId)
                if (response.success && response.data != null) response.data.toDomain() else null
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to fetch provider profile id=$providerId")
            }

        override suspend fun getServicesByProvider(providerId: String): Result<List<Service>> =
            runCatching {
                // Uses the browse endpoint filtered by providerId until a dedicated endpoint exists.
                val response = serviceApiService.browseServices(size = 50)
                if (response.success && response.data != null) {
                    response.data.content
                        .filter { it.providerId == providerId }
                        .map { it.toDomainService() }
                } else {
                    emptyList()
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to fetch services for provider id=$providerId")
            }

        override suspend fun updateProfile(
            name: String,
            bio: String,
            phone: String,
            campusLocation: String,
            avatarUrl: String?,
            allowCalls: Boolean,
        ): Result<User> =
            runCatching {
                val request = UpdateProfileRequest(
                    name = name,
                    bio = bio.takeIf { it.isNotBlank() },
                    avatarUrl = avatarUrl,
                    phone = phone.takeIf { it.isNotBlank() },
                    campusLocation = campusLocation.takeIf { it.isNotBlank() },
                    allowCalls = allowCalls,
                )
                val response = userApiService.updateMe(request)
                if (response.success && response.data != null) {
                    response.data.toDomain()
                } else {
                    throw Exception(response.message)
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to update profile")
            }

        override suspend fun updateFcmToken(token: String): Result<Unit> =
            runCatching {
                val response = userApiService.updateFcmToken(FcmTokenRequest(token))
                if (!response.isSuccessful) {
                    throw Exception("FCM token update failed: code ${response.code()}")
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to update FCM token")
            }

        override suspend fun removeFcmToken(token: String): Result<Unit> =
            runCatching {
                val response = userApiService.removeFcmToken(token)
                if (!response.isSuccessful) {
                    throw Exception("FCM token removal failed: code ${response.code()}")
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to remove FCM token")
            }

        override suspend fun updateUserLocation(
            lat: Double,
            lng: Double,
        ): Result<Unit> =
            runCatching {
                val response = userApiService.updateLocation(LocationUpdateRequest(lat, lng))
                if (!response.isSuccessful) {
                    throw Exception("Location update failed: code ${response.code()}")
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to update user location")
            }

        override suspend fun getNearbyProviders(
            lat: Double,
            lng: Double,
            radiusMeters: Double,
        ): Result<List<User>> =
            runCatching {
                val response = userApiService.getNearbyProviders(lat, lng, radiusMeters)
                if (response.success && response.data != null) {
                    response.data.map { it.toDomain() }
                } else {
                    throw Exception(response.message)
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to fetch nearby providers")
            }

        override suspend fun blockUser(targetId: String): Result<Unit> =
            runCatching {
                val response = userApiService.blockUser(targetId)
                if (!response.isSuccessful) {
                    throw Exception("Block user failed: code ${response.code()}")
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to block user $targetId")
            }

        override suspend fun unblockUser(targetId: String): Result<Unit> =
            runCatching {
                val response = userApiService.unblockUser(targetId)
                if (!response.isSuccessful) {
                    throw Exception("Unblock user failed: code ${response.code()}")
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to unblock user $targetId")
            }

        override suspend fun getBlockedUsers(): Result<List<User>> =
            runCatching {
                val response = userApiService.getBlockedUsers()
                if (response.success && response.data != null) {
                    response.data.map { it.toDomain() }
                } else {
                    throw Exception(response.message)
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to fetch blocked users")
            }
    }

// DTO → Domain mapper (private to this file)
private fun UserResponseDto.toDomain(): User =
    User(
        id = firebaseUid,
        uuid = id,
        name = name,
        email = email,
        phone = phone ?: "",
        campusLocation = campusLocation ?: "",
        role = runCatching { UserRole.valueOf(role.removePrefix("ROLE_")) }.getOrDefault(UserRole.CUSTOMER),
        profilePhotoUrl = avatarUrl ?: "",
        bio = bio ?: "",
        isVerified = verified,
        isVerifiedPro = isVerifiedPro,
        isOnline = active,
        allowCalls = allowCalls ?: false,
        hustleScore = hustleScore ?: 0f,
        reviewCount = reviewCount ?: 0,
        lat = lat,
        lng = lng,
    )

private fun ServiceResponse.toDomainService(): Service =
    Service(
        id = serviceId,
        providerId = providerId,
        title = title,
        category = runCatching { ServiceCategory.valueOf(category) }.getOrDefault(ServiceCategory.OTHER),
        description = description ?: "",
        priceRange = priceRange,
        portfolio = portfolioImages ?: emptyList(),
        availability = runCatching { ServiceAvailability.valueOf(availability) }.getOrDefault(ServiceAvailability.AVAILABLE),
        averageRating = avgRating.toFloat(),
        reviewCount = reviewCount,
        openToBarter = openToBarter,
        tags = tags ?: emptyList(),
        iconUrl = portfolioImages?.firstOrNull() ?: "",
    )
