package must.kdroiders.hustlehub.ui.features.profile.domain.repository

import android.net.Uri
import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.service.domain.model.Service

/**
 * Contract for profile-related operations: photo upload, saving, and fetching.
 *
 * All methods return [Result] so callers handle success/failure without try/catch
 * at the presentation layer.
 */
interface UserRepository {
    /**
     * Uploads the profile photo at [imageUri] for the given [userId].
     *
     * @return [Result.success] with the public photo URL, or [Result.failure] on error.
     */
    suspend fun uploadProfilePhoto(
        userId: String,
        imageUri: Uri,
    ): Result<String>

    /**
     * Creates or updates the user's backend profile.
     *
     * @return [Result.success] with the confirmed [User] as stored by the backend.
     */
    suspend fun saveUserProfile(user: User): Result<User>

    /**
     * Fetches the current user's profile from the backend.
     *
     * @return [Result.success] with [User] if found, `null` if no profile exists yet.
     */
    suspend fun getUserProfile(userId: String): Result<User?>

    /**
     * Checks whether a backend profile exists for [userId].
     *
     * HTTP 403/404 is mapped to `false` (not an error).
     *
     * @return [Result.success] with `true` if profile exists, `false` if not.
     */
    suspend fun hasUserProfile(userId: String): Result<Boolean>

    /**
     * Fetches the public profile of any provider by their backend UUID.
     * Maps to GET /api/v1/users/{userId}.
     */
    suspend fun getProviderProfile(providerId: String): Result<User?>

    /**
     * Fetches all services listed by a specific provider.
     * Maps to GET /api/v1/services?providerId={providerId} once available;
     * currently uses GET /api/v1/discovery/services filtered by providerId.
     */
    suspend fun getServicesByProvider(providerId: String): Result<List<Service>>

    /**
     * Updates the currently authenticated user's profile.
     * Wraps PUT /api/v1/users/me.
     *
     * @param avatarUrl Pass a new photo URL after uploading via [uploadProfilePhoto]; pass null to leave unchanged.
     */
    suspend fun updateProfile(
        name: String,
        bio: String,
        phone: String,
        campusLocation: String,
        avatarUrl: String? = null,
        allowCalls: Boolean = false,
    ): Result<User>

    /**
     * Updates the currently authenticated user's FCM token.
     * Wraps PUT /api/v1/users/fcm-token.
     */
    suspend fun updateFcmToken(token: String): Result<Unit>

    /**
     * Removes an FCM token for the currently authenticated user.
     * Wraps DELETE /api/v1/users/fcm-token.
     */
    suspend fun removeFcmToken(token: String): Result<Unit>

    /**
     * Updates the user's location coordinates.
     * Wraps PUT /api/v1/users/me/location.
     */
    suspend fun updateUserLocation(
        lat: Double,
        lng: Double,
    ): Result<Unit>

    /**
     * Fetches nearby providers within a given radius.
     * Wraps GET /api/v1/users/nearby.
     */
    suspend fun getNearbyProviders(
        lat: Double,
        lng: Double,
        radiusMeters: Double = 1000.0,
    ): Result<List<User>>
}
