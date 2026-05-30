package must.kdroiders.hustlehub.ui.features.home.domain.model

data class LiveService(
    val id: String,
    val title: String,
    val price: String,          // e.g. "KES 250"
    val location: String,       // e.g. "Hall 6, Room 102"
    val imageUrl: String = ""
)
