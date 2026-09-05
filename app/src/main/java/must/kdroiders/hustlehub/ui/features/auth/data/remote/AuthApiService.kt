package must.kdroiders.hustlehub.ui.features.auth.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import must.kdroiders.hustlehub.core.api.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

@Keep
data class RegisterRequest(
    @SerializedName("firebaseUid")
    val firebaseUid: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("campusLocation")
    val campusLocation: String? = null,
)

@Keep
data class UserResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("firebaseUid")
    val firebaseUid: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("bio")
    val bio: String?,
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("campusLocation")
    val campusLocation: String?,
    // Jackson strips the "is" prefix from Kotlin Boolean properties when serializing:
    // isVerified → "verified", isActive → "active" in the JSON response.
    @SerializedName("verified")
    val verified: Boolean,
    @SerializedName("isVerifiedPro")
    val isVerifiedPro: Boolean = false,
    @SerializedName("active")
    val active: Boolean,
    @SerializedName("allowCalls")
    val allowCalls: Boolean? = null,
    @SerializedName("hustleScore")
    val hustleScore: Float? = null,
    @SerializedName("reviewCount")
    val reviewCount: Int? = null,
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("lng")
    val lng: Double? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
)

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): ApiResponse<UserResponseDto>
}
