package must.kdroiders.hustlehub.ui.features.home.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.ServiceResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DiscoveryApiService {
    @POST("discovery/ai-search")
    suspend fun aiSearch(
        @Body request: AiSearchRequest,
    ): ApiResponse<AiSearchResponse>

    @GET("discovery/suggestions")
    suspend fun getSuggestions(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
    ): ApiResponse<List<String>>

    @GET("discovery/map-pins")
    suspend fun getMapPins(
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?,
        @Query("radiusKm") radiusKm: Double?,
        @Query("category") category: String?,
        @Query("availability") availability: String?,
    ): ApiResponse<List<MapPinResponseDto>>

    @GET("discovery/nearest")
    suspend fun getNearestServices(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("category") category: String? = null,
        @Query("availability") availability: String? = null,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<List<ServiceResponse>>

    @GET("discovery/trending")
    suspend fun getTrendingServices(
        @Query("category") category: String? = null,
        @Query("availability") availability: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): ApiResponse<PageResponse<ServiceResponse>>
}

@Keep
data class MapPinResponseDto(
    @SerializedName("serviceId")
    val serviceId: String,
    @SerializedName("providerId")
    val providerId: String,
    @SerializedName("providerName")
    val providerName: String,
    @SerializedName("providerPhotoUrl")
    val providerPhotoUrl: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("availability")
    val availability: String,
    @SerializedName("averageRating")
    val averageRating: Double,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double,
    @SerializedName("distanceMeters")
    val distanceMeters: Double? = null,
    @SerializedName("isPro", alternate = ["is_pro", "isVerifiedPro", "is_verified_pro", "providerIsVerifiedPro", "providerPro", "verifiedPro", "verified_pro"])
    val isPro: Boolean? = null,
)

@Keep
data class AiSearchRequest(
    @SerializedName("query")
    val query: String,
    @SerializedName("userLocation")
    val userLocation: UserLocationDto? = null,
    @SerializedName("maxResults")
    val maxResults: Int = 10,
) {
    @Keep
    data class UserLocationDto(
        @SerializedName("lat")
        val lat: Double,
        @SerializedName("lng")
        val lng: Double,
    )
}

@Keep
data class AiSearchResponse(
    @SerializedName("matches")
    val matches: List<AiSearchMatch>,
    @SerializedName("queryUnderstanding")
    val queryUnderstanding: QueryUnderstanding,
)

@Keep
data class AiSearchMatch(
    @SerializedName("serviceId")
    val serviceId: String,
    @SerializedName("providerId")
    val providerId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("priceRange")
    val priceRange: String,
    @SerializedName("relevanceScore")
    val relevanceScore: Double,
    @SerializedName("matchReason")
    val matchReason: String,
    @SerializedName("distanceMeters")
    val distanceMeters: Double?,
)

@Keep
data class QueryUnderstanding(
    @SerializedName("service")
    val service: String?,
    @SerializedName("location")
    val location: String?,
    @SerializedName("maxPrice")
    val maxPrice: Int?,
    @SerializedName("category")
    val category: String?,
)
