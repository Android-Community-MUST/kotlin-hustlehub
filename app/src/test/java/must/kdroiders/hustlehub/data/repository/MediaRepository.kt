package must.kdroiders.hustlehub.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val auth: FirebaseAuth?,
    private val storage: FirebaseStorage
) {

    suspend fun uploadUserMedia(fileUri: Uri, fileName: String): String {
        val userId = auth?.currentUser?.uid ?: throw Exception("User not authenticated")


        val storageRef = storage.reference.child("users/$userId/$fileName")

        storageRef.putFile(fileUri).await()

        return storageRef.downloadUrl.await().toString()
    }
}
