package must.kdroiders.hustlehub.ui.features.service.domain.model

import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.LocationDto
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory

data class Service(
    val id: String = "",
    val providerId: String = "",
    val title: String = "",
    val category: ServiceCategory = ServiceCategory.OTHER,
    val description: String = "",
    val priceRange: String = "", // e.g. "300-800"
    val portfolio: List<String> = emptyList(), // image URLs
    val availability: ServiceAvailability = ServiceAvailability.AVAILABLE,
    val averageRating: Float = 0f,
    val reviewCount: Int = 0,
    val openToBarter: Boolean = false,
    val isFeatured: Boolean = false,
    val tags: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,

    // UI-specific fallback field for existing components if needed
    // (Could be removed after fully refactoring the UI to use portfolio)
    val iconUrl: String = "",
    val location: LocationDto? = null,
)
