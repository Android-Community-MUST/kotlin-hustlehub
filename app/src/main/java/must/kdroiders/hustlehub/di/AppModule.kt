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
import must.kdroiders.hustlehub.datastore.UserPreferences
import must.kdroiders.hustlehub.datastore.dataStore
import must.kdroiders.hustlehub.ui.features.auth.data.remote.AuthApiService
import must.kdroiders.hustlehub.ui.features.auth.data.repository.AuthRepositoryImpl
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.AuthRepository
import must.kdroiders.hustlehub.ui.features.auth.domain.repository.LoginResult
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.ConversationDao
import must.kdroiders.hustlehub.ui.features.chat.data.local.dao.MessageDao
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ChatWebSocketService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ConversationApiService
import must.kdroiders.hustlehub.ui.features.chat.data.repository.ChatRepositoryImpl
import must.kdroiders.hustlehub.ui.features.chat.domain.repository.ChatRepository
import must.kdroiders.hustlehub.ui.features.home.data.remote.DiscoveryApiService
import must.kdroiders.hustlehub.ui.features.home.data.repository.AiSearchRepositoryImpl
import must.kdroiders.hustlehub.ui.features.home.domain.repository.AiSearchRepository
import must.kdroiders.hustlehub.ui.features.map.data.repository.MapRepositoryImpl
import must.kdroiders.hustlehub.ui.features.map.domain.repository.MapRepository
import must.kdroiders.hustlehub.ui.features.media.data.remote.MediaApiService
import must.kdroiders.hustlehub.ui.features.media.data.repository.StorageRepositoryImpl
import must.kdroiders.hustlehub.ui.features.media.domain.repository.StorageRepository
import must.kdroiders.hustlehub.ui.features.notification.data.remote.NotificationApiService
import must.kdroiders.hustlehub.ui.features.notification.data.repository.NotificationRepositoryImpl
import must.kdroiders.hustlehub.ui.features.notification.domain.repository.NotificationRepository
import must.kdroiders.hustlehub.ui.features.profile.data.remote.UserApiService
import must.kdroiders.hustlehub.ui.features.profile.data.repository.UserRepositoryImpl
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.features.service.data.local.dao.ServiceDao
import must.kdroiders.hustlehub.ui.features.service.data.remote.ServiceApiService
import must.kdroiders.hustlehub.ui.features.service.data.repository.ReviewRepositoryImpl
import must.kdroiders.hustlehub.ui.features.service.data.repository.ServiceRepositoryImpl
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ReviewRepository
import must.kdroiders.hustlehub.ui.features.service.domain.repository.ServiceRepository
import timber.log.Timber
import javax.inject.Provider
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
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth?,
        userRepositoryProvider: Provider<UserRepository>,
    ): AuthRepository {
        return if (firebaseAuth != null) {
            AuthRepositoryImpl(firebaseAuth, userRepositoryProvider)
        } else {
            NoopAuthRepository()
        }
    }

    @Provides
    @Singleton
    fun provideStorageRepository(mediaApiService: MediaApiService): StorageRepository = StorageRepositoryImpl(mediaApiService)

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
        serviceApiService: ServiceApiService,
    ): UserRepository {
        return UserRepositoryImpl(context, authApiService, userApiService, mediaApiService, serviceApiService)
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
            ).fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideServiceDao(db: AppDatabase): ServiceDao = db.serviceDao()

    @Provides
    @Singleton
    fun provideConversationDao(db: AppDatabase): ConversationDao = db.conversationDao()

    @Provides
    @Singleton
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    @Singleton
    fun provideServiceRepository(
        serviceApiService: ServiceApiService,
        serviceDao: ServiceDao,
        authManager: AuthManager,
    ): ServiceRepository {
        return ServiceRepositoryImpl(serviceApiService, serviceDao, authManager)
    }

    @Provides
    @Singleton
    fun provideReviewRepository(
        serviceApiService: ServiceApiService,
        userPreferences: UserPreferences,
    ): ReviewRepository {
        return ReviewRepositoryImpl(serviceApiService, userPreferences)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        @ApplicationContext context: Context,
        conversationApiService: ConversationApiService,
        chatWebSocketService: ChatWebSocketService,
        conversationDao: ConversationDao,
        messageDao: MessageDao,
        firebaseAuth: FirebaseAuth?,
    ): ChatRepository {
        return ChatRepositoryImpl(
            context,
            conversationApiService,
            conversationDao,
            messageDao,
            chatWebSocketService,
            firebaseAuth,
        )
    }

    @Provides
    @Singleton
    fun provideAiSearchRepository(discoveryApiService: DiscoveryApiService): AiSearchRepository =
        AiSearchRepositoryImpl(discoveryApiService)

    @Provides
    @Singleton
    fun provideNotificationRepository(notificationApiService: NotificationApiService): NotificationRepository {
        return NotificationRepositoryImpl(notificationApiService)
    }

    @Provides
    @Singleton
    fun provideMapRepository(discoveryApiService: DiscoveryApiService): MapRepository = MapRepositoryImpl(discoveryApiService)

    @Provides
    @Singleton
    fun provideReportRepository(
        reportApiService: must.kdroiders.hustlehub.ui.features.report.data.remote.ReportApiService,
    ): must.kdroiders.hustlehub.ui.features.report.domain.repository.ReportRepository {
        return must.kdroiders.hustlehub.ui.features.report.data.repository
            .ReportRepositoryImpl(reportApiService)
    }

    @Provides
    @Singleton
    fun providePrivacyRepository(
        privacyApiService: must.kdroiders.hustlehub.ui.features.privacy.data.remote.PrivacyApiService,
    ): must.kdroiders.hustlehub.ui.features.privacy.domain.repository.PrivacyRepository {
        return must.kdroiders.hustlehub.ui.features.privacy.data.repository.PrivacyRepositoryImpl(privacyApiService)
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

    override suspend fun logout() {}
}
