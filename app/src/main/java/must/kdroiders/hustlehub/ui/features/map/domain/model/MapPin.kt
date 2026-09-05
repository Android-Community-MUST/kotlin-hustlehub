package must.kdroiders.hustlehub.ui.features.map.domain.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory

data class MapPin(
    val serviceId: String,
    val providerId: String,
    val providerName: String,
    val providerPhotoUrl: String?,
    val serviceTitle: String,
    val category: ServiceCategory,
    val availability: ServiceAvailability,
    val averageRating: Double,
    val lat: Double,
    val lng: Double,
    val distanceMeters: Double? = null,
) : ClusterItem {
    override val position: LatLng
        get() = LatLng(lat, lng)
    override val title: String
        get() = serviceTitle
    override val snippet: String
        get() = providerName
    override val zIndex: Float?
        get() = null
}
