package must.kdroiders.hustlehub.ui.features.map.domain.repository

import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory

interface MapRepository {
    suspend fun getMapPins(
        lat: Double?,
        lng: Double?,
        radiusKm: Double?,
        category: ServiceCategory?,
        availability: ServiceAvailability?,
    ): Result<List<MapPin>>
}
