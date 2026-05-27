package must.kdroiders.hustlehub.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import must.kdroiders.hustlehub.data.model.User
import must.kdroiders.hustlehub.data.model.UserRole
import must.kdroiders.hustlehub.ui.auth.data.remote.AuthApiService
import must.kdroiders.hustlehub.ui.auth.data.remote.RegisterRequest
import must.kdroiders.hustlehub.data.remote.MediaApiService
import must.kdroiders.hustlehub.data.remote.UserApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    suspend fun uploadProfilePhoto(
        userId: String,
        imageUri: Uri
    ): Result<String>

    suspend fun saveUserProfile(user: User): Result<User>
    suspend fun getUserProfile(userId: String): Result<User?>
    suspend fun hasUserProfile(userId: String): Result<Boolean>
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApiService: AuthApiService,
    private val userApiService: UserApiService,
    private val mediaApiService: MediaApiService
) : UserRepository {

    override suspend fun uploadProfilePhoto(
        userId: String,
        imageUri: Uri
    ): Result<String> = runCatching {
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
        Timber.e(e, "Failed to upload profile photo")
    }

    override suspend fun saveUserProfile(
        user: User
    ): Result<User> = runCatching {
        val request = RegisterRequest(
            firebaseUid = user.id,
            email = user.email,
            name = user.name,
            bio = user.bio.takeIf { it.isNotBlank() },
            avatarUrl = user.profilePhotoUrl.takeIf { it.isNotBlank() },
            phone = user.phone.takeIf { it.isNotBlank() },
            campusLocation = user.campusLocation.takeIf { it.isNotBlank() }
        )
        val response = authApiService.register(request)
        if (response.success && response.data != null) {
            val dto = response.data
            User(
                id = dto.firebaseUid,
                uuid = dto.id,
                name = dto.name,
                email = dto.email,
                phone = dto.phone ?: "",
                campusLocation = dto.campusLocation ?: "",
                role = try {
                    UserRole.valueOf(dto.role)
                } catch (_: Exception) {
                    UserRole.CUSTOMER
                },
                profilePhotoUrl = dto.avatarUrl ?: "",
                bio = dto.bio ?: "",
                isVerified = dto.verified,
                isOnline = dto.active
            )
        } else {
            throw Exception(response.message)
        }
    }.recoverCatching { e ->
        if (e is retrofit2.HttpException) {
            when (e.code()) {
                409 -> {
                    // Backend returned 409 Conflict = user already exists → treat as success
                    Timber.d("User already registered in backend (HTTP 409) — treating as success")
                    user
                }
                500 -> {
                    // Backend returned 500 = likely a duplicate key constraint crash
                    // (backend should send 409 but sends 500 for existing emails).
                    // Allow the user to proceed — their account already exists.
                    Timber.w("Backend returned HTTP 500 on register — likely duplicate email. Treating as existing user and proceeding.")
                    user
                }
                else -> {
                    Timber.e(e, "Failed to save user profile (HTTP ${e.code()})")
                    throw e
                }
            }
        } else {
            Timber.e(e, "Failed to save user profile")
            throw e
        }
    }

    override suspend fun getUserProfile(
        userId: String
    ): Result<User?> = runCatching {
        val response = userApiService.getMe()
        if (response.success && response.data != null) {
            val dto = response.data
            User(
                id = dto.firebaseUid,
                uuid = dto.id,
                name = dto.name,
                email = dto.email,
                phone = dto.phone ?: "",
                campusLocation = dto.campusLocation ?: "",
                role = try {
                    UserRole.valueOf(dto.role)
                } catch (_: Exception) {
                    UserRole.CUSTOMER
                },
                profilePhotoUrl = dto.avatarUrl ?: "",
                bio = dto.bio ?: "",
                isVerified = dto.verified,
                isOnline = dto.active
            )
        } else {
            null
        }
    }.onFailure { e ->
        Timber.e(e, "Failed to get user profile")
    }

    override suspend fun hasUserProfile(
        userId: String
    ): Result<Boolean> = runCatching {
        val response = userApiService.getMe()
        response.success && response.data != null
    }.recoverCatching { e ->
        // HTTP 403 or 404 means the profile doesn't exist yet — not an error
        if (e is retrofit2.HttpException && (e.code() == 403 || e.code() == 404)) {
            Timber.d("No backend profile found for user (HTTP ${e.code()}) — assuming new user")
            false
        } else {
            Timber.e(e, "Failed to check user profile")
            throw e
        }
    }
}
