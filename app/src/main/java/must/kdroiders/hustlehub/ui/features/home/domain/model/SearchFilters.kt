package must.kdroiders.hustlehub.ui.features.home.domain.model

import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability

/**
 * Immutable value object representing the user's active search filters.
 *
 * An empty [SearchFilters] (all defaults) means "show everything".
 * [isDefault] can be used to hide the active-filter chip row when no filters are set.
 */
data class SearchFilters(
    val categories: Set<String> = emptySet(),
    val minRating: Float = 0f,
    val maxPrice: Int = 5000,
    val availability: ServiceAvailability? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val lat: Double? = null,
    val lng: Double? = null,
) {
    val isDefault: Boolean
        get() = categories.isEmpty() &&
            minRating == 0f &&
            maxPrice == 5000 &&
            availability == null &&
            sortOrder == SortOrder.NEWEST
}

/**
 * Sort options supported by the backend's BrowseFilters.SortBy enum.
 * [apiValue] must match the backend enum name exactly.
 */
enum class SortOrder(val label: String, val apiValue: String) {
    NEWEST("Newest", "NEWEST"),
    RATING("Highest Rated", "RATING"),
    DISTANCE("Nearest", "DISTANCE"),
}
