package must.kdroiders.hustlehub.data.model

/**
 * Represents a service offered by a hustle provider.
 */
data class Service(
    val id: String = "",
    val title: String = "",
    val priceRange: String = "",
    val isActive: Boolean = true,
    val tags: List<String> = emptyList(),
    val iconUrl: String = ""
)
