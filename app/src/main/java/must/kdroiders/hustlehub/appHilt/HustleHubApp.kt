package must.kdroiders.hustlehub.appHilt

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import must.kdroiders.hustlehub.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class HustleHubApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging (optional)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}