package must.kdroiders.hustlehub.appHilt

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import must.kdroiders.hustlehub.BuildConfig
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class HustleHubApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var imageLoaderProvider: Provider<ImageLoader>

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Timber.e(e, "Firebase initialization failed")
        }
    }

    override fun newImageLoader(): ImageLoader = imageLoaderProvider.get()
}
