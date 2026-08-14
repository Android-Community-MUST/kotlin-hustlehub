package must.kdroiders.hustlehub.core.telemetry

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HustleCrashlytics
    @Inject
    constructor(
        private val crashlytics: FirebaseCrashlytics?,
    ) {
        fun setCrashlyticsUserContext(
            userId: String,
            screen: String,
            backendVersion: String = "4.0.0-SpringBoot",
        ) {
            crashlytics?.setUserId(userId)
            crashlytics?.setCustomKey("screen", screen)
            crashlytics?.setCustomKey("backend_version", backendVersion)
        }

        fun setScreen(screenName: String) {
            crashlytics?.setCustomKey("screen", screenName)
        }

        fun recordNonFatal(
            e: Throwable,
            screenName: String? = null,
        ) {
            screenName?.let { crashlytics?.setCustomKey("screen", it) }
            crashlytics?.recordException(e)
        }

        fun triggerTestCrash() {
            throw RuntimeException("Test Crash - HustleHub Crashlytics Verification")
        }
    }
