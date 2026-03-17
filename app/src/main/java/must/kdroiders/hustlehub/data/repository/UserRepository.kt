package must.kdroiders.hustlehub.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import must.kdroiders.hustlehub.data.model.User
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


interface UserRepository {
    suspend fun uploadProfilePhoto(
        userId: String,
        imageUri: Uri
    ): Result<String>

    suspend fun saveUserProfile(user: User): Result<Void?>
    suspend fun getUserProfile(userId: String): Result<User?>
    suspend fun hasUserProfile(userId: String): Result<Boolean>
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val PROFILE_PHOTOS_PATH = "profile_photos"
    }

    override suspend fun uploadProfilePhoto(
        userId: String,
        imageUri: Uri
    ): Result<String> = runCatching {
        val ref = storage.reference
            .child("$PROFILE_PHOTOS_PATH/$userId.jpg")
        // putFile returns an UploadTask; .await() suspends
        // until it finishes, keeping the coroutine non-blocking
        ref.putFile(imageUri).await()
        // After upload completes, get the public URL
        val downloadUrl = ref.downloadUrl.await()
        downloadUrl.toString()
    }.onFailure { e ->
        Timber.e(e, "Failed to upload profile photo")
    }
    /**
     * Saves the full user document to Firestore.
     * Uses .set() which creates or overwrites
     * the entire document.
     */
    override suspend fun saveUserProfile(
        user: User
    ): Result<Void?> = runCatching {
        firestore.collection(USERS_COLLECTION)
            .document(user.id)
            .set(user.toMap())
            .await()
    }.onFailure { e ->
        Timber.e(e, "Failed to save user profile")
    }
    /**
     * Reads a user document from Firestore.
     * Returns null if the document doesn't exist.
     */
    override suspend fun getUserProfile(
        userId: String
    ): Result<User?> = runCatching {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()
        if (snapshot.exists()) {
            snapshot.data?.let { User.fromMap(it) }
        } else {
            null
        }
    }.onFailure { e ->
        Timber.e(e, "Failed to get user profile")
    }
    /**
     * Quick existence check — cheaper than reading
     * the full document when you just need a boolean.
     */
    override suspend fun hasUserProfile(
        userId: String
    ): Result<Boolean> = runCatching {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()
        snapshot.exists()
    }.onFailure { e ->
        Timber.e(e, "Failed to check user profile")
    }

}
