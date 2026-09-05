package must.kdroiders.hustlehub.ui.features.profile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val uuid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val campusLocation: String = "",
    val role: String = UserRole.CUSTOMER.name,
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
    val updatedAt: Long = System.currentTimeMillis(),
)

fun UserEntity.toDomain(): User =
    User(
        id = id,
        uuid = uuid,
        name = name,
        email = email,
        phone = phone,
        campusLocation = campusLocation,
        role = runCatching { UserRole.valueOf(role.removePrefix("ROLE_")) }.getOrDefault(UserRole.CUSTOMER),
        profilePhotoUrl = profilePhotoUrl,
        bio = bio,
        isVerified = isVerified,
        isVerifiedPro = isVerifiedPro,
        isOnline = isOnline,
        isSuspended = isSuspended,
        suspendedReason = suspendedReason,
        hustleScore = hustleScore,
        reviewCount = reviewCount,
        lat = lat,
        lng = lng,
        allowCalls = allowCalls,
        createdAt = createdAt,
    )

fun User.toEntity(updatedAt: Long = System.currentTimeMillis()): UserEntity =
    UserEntity(
        id = id,
        uuid = uuid,
        name = name,
        email = email,
        phone = phone,
        campusLocation = campusLocation,
        role = role.name,
        profilePhotoUrl = profilePhotoUrl,
        bio = bio,
        isVerified = isVerified,
        isVerifiedPro = isVerifiedPro,
        isOnline = isOnline,
        isSuspended = isSuspended,
        suspendedReason = suspendedReason,
        hustleScore = hustleScore,
        reviewCount = reviewCount,
        lat = lat,
        lng = lng,
        allowCalls = allowCalls,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
