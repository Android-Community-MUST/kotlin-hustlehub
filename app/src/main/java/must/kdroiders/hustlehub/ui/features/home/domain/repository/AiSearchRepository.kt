package must.kdroiders.hustlehub.ui.features.home.domain.repository

import must.kdroiders.hustlehub.ui.features.home.data.remote.AiSearchRequest
import must.kdroiders.hustlehub.ui.features.home.data.remote.AiSearchResponse

/**
 * Contract for AI-powered service discovery.
 *
 * Implementations may cache results to avoid repeated Gemini calls for identical queries.
 */
interface AiSearchRepository {
    suspend fun aiSearch(
        query: String,
        userLocation: AiSearchRequest.UserLocationDto?,
        maxResults: Int = 10,
    ): Result<AiSearchResponse>
}
