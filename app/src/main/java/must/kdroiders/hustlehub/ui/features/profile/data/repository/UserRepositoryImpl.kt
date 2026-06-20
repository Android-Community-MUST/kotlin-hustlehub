package must.kdroiders.hustlehub.ui.features.profile.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import must.kdroiders.hustlehub.data.model.User
import must.kdroiders.hustlehub.data.model.UserRole
import must.kdroiders.hustlehub.data.remote.MediaApiService
import must.kdroiders.hustlehub.data.remote.UserApiService
import must.kdroiders.hustlehub.ui.features.auth.data.remote.AuthApiService
import must.kdroiders.hustlehub.ui.features.auth.data.remote.RegisterRequest
import must.kdroiders.hustlehub.ui.features.auth.data.remote.UserResponseDto
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
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
    ) : UserRepository {
        override suspend fun uploadProfilePhoto(
            userId: String,
            imageUri: Uri,
        ): Result<String> =
            runCatching {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("Failed to open input stream for Uri: $imageUri")
                val bytes = inputStream.use { it.readBytes() }

                val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, bytes.size)
                val filePart = MultipartBody.Part.createFormData("file", "profile_$userId.jpg", requestFile)
                val typePart = "PROFILE_PHOTO".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = mediaApiService.uploadImage(filePart, typePart)
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
                    response.data.toDomain()
                } else {
                    null
                }
            }.onFailure { e ->
                Timber.e(e, "UserRepositoryImpl: failed to get user profile")
            }

        override suspend fun hasUserProfile(userId: String): Result<Boolean> =
            runCatching {
                val response = userApiService.getMe()
                response.success && response.data != null
            }.recoverCatching { e ->
                // HTTP 403/404 means the profile doesn't exist yet — not a fatal error
                if (e is retrofit2.HttpException && (e.code() == 403 || e.code() == 404)) {
                    Timber.d("UserRepositoryImpl: no backend profile found (HTTP ${e.code()}) — assuming new user")
                    false
                } else {
                    Timber.e(e, "UserRepositoryImpl: failed to check user profile")
                    throw e
                }
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
        role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.CUSTOMER),
        profilePhotoUrl = avatarUrl ?: "",
        bio = bio ?: "",
        isVerified = verified,
        isOnline = active,
    )
