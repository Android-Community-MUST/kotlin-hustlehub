package must.kdroiders.hustlehub.ui.features.home.data.repository

import kotlinx.coroutines.CancellationException
import must.kdroiders.hustlehub.ui.features.home.data.remote.AiSearchRequest
import must.kdroiders.hustlehub.ui.features.home.data.remote.AiSearchResponse
import must.kdroiders.hustlehub.ui.features.home.data.remote.DiscoveryApiService
import must.kdroiders.hustlehub.ui.features.home.domain.repository.AiSearchRepository
import timber.log.Timber

/** Cache TTL in milliseconds (5 minutes). */
private const val CACHE_TTL_MS = 5 * 60 * 1_000L

/** Maximum number of cached query results held in memory. Oldest entry is evicted when full. */
private const val CACHE_MAX_ENTRIES = 20

/**
 * Concrete implementation of [AiSearchRepository].
 *
 * The backend handles Gemini interaction and keyword fallback internally, so this impl
 * simply calls the endpoint and maintains an LRU-bounded in-memory TTL cache to avoid
 * redundant Gemini calls for repeated queries within a 5-minute window.
 *
 * Cache key: trimmed, lowercased query string.
 */
class AiSearchRepositoryImpl(
    private val discoveryApiService: DiscoveryApiService,
) : AiSearchRepository {
    // LinkedHashMap in access-order mode = LRU eviction.
    private val cache = object : LinkedHashMap<String, Pair<Long, AiSearchResponse>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Pair<Long, AiSearchResponse>>): Boolean =
            size > CACHE_MAX_ENTRIES
    }

    override suspend fun aiSearch(
        query: String,
        userLocation: AiSearchRequest.UserLocationDto?,
        maxResults: Int,
    ): Result<AiSearchResponse> {
        val cacheKey = query.trim().lowercase()
        val now = System.currentTimeMillis()

        // Serve from cache if a fresh entry exists.
        cache[cacheKey]?.let { (timestamp, cached) ->
            if (now - timestamp < CACHE_TTL_MS) {
                Timber.d("AI search cache hit for query='$cacheKey'")
                return Result.success(cached)
            } else {
                cache.remove(cacheKey) // evict stale entry
            }
        }

        return runCatching {
            val request = AiSearchRequest(query = query, userLocation = userLocation, maxResults = maxResults)
            val response = discoveryApiService.aiSearch(request)
            check(response.success && response.data != null) { response.message ?: "AI search failed" }
            cache[cacheKey] = now to response.data
            response.data
        }.onFailure { e ->
            if (e is CancellationException) throw e
            Timber.w(e, "AI search network failure for query='$query'")
        }
    }
}
