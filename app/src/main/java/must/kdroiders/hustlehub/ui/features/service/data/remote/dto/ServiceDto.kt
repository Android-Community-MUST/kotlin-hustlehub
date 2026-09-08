package must.kdroiders.hustlehub.ui.features.service.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ServiceResponse(
    @SerializedName("serviceId")
    val serviceId: String,
    @SerializedName("providerId")
    val providerId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("priceRange")
    val priceRange: String,
    @SerializedName("portfolioImages")
    val portfolioImages: List<String>?,
    @SerializedName("availability")
    val availability: String,
    @SerializedName("avgRating")
    val avgRating: Double,
    @SerializedName("reviewCount")
    val reviewCount: Int,
    @SerializedName("openToBarter")
    val openToBarter: Boolean,
    @SerializedName("isFeatured")
    val isFeatured: Boolean? = false,
    @SerializedName("tags")
    val tags: List<String>?,
    @SerializedName("location")
    val location: LocationDto?,
    @SerializedName("distanceMeters")
    val distanceMeters: Double?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
)

@Keep
data class CreateServiceRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("minPrice")
    val minPrice: Int,
    @SerializedName("maxPrice")
    val maxPrice: Int,
    @SerializedName("openToBarter")
    val openToBarter: Boolean,
    @SerializedName("tags")
    val tags: List<String>,
    @SerializedName("location")
    val location: LocationDto? = null,
    @SerializedName("portfolioUrls")
    val portfolioUrls: List<String> = emptyList(),
)

@Keep
data class UpdateServiceRequest(
    @SerializedName("title")
    val title: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("minPrice")
    val minPrice: Int?,
    @SerializedName("maxPrice")
    val maxPrice: Int?,
    @SerializedName("openToBarter")
    val openToBarter: Boolean?,
    @SerializedName("tags")
    val tags: List<String>?,
    @SerializedName("location")
    val location: LocationDto? = null,
    @SerializedName("portfolioUrls")
    val portfolioUrls: List<String>? = null,
)

@Keep
data class AvailabilityRequest(
    @SerializedName("availability")
    val availability: String,
)

@Keep
data class LocationDto(
    @SerializedName("lat")
    val lat: Double?,
    @SerializedName("lng")
    val lng: Double?,
    @SerializedName("label")
    val label: String?,
)
