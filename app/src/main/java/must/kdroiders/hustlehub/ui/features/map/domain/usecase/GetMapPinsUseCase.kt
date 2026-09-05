package must.kdroiders.hustlehub.ui.features.map.domain.usecase

import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.map.domain.repository.MapRepository
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import javax.inject.Inject

class GetMapPinsUseCase
    @Inject
    constructor(
        private val repository: MapRepository,
    ) {
        suspend operator fun invoke(
            lat: Double?,
            lng: Double?,
            radiusKm: Double?,
            category: ServiceCategory?,
            availability: ServiceAvailability?,
        ): Result<List<MapPin>> =
            repository.getMapPins(
                lat = lat,
                lng = lng,
                radiusKm = radiusKm,
                category = category,
                availability = availability,
            )
    }
