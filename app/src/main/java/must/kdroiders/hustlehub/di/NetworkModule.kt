package must.kdroiders.hustlehub.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.core.api.AuthInterceptor
import must.kdroiders.hustlehub.core.api.TokenAuthenticator
import must.kdroiders.hustlehub.core.auth.AuthManager
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.AnalyticsApiService
import must.kdroiders.hustlehub.ui.features.auth.data.remote.AuthApiService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.ConversationApiService
import must.kdroiders.hustlehub.ui.features.chat.data.remote.KeyExchangeApiService
import must.kdroiders.hustlehub.ui.features.home.data.remote.DiscoveryApiService
import must.kdroiders.hustlehub.ui.features.media.data.remote.MediaApiService
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.PaymentApiService
import must.kdroiders.hustlehub.ui.features.notification.data.remote.NotificationApiService
import must.kdroiders.hustlehub.ui.features.privacy.data.remote.PrivacyApiService
import must.kdroiders.hustlehub.ui.features.profile.data.remote.UserApiService
import must.kdroiders.hustlehub.ui.features.report.data.remote.ReportApiService
import must.kdroiders.hustlehub.ui.features.service.data.remote.ServiceApiService
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideAuthInterceptor(firebaseAuth: FirebaseAuth?): AuthInterceptor {
        return AuthInterceptor(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        firebaseAuth: FirebaseAuth?,
        authManager: AuthManager,
    ): TokenAuthenticator {
        return TokenAuthenticator(firebaseAuth, authManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        val cache = Cache(File(context.cacheDir, "okhttp"), 10L * 1024L * 1024L)

        val builder = OkHttpClient
            .Builder()
            .cache(cache)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        // Certificate pinning — release builds only.
        // In debug, we allow user-installed CAs (Charles/mitmproxy).
        if (!BuildConfig.DEBUG) {
            val baseHost = runCatching { java.net.URI(BuildConfig.BASE_URL).host }.getOrNull()
            val pinnerBuilder = CertificatePinner.Builder()

            // Google Trust Services (GTS) Root & Intermediate CA chain for Cloud Run & custom domains
            val gtsPins = arrayOf(
                // GTS Root CAs (Permanent Google Root Authorities)
                "sha256/hxqRlPTu1bMS/0DITB1SSu0vd4u/8l8TjPgfaAp63Gc=", // GTS Root R1
                "sha256/Vfd95BwDeSQo+NUYxVEEecvvnnJtDeM8SOr0g8jfZ28=", // GTS Root R2
                "sha256/QXnt2YHvdHR3tJYmQIr0Paosp6t/YM2SqRGH0CTRem8=", // GTS Root R3
                "sha256/mEfIZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=", // GTS Root R4 (active Cloud Run root)
                "sha256/K87oWBWM9UZfyddvDfoxL+8IpNyoUB2ptGtn0fv6G2Q=", // GlobalSign Root CA (GTS cross-signer)
                // Active Intermediates & Leaf
                "sha256/vh78KSg1Ry4NaqGDV10w/cTb9VH3BQUZoCWNa93W/EY=", // GTS Intermediate WE2
                "sha256/9voVCSg/xBZgKjiuM901dNEPYEcqSJm381lxRK7hIDc=", // *.a.run.app Leaf
                "sha256/YPtHaftLw6/0vnc2BnNKGF54xiCA28WFcccjkA4ypCM=", // GTS WR2 Intermediate
                "sha256/8jVhONRfoLxp9xEO7Gc/HdRfHqtEqkqd44YdfeZq5Wo=", // GTS Leaf
            )

            pinnerBuilder.add("api.hustlehub.app", *gtsPins)

            if (!baseHost.isNullOrEmpty() && baseHost != "api.hustlehub.app") {
                pinnerBuilder.add(baseHost, *gtsPins)
            }

            builder.certificatePinner(pinnerBuilder.build())
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMediaApiService(retrofit: Retrofit): MediaApiService {
        return retrofit.create(MediaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideServiceApiService(retrofit: Retrofit): ServiceApiService {
        return retrofit.create(ServiceApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideConversationApiService(retrofit: Retrofit): ConversationApiService {
        return retrofit.create(ConversationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDiscoveryApiService(retrofit: Retrofit): DiscoveryApiService = retrofit.create(DiscoveryApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService {
        return retrofit.create(NotificationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideReportApiService(retrofit: Retrofit): ReportApiService {
        return retrofit.create(ReportApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePrivacyApiService(retrofit: Retrofit): PrivacyApiService {
        return retrofit.create(PrivacyApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePaymentApiService(retrofit: Retrofit): PaymentApiService = retrofit.create(PaymentApiService::class.java)

    @Provides
    @Singleton
    fun provideAnalyticsApiService(retrofit: Retrofit): AnalyticsApiService = retrofit.create(AnalyticsApiService::class.java)

    @Provides
    @Singleton
    fun provideKeyExchangeApiService(retrofit: Retrofit): KeyExchangeApiService = retrofit.create(KeyExchangeApiService::class.java)
}
