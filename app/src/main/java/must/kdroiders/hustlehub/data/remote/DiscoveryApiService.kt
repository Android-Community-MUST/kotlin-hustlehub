package must.kdroiders.hustlehub.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

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
    suspend fun aiSearch(@Body request: AiSearchRequest): ApiResponse<AiSearchResponse>
}

/** @property query Natural language query from the user, max 500 chars. */
data class AiSearchRequest(
    val query: String,
    val userLocation: UserLocationDto? = null,
    /** Number of results to return (1–100). Default 10. */
    val maxResults: Int = 10,
) {
    data class UserLocationDto(val lat: Double, val lng: Double)
}

data class AiSearchResponse(
    val matches: List<AiSearchMatch>,
    val queryUnderstanding: QueryUnderstanding,
)

data class AiSearchMatch(
    val serviceId: String,
    val providerId: String,
    val title: String,
    val category: String,
    val priceRange: String,
    /** Relevance score 0.0–1.0, computed server-side from rating + distance. */
    val relevanceScore: Double,
    /** Human-readable reason string, e.g. "Offers braids, 200m from you, price 300–800 KSh" */
    val matchReason: String,
    val distanceMeters: Double?,
)

/**
 * Structured understanding of the user's natural language query extracted by Gemini.
 * All fields are nullable — the backend sets only what it could confidently extract.
 */
data class QueryUnderstanding(
    val service: String?,
    val location: String?,
    val maxPrice: Int?,
    val category: String?,
)
