package must.kdroiders.hustlehub.ui.features.profile.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.auth.data.remote.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Keep
data class UpdateProfileRequest(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("campusLocation")
    val campusLocation: String? = null,
    @SerializedName("allowCalls")
    val allowCalls: Boolean? = null,
)

@Keep
data class OnlineStatusRequest(
    @SerializedName("isOnline")
    val isOnline: Boolean,
)

@Keep
data class FcmTokenRequest(
    @SerializedName("token")
    val token: String,
)

@Keep
data class LocationUpdateRequest(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double,
)

interface UserApiService {
    @GET("users/me")
    suspend fun getMe(): ApiResponse<UserResponseDto>

    @PUT("users/me")
    suspend fun updateMe(
        @Body request: UpdateProfileRequest,
    ): ApiResponse<UserResponseDto>

    @DELETE("users/me")
    suspend fun deleteMe(): Response<Unit>

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

    @DELETE("users/fcm-token")
    suspend fun removeFcmToken(
        @Query("token") token: String,
    ): Response<Unit>

    @PUT("users/me/location")
    suspend fun updateLocation(
        @Body request: LocationUpdateRequest,
    ): Response<Unit>

    @GET("users/nearby")
    suspend fun getNearbyProviders(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radiusMeters") radiusMeters: Double,
    ): ApiResponse<List<UserResponseDto>>

    @POST("users/{id}/block")
    suspend fun blockUser(
        @Path("id") id: String,
    ): Response<Unit>

    @DELETE("users/{id}/block")
    suspend fun unblockUser(
        @Path("id") id: String,
    ): Response<Unit>

    @GET("users/me/blocked")
    suspend fun getBlockedUsers(): ApiResponse<List<UserResponseDto>>
}
