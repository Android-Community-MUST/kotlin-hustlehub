package must.kdroiders.hustlehub.data.remote.dto

data class ServiceResponse(
    val serviceId: String,
    val providerId: String,
    val title: String,
    val category: String,
    val description: String?,
    val priceRange: String,
    val portfolioImages: List<String>?,
    val availability: String,
    val avgRating: Double,
    val reviewCount: Int,
    val openToBarter: Boolean,
    val tags: List<String>?,
    val location: LocationDto?,
    val distanceMeters: Double?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateServiceRequest(
    val title: String,
    val category: String,
    val description: String?,
    val minPrice: Int,
    val maxPrice: Int,
    val openToBarter: Boolean,
    val tags: List<String>,
    val location: LocationDto? = null
)

data class UpdateServiceRequest(
    val title: String?,
    val category: String?,
    val description: String?,
    val minPrice: Int?,
    val maxPrice: Int?,
    val openToBarter: Boolean?,
    val tags: List<String>?,
    val location: LocationDto? = null
)

data class AvailabilityRequest(
    val availability: String
)

data class LocationDto(
    val lat: Double?,
    val lng: Double?,
    val label: String?
)
