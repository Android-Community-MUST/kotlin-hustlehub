package must.kdroiders.hustlehub.di

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import must.kdroiders.hustlehub.data.model.User
import must.kdroiders.hustlehub.data.repository.UserRepository
import must.kdroiders.hustlehub.data.repository.UserRepositoryImpl
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.datastore.dataStore
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: IllegalStateException) {
            Timber.w(
                e,
                "Firebase not initialized — running without auth"
            )
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: IllegalStateException) {
            Timber.w(e, "Firebase not initialized — running without Firestore")
            null
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage? {
        return try {
            FirebaseStorage.getInstance()
        } catch (e: IllegalStateException) {
            Timber.w(e, "Firebase not initialized — running without Storage")
            null
        }
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideUserPreferences(
        dataStore: DataStore<Preferences>
    ): UserPreferences = UserPreferences(dataStore)

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore?,
        storage: FirebaseStorage?
    ): UserRepository {
        if (firestore != null && storage != null) {
            return UserRepositoryImpl(firestore, storage)
        }
        // Return a dummy/noop implementation if Firebase is not available
        return object : UserRepository {
            override suspend fun uploadProfilePhoto(userId: String, imageUri: Uri): Result<String> =
                Result.failure(IllegalStateException("Firebase not initialized"))

            override suspend fun saveUserProfile(user: User): Result<Void?> =
                Result.failure(IllegalStateException("Firebase not initialized"))

            override suspend fun getUserProfile(userId: String): Result<User?> =
                Result.failure(IllegalStateException("Firebase not initialized"))

            override suspend fun hasUserProfile(userId: String): Result<Boolean> =
                Result.failure(IllegalStateException("Firebase not initialized"))
        }
    }
}
