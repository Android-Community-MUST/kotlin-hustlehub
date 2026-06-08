package must.kdroiders.hustlehub.ui.features.auth.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(
    val firebaseUid: String,
    val email: String,
    val name: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val campusLocation: String? = null,
)

data class UserResponseDto(
    val id: String,
    val firebaseUid: String,
    val email: String,
    val name: String,
    val role: String,
    val bio: String?,
    val avatarUrl: String?,
    val phone: String?,
    val campusLocation: String?,
    // Jackson strips the "is" prefix from Kotlin Boolean properties when serializing:
    // isVerified → "verified", isActive → "active" in the JSON response.
    val verified: Boolean,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): ApiResponse<UserResponseDto>
}
