package must.kdroiders.hustlehub.ui.features.home.domain.model

import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory

data class TopHustler(
    val id: String,
    val providerName: String,
    val serviceTitle: String,
    val category: ServiceCategory,
    val rating: Float,
    val priceLabel: String, // e.g. "from KES 500" or "per hr KES 300"
    val imageUrl: String = "", // placeholder — real impl loads from backend
)
