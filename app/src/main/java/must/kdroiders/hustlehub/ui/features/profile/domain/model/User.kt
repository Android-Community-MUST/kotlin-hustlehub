package must.kdroiders.hustlehub.ui.features.profile.domain.model

enum class UserRole {
    PROVIDER,
    CUSTOMER,
    BOTH,
}

data class User(
    val id: String = "", // Firebase UID
    val uuid: String = "", // Backend database UUID
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val campusLocation: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val profilePhotoUrl: String = "",
    val bio: String = "",
    val isVerified: Boolean = false,
    val isOnline: Boolean = true,
    val hustleScore: Float = 0f,
    val reviewCount: Int = 0,
    val lat: Double? = null,
    val lng: Double? = null,
    val allowCalls: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
