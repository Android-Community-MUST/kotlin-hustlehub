package must.kdroiders.hustlehub.data.model

enum class UserRole {
    PROVIDER,
    CUSTOMER,
    BOTH
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
    val createdAt: Long = System.currentTimeMillis()
)
