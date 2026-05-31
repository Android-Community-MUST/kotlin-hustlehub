package must.kdroiders.hustlehub.data.remote.dto

data class ServiceResponse(
    val serviceId: String,
    val providerId: String,
    val title: String,
    val category: String,
    val description: String?,
    val priceRange: String?,
    val portfolio: List<String>?,
    val availability: String,
    val averageRating: Float,
    val reviewCount: Int,
    val openToBarter: Boolean,
    val tags: List<String>?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateServiceRequest(
    val title: String,
    val category: String,
    val description: String?,
    val priceRange: String?,
    val portfolio: List<String>,
    val openToBarter: Boolean,
    val tags: List<String>
)

data class UpdateServiceRequest(
    val title: String?,
    val category: String?,
    val description: String?,
    val priceRange: String?,
    val portfolio: List<String>?,
    val openToBarter: Boolean?,
    val tags: List<String>?
)

data class UpdateAvailabilityRequest(
    val availability: String
)
