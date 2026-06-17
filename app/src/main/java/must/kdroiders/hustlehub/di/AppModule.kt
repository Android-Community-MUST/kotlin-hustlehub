package must.kdroiders.hustlehub.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import must.kdroiders.hustlehub.core.auth.AuthManager
import must.kdroiders.hustlehub.data.local.AppDatabase
import must.kdroiders.hustlehub.data.local.dao.ServiceDao
import must.kdroiders.hustlehub.data.remote.MediaApiService
import must.kdroiders.hustlehub.data.remote.ServiceApiService
import must.kdroiders.hustlehub.data.remote.UserApiService
import must.kdroiders.hustlehub.data.repository.ServiceRepositoryImpl
import must.kdroiders.hustlehub.data.repository.StorageRepository
import must.kdroiders.hustlehub.data.repository.StorageRepositoryImpl
import must.kdroiders.hustlehub.data.repository.UserRepository
import must.kdroiders.hustlehub.data.repository.UserRepositoryImpl
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.datastore.dataStore
import must.kdroiders.hustlehub.domain.repository.ServiceRepository
import must.kdroiders.hustlehub.ui.features.auth.data.remote.AuthApiService
import must.kdroiders.hustlehub.ui.features.auth.data.repository.AuthRepositoryImpl
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.LoginResult
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
                "Firebase not initialized — running without auth",
            )
            null
        }
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth?): AuthRepository {
        return if (firebaseAuth != null) {
            AuthRepositoryImpl(firebaseAuth)
        } else {
            NoopAuthRepository()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage? {
        return try {
            FirebaseStorage.getInstance()
        } catch (e: IllegalStateException) {
            Timber.w(
                e,
                "Firebase not initialized — running without storage"
            )
            null
        }
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideUserPreferences(dataStore: DataStore<Preferences>): UserPreferences = UserPreferences(dataStore)

    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context,
        authApiService: AuthApiService,
        userApiService: UserApiService,
        mediaApiService: MediaApiService,
    ): UserRepository {
        return UserRepositoryImpl(context, authApiService, userApiService, mediaApiService)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "hustlehub.db",
            ).build()

    @Provides
    @Singleton
    fun provideServiceDao(db: AppDatabase): ServiceDao = db.serviceDao()

    @Provides
    @Singleton
    fun provideServiceRepository(
        serviceApiService: ServiceApiService,
        serviceDao: ServiceDao,
        authManager: AuthManager,
    ): ServiceRepository {
        return ServiceRepositoryImpl(serviceApiService, serviceDao, authManager)
    }
}

private class NoopAuthRepository : AuthRepository {
    override suspend fun login(
        email: String,
        password: String,
    ): LoginResult = throw IllegalStateException("Firebase not initialized")

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
    ): LoginResult = throw IllegalStateException("Firebase not initialized")

    override suspend fun signInWithGoogle(idToken: String): LoginResult = throw IllegalStateException("Firebase not initialized")

    override suspend fun sendOtp(email: String) = throw IllegalStateException("Firebase not initialized")

    override suspend fun verifyOtp(
        email: String,
        otp: String,
    ) = throw IllegalStateException("Firebase not initialized")

    override suspend fun resendOtp(email: String) = throw IllegalStateException("Firebase not initialized")

    override suspend fun sendPasswordResetEmail(email: String) = throw IllegalStateException("Firebase not initialized")

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
    ): Result<Unit> = throw IllegalStateException("Firebase not initialized")

    override fun getCurrentUser() = null

    override fun logout() {}
}
