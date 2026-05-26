package must.kdroiders.hustlehub.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.core.api.AuthInterceptor
import must.kdroiders.hustlehub.core.api.TokenAuthenticator
import must.kdroiders.hustlehub.ui.auth.data.remote.AuthApiService
import must.kdroiders.hustlehub.data.remote.UserApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun provideTokenAuthenticator(firebaseAuth: FirebaseAuth?): TokenAuthenticator {
        return TokenAuthenticator(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
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
    fun provideMediaApiService(retrofit: Retrofit): must.kdroiders.hustlehub.data.remote.MediaApiService {
        return retrofit.create(must.kdroiders.hustlehub.data.remote.MediaApiService::class.java)
    }
}
