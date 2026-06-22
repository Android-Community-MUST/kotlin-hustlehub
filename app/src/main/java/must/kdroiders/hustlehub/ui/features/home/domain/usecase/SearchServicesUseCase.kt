package must.kdroiders.hustlehub.ui.features.home.domain.usecase

import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.home.domain.model.SearchFilters
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import javax.inject.Inject

/**
 * Executes a service search applying text query and/or filter criteria.
 *
 * Routing logic:
 * - When [query] is non-blank: calls [ServiceRepository.searchServices] (GET /discovery/search?q=)
 *   which performs full-text search sorted by rating on the server.
 * - When [query] is blank but filters are set: calls [ServiceRepository.browseServices]
 *   with the filter params forwarded to GET /discovery/services.
 * - When both are empty: calls browse with no constraints (same as the Home screen).
 */
class SearchServicesUseCase
    @Inject
    constructor(
        private val repository: ServiceRepository,
    ) {
        suspend operator fun invoke(
            query: String,
            filters: SearchFilters,
            page: Int = 0,
            size: Int = 20,
        ): Result<PageResponse<Service>> {
            val trimmedQuery = query.trim()

            return if (trimmedQuery.isNotBlank()) {
                // Text search — dedicated endpoint, ignores filter params (server sorts by rating).
                repository.searchServices(query = trimmedQuery, page = page, size = size)
            } else {
                // Filter-only browse — no text query.
                val categoryEnum = if (filters.categories.size == 1) {
                    runCatching { ServiceCategory.valueOf(filters.categories.first()) }.getOrNull()
                } else {
                    null
                }

                repository.browseServices(
                    page = page,
                    size = size,
                    category = categoryEnum,
                    query = null,
                    availability = filters.availability,
                    minRating = if (filters.minRating > 0f) filters.minRating.toDouble() else null,
                    maxPrice = if (filters.maxPrice < 5000) filters.maxPrice else null,
                    lat = filters.lat,
                    lng = filters.lng,
                    sortBy = filters.sortOrder.apiValue,
                )
            }
        }
    }
