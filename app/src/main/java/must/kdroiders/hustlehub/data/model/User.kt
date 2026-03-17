package must.kdroiders.hustlehub.data.model

enum class UserRole {
    PROVIDER,
    CUSTOMER,
    BOTH
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val studentId: String = "",
    val campus: String = "Meru University",
    val course: String = "",
    val yearOfStudy: Int = 1,
    val hostel: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val profilePhotoUrl: String = "",
    val bio: String = "",
    val location: String = "",
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis()
) {

    fun toMap(): Map<String, Any> = mapOf (
        "id" to id,
        "name" to name,
        "email" to email,
        "studentId" to studentId,
        "campus" to campus,
        "course" to course,
        "yearOfStudy" to yearOfStudy,
        "hostel" to hostel,
        "role" to role.name,
        "profilePhotoUrl" to profilePhotoUrl,
        "bio" to bio,
        "location" to location,
        "isVerified" to isVerified,
        "isOnline" to isOnline,
        "createdAt" to createdAt,
        "lastSeen" to lastSeen
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): User = User(
            id = map["id"] as? String ?: "",
            name = map["name"] as? String ?: "",
            email = map["email"] as? String ?: "",
            studentId = map["studentId"] as? String ?: "",
            campus = map["campus"] as? String ?: "Meru University",
            course = map["course"] as? String ?: "",
            yearOfStudy = (map["yearOfStudy"] as? Long)?.toInt() ?: 1,
            hostel = map["hostel"] as? String ?: "",
            role = try {
                UserRole.valueOf(map["role"] as? String ?: "CUSTOMER")
            } catch (_: IllegalArgumentException) {
                UserRole.CUSTOMER
            },
            profilePhotoUrl = map["profilePhotoUrl"] as? String ?: "",
            bio = map["bio"] as? String ?: "",
            location = map["location"] as? String ?: "",
            isVerified = map["isVerified"] as? Boolean ?: false,
            isOnline = map["isOnline"] as? Boolean ?: false,
            createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
            lastSeen = map["lastSeen"] as? Long ?: System.currentTimeMillis()
        )

    }

}
