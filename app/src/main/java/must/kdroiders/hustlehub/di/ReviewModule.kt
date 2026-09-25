package must.kdroiders.hustlehub.di

import android.content.Context
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.core.review.AppReviewManager
import must.kdroiders.hustlehub.core.review.AppReviewManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReviewModule {
    @Provides
    @Singleton
    fun provideReviewManager(
        @ApplicationContext context: Context,
    ): ReviewManager {
        return if (BuildConfig.DEBUG) {
            FakeReviewManager(context)
        } else {
            ReviewManagerFactory.create(context)
        }
    }

    @Provides
    @Singleton
    fun provideAppReviewManager(
        impl: AppReviewManagerImpl,
    ): AppReviewManager = impl
}
