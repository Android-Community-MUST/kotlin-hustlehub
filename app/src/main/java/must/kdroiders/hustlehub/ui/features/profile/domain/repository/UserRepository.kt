package must.kdroiders.hustlehub.ui.features.profile.domain.repository

import android.net.Uri
import must.kdroiders.hustlehub.data.model.User

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
}
