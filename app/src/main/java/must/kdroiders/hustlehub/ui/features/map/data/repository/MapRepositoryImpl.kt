package must.kdroiders.hustlehub.ui.features.map.data.repository

import must.kdroiders.hustlehub.ui.features.home.data.remote.DiscoveryApiService
import must.kdroiders.hustlehub.ui.features.map.data.local.dao.MapPinDao
import must.kdroiders.hustlehub.ui.features.map.data.local.entity.toDomain
import must.kdroiders.hustlehub.ui.features.map.data.local.entity.toEntity
import must.kdroiders.hustlehub.ui.features.map.domain.model.MapPin
import must.kdroiders.hustlehub.ui.features.map.domain.repository.MapRepository
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import timber.log.Timber
import javax.inject.Inject

class MapRepositoryImpl
    @Inject
    constructor(
        private val discoveryApiService: DiscoveryApiService,
        private val mapPinDao: MapPinDao,
    ) : MapRepository {
        override suspend fun getMapPins(
            lat: Double?,
            lng: Double?,
            radiusKm: Double?,
            category: ServiceCategory?,
            availability: ServiceAvailability?,
        ): Result<List<MapPin>> =
            runCatching {
                val apiResponse = discoveryApiService.getMapPins(
                    lat = lat,
                    lng = lng,
                    radiusKm = radiusKm,
                    category = category?.name,
                    availability = availability?.name,
                )
                val dtoList = apiResponse.data ?: return@runCatching emptyList()
                val pins = dtoList.map { dto ->
                    MapPin(
                        serviceId = dto.serviceId,
                        providerId = dto.providerId,
                        providerName = dto.providerName,
                        providerPhotoUrl = dto.providerPhotoUrl,
                        serviceTitle = dto.title,
                        category = try {
                            ServiceCategory.valueOf(dto.category)
                        } catch (e: Exception) {
                            ServiceCategory.OTHER
                        },
                        availability = try {
                            ServiceAvailability.valueOf(dto.availability)
                        } catch (e: Exception) {
                            ServiceAvailability.AVAILABLE
                        },
                        averageRating = dto.averageRating,
                        lat = dto.lat,
                        lng = dto.lng,
                    )
                }
                mapPinDao.upsertAll(pins.map { it.toEntity() })
                pins
            }.recoverCatching { e ->
                Timber.w(e, "MapRepositoryImpl: network miss, returning Room cached map pins")
                val cached = mapPinDao.getAllMapPins()
                if (cached.isNotEmpty()) {
                    cached.map { it.toDomain() }
                } else {
                    throw e
                }
            }
    }
