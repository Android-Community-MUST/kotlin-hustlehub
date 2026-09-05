package must.kdroiders.hustlehub.ui.features.home.data.remote

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import must.kdroiders.hustlehub.core.api.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Backend contract for AI-powered service discovery. */
interface DiscoveryApiService {
    /**
     * Submits a natural language query to the backend, which calls Gemini internally,
     * extracts search parameters, queries the database, and returns ranked matches.
     *
     * The API key never leaves the server. Fallback to keyword search happens server-side
     * if Gemini is unavailable, so the client always receives a valid response.
     *
     * POST /api/v1/discovery/ai-search
     */
    @POST("discovery/ai-search")
    suspend fun aiSearch(
        @Body request: AiSearchRequest,
    ): ApiResponse<AiSearchResponse>

    @GET("discovery/map-pins")
    suspend fun getMapPins(
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?,
        @Query("radiusKm") radiusKm: Double?,
        @Query("category") category: String?,
        @Query("availability") availability: String?,
    ): ApiResponse<List<MapPinResponseDto>>
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
)

/** @property query Natural language query from the user, max 500 chars. */
@Keep
data class AiSearchRequest(
    @SerializedName("query")
    val query: String,
    @SerializedName("userLocation")
    val userLocation: UserLocationDto? = null,
    /** Number of results to return (1–100). Default 10. */
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
    /** Relevance score 0.0–1.0, computed server-side from rating + distance. */
    @SerializedName("relevanceScore")
    val relevanceScore: Double,
    /** Human-readable reason string, e.g. "Offers braids, 200m from you, price 300–800 KSh" */
    @SerializedName("matchReason")
    val matchReason: String,
    @SerializedName("distanceMeters")
    val distanceMeters: Double?,
)

/**
 * Structured understanding of the user's natural language query extracted by Gemini.
 * All fields are nullable — the backend sets only what it could confidently extract.
 */
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
