package must.kdroiders.hustlehub.appHilt

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.core.telemetry.HustleAnalytics
import must.kdroiders.hustlehub.core.telemetry.HustleCrashlytics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class HustleHubApp : Application(), ImageLoaderFactory, Configuration.Provider {
    @Inject
    lateinit var imageLoaderProvider: Provider<ImageLoader>

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var hustleAnalytics: HustleAnalytics

    @Inject
    lateinit var hustleCrashlytics: HustleCrashlytics

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        try {
            FirebaseApp.initializeApp(this)
            hustleAnalytics.setCollectionEnabled(!BuildConfig.DEBUG)
            hustleCrashlytics.setScreen("HustleHubApp")
        } catch (e: Exception) {
            Timber.e(e, "Firebase initialization failed")
        }
    }

    override fun newImageLoader(): ImageLoader = imageLoaderProvider.get()

    override val workManagerConfiguration: Configuration
        get() = Configuration
            .Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
