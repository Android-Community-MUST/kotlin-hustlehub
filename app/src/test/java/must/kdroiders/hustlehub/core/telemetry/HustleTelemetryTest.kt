package must.kdroiders.hustlehub.core.telemetry

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class HustleTelemetryTest {

    @Before
    fun setup() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putFloat(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(Bundle::class)
    }

    @Test
    fun `HustleCrashlytics sets user context correctly`() {
        val mockCrashlytics = mockk<FirebaseCrashlytics>(relaxed = true)
        val helper = HustleCrashlytics(mockCrashlytics)

        helper.setCrashlyticsUserContext("user123", "HomeScreen", "4.0.0-SpringBoot")

        verify { mockCrashlytics.setUserId("user123") }
        verify { mockCrashlytics.setCustomKey("screen", "HomeScreen") }
        verify { mockCrashlytics.setCustomKey("backend_version", "4.0.0-SpringBoot") }
    }

    @Test
    fun `HustleAnalytics sets user properties correctly`() {
        val mockAnalytics = mockk<FirebaseAnalytics>(relaxed = true)
        val helper = HustleAnalytics(mockAnalytics)

        helper.setUserProperties("PROVIDER", "Main Campus", true)

        verify { mockAnalytics.setUserProperty("role", "PROVIDER") }
        verify { mockAnalytics.setUserProperty("campus", "Main Campus") }
        verify { mockAnalytics.setUserProperty("is_verified_pro", "true") }
    }

    @Test
    fun `HustleAnalytics logs events correctly`() {
        val mockAnalytics = mockk<FirebaseAnalytics>(relaxed = true)
        val helper = HustleAnalytics(mockAnalytics)

        helper.logSignupCompleted("email")
        helper.logServiceCreated("service123", "TUTORING")
        helper.logMapOpened()

        verify { mockAnalytics.logEvent("signup_completed", any()) }
        verify { mockAnalytics.logEvent("service_created", any()) }
        verify { mockAnalytics.logEvent("map_opened", null) }
    }
}
