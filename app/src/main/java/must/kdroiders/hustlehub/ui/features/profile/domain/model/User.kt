package must.kdroiders.hustlehub.ui.features.profile.domain.model

enum class UserRole {
    ROLE_CUSTOMER,
    ROLE_PROVIDER,
    ROLE_BOTH,
    ROLE_ADMIN,
    ROLE_SUPER_ADMIN;

    companion object {
        fun from(value: String?): UserRole {
            if (value.isNullOrBlank()) return ROLE_CUSTOMER
            val normalized = if (value.startsWith("ROLE_")) value else "ROLE_$value"
            return runCatching { valueOf(normalized) }.getOrDefault(ROLE_CUSTOMER)
        }
    }
}

data class User(
    val id: String = "", // Firebase UID
    val uuid: String = "", // Backend database UUID
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val campusLocation: String = "",
    val role: UserRole = UserRole.ROLE_CUSTOMER,
    val profilePhotoUrl: String = "",
    val bio: String = "",
    val isVerified: Boolean = false,
    val isVerifiedPro: Boolean = false,
    val isOnline: Boolean = true,
    val isSuspended: Boolean = false,
    val suspendedReason: String? = null,
    val hustleScore: Float = 0f,
    val reviewCount: Int = 0,
    val lat: Double? = null,
    val lng: Double? = null,
    val allowCalls: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
