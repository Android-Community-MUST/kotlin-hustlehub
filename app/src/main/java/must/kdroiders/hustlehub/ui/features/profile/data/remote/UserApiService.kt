package must.kdroiders.hustlehub.ui.features.profile.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.auth.data.remote.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

data class UpdateProfileRequest(
    val name: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val campusLocation: String? = null,
)

data class OnlineStatusRequest(
    val isOnline: Boolean,
)

data class FcmTokenRequest(
    val token: String,
)

interface UserApiService {
    @GET("users/me")
    suspend fun getMe(): ApiResponse<UserResponseDto>

    @PUT("users/me")
    suspend fun updateMe(
        @Body request: UpdateProfileRequest,
    ): ApiResponse<UserResponseDto>

    @GET("users/{id}")
    suspend fun getById(
        @Path("id") id: String,
    ): ApiResponse<UserResponseDto>

    /** Returns 204 No Content on success. */
    @PUT("users/me/status")
    suspend fun updateOnlineStatus(
        @Body request: OnlineStatusRequest,
    ): Response<Unit>

    @PUT("users/fcm-token")
    suspend fun updateFcmToken(
        @Body request: FcmTokenRequest,
    ): Response<Unit>
}
