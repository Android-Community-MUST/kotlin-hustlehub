package must.kdroiders.hustlehub.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import must.kdroiders.hustlehub.data.model.User
import must.kdroiders.hustlehub.ui.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.auth.data.repository.AuthRepositoryImpl
import must.kdroiders.hustlehub.ui.auth.domain.repository.LoginResult
import must.kdroiders.hustlehub.data.repository.UserRepository
import must.kdroiders.hustlehub.data.repository.UserRepositoryImpl
import must.kdroiders.hustlehub.ui.auth.data.remote.AuthApiService
import must.kdroiders.hustlehub.data.remote.UserApiService
import must.kdroiders.hustlehub.data.remote.MediaApiService
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
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth?
    ): AuthRepository {
        return if (firebaseAuth != null) {
            AuthRepositoryImpl(firebaseAuth)
        } else {
            NoopAuthRepository()
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
        @ApplicationContext context: Context,
        authApiService: AuthApiService,
        userApiService: UserApiService,
        mediaApiService: MediaApiService
    ): UserRepository {
        return UserRepositoryImpl(context, authApiService, userApiService, mediaApiService)
    }
}

private class NoopAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): LoginResult =
        throw IllegalStateException("Firebase not initialized")

    override suspend fun signUp(name: String, email: String, password: String): LoginResult =
        throw IllegalStateException("Firebase not initialized")

    override suspend fun signInWithGoogle(idToken: String): LoginResult =
        throw IllegalStateException("Firebase not initialized")

    override suspend fun sendOtp(email: String) =
        throw IllegalStateException("Firebase not initialized")

    override suspend fun verifyOtp(email: String, otp: String) =
        throw IllegalStateException("Firebase not initialized")

    override suspend fun resendOtp(email: String) =
        throw IllegalStateException("Firebase not initialized")

    override fun getCurrentUser() = null

    override fun logout() {}
}
