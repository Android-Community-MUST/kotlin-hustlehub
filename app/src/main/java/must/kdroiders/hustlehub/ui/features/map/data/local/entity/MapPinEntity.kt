package must.kdroiders.hustlehub.ui.features.map.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory

@Entity(tableName = "map_pins")
data class MapPinEntity(
    @PrimaryKey val serviceId: String,
    val providerId: String,
    val providerName: String,
    val providerPhotoUrl: String?,
    val serviceTitle: String,
    val category: String,
    val availability: String,
    val averageRating: Double,
    val lat: Double,
    val lng: Double,
    val distanceMeters: Double?,
    val cachedAt: Long = System.currentTimeMillis(),
)

fun MapPinEntity.toDomain(): MapPin =
    MapPin(
        serviceId = serviceId,
        providerId = providerId,
        providerName = providerName,
        providerPhotoUrl = providerPhotoUrl,
        serviceTitle = serviceTitle,
        category = runCatching { ServiceCategory.valueOf(category) }.getOrDefault(ServiceCategory.OTHER),
        availability = runCatching { ServiceAvailability.valueOf(availability) }.getOrDefault(ServiceAvailability.AVAILABLE),
        averageRating = averageRating,
        lat = lat,
        lng = lng,
        distanceMeters = distanceMeters,
    )

fun MapPin.toEntity(cachedAt: Long = System.currentTimeMillis()): MapPinEntity =
    MapPinEntity(
        serviceId = serviceId,
        providerId = providerId,
        providerName = providerName,
        providerPhotoUrl = providerPhotoUrl,
        serviceTitle = serviceTitle,
        category = category.name,
        availability = availability.name,
        averageRating = averageRating,
        lat = lat,
        lng = lng,
        distanceMeters = distanceMeters,
        cachedAt = cachedAt,
    )
