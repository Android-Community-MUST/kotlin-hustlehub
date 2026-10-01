package must.kdroiders.hustlehub.core.review

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import must.kdroiders.hustlehub.datastore.ReviewMetrics
import must.kdroiders.hustlehub.datastore.ReviewPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class AppReviewManagerTest {
    private val context: Context = mockk(relaxed = true)
    private val reviewPreferences: ReviewPreferences = mockk(relaxed = true)
    private val reviewManager: ReviewManager = mockk(relaxed = true)
    private val activity: Activity = mockk(relaxed = true)

    private lateinit var appReviewManager: AppReviewManagerImpl

    @Before
    fun setUp() {
        every { context.packageName } returns "must.kdroiders.hustlehub"
        appReviewManager = AppReviewManagerImpl(context, reviewPreferences, reviewManager)
    }

    @Test
    fun `isEligibleForReview returns false when user has already rated the app`() =
        runTest {
            val now = System.currentTimeMillis()
            val metrics =
                ReviewMetrics(
                    firstInstallTime = now - TimeUnit.DAYS.toMillis(10),
                    appOpenCount = 10,
                    significantActionsCount = 5,
                    lastPromptTime = 0L,
                    hasRatedApp = true,
                )
            every { reviewPreferences.reviewMetrics } returns flowOf(metrics)

            assertFalse(appReviewManager.isEligibleForReview())
        }

    @Test
    fun `isEligibleForReview returns false when first install timestamp is not initialized`() =
        runTest {
            val metrics =
                ReviewMetrics(
                    firstInstallTime = 0L,
                    appOpenCount = 10,
                    significantActionsCount = 5,
                    lastPromptTime = 0L,
                    hasRatedApp = false,
                )
            every { reviewPreferences.reviewMetrics } returns flowOf(metrics)

            assertFalse(appReviewManager.isEligibleForReview())
        }

    @Test
    fun `isEligibleForReview returns false when days since install is less than threshold`() =
        runTest {
            val now = System.currentTimeMillis()
            val metrics =
                ReviewMetrics(
                    firstInstallTime = now - TimeUnit.DAYS.toMillis(1),
                    appOpenCount = 10,
                    significantActionsCount = 5,
                    lastPromptTime = 0L,
                    hasRatedApp = false,
                )
            every { reviewPreferences.reviewMetrics } returns flowOf(metrics)

            assertFalse(appReviewManager.isEligibleForReview())
        }

    @Test
    fun `isEligibleForReview returns false when app opens count is less than threshold`() =
        runTest {
            val now = System.currentTimeMillis()
            val metrics =
                ReviewMetrics(
                    firstInstallTime = now - TimeUnit.DAYS.toMillis(5),
                    appOpenCount = 3,
                    significantActionsCount = 5,
                    lastPromptTime = 0L,
                    hasRatedApp = false,
                )
            every { reviewPreferences.reviewMetrics } returns flowOf(metrics)

            assertFalse(appReviewManager.isEligibleForReview())
        }

    @Test
    fun `isEligibleForReview returns false when significant actions count is less than threshold`() =
        runTest {
            val now = System.currentTimeMillis()
            val metrics =
                ReviewMetrics(
                    firstInstallTime = now - TimeUnit.DAYS.toMillis(5),
                    appOpenCount = 6,
                    significantActionsCount = 1,
                    lastPromptTime = 0L,
                    hasRatedApp = false,
                )
            every { reviewPreferences.reviewMetrics } returns flowOf(metrics)

            assertFalse(appReviewManager.isEligibleForReview())
        }

    @Test
    fun `isEligibleForReview returns false when cool-down period has not elapsed`() =
        runTest {
            val now = System.currentTimeMillis()
            val metrics =
                ReviewMetrics(
                    firstInstallTime = now - TimeUnit.DAYS.toMillis(40),
                    appOpenCount = 10,
                    significantActionsCount = 5,
                    lastPromptTime = now - TimeUnit.DAYS.toMillis(20),
                    hasRatedApp = false,
                )
            every { reviewPreferences.reviewMetrics } returns flowOf(metrics)

            assertFalse(appReviewManager.isEligibleForReview())
        }

    @Test
    fun `isEligibleForReview returns true when all thresholds are satisfied`() =
        runTest {
            val now = System.currentTimeMillis()
            val metrics =
                ReviewMetrics(
                    firstInstallTime = now - TimeUnit.DAYS.toMillis(10),
                    appOpenCount = 7,
                    significantActionsCount = 3,
                    lastPromptTime = now - TimeUnit.DAYS.toMillis(70),
                    hasRatedApp = false,
                )
            every { reviewPreferences.reviewMetrics } returns flowOf(metrics)

            assertTrue(appReviewManager.isEligibleForReview())
        }

    @Test
    fun `launchReviewIfEligible skips review when ineligible`() =
        runTest {
            val metrics = ReviewMetrics(hasRatedApp = true)
            every { reviewPreferences.reviewMetrics } returns flowOf(metrics)

            val launched = appReviewManager.launchReviewIfEligible(activity)

            assertFalse(launched)
            verify(exactly = 0) { reviewManager.requestReviewFlow() }
        }

    @Test
    fun `launchReviewIfEligible launches review and records timestamp when eligible`() =
        runTest {
            val now = System.currentTimeMillis()
            val metrics =
                ReviewMetrics(
                    firstInstallTime = now - TimeUnit.DAYS.toMillis(10),
                    appOpenCount = 10,
                    significantActionsCount = 5,
                    lastPromptTime = 0L,
                    hasRatedApp = false,
                )
            every { reviewPreferences.reviewMetrics } returns flowOf(metrics)

            val mockReviewInfo: ReviewInfo = mockk()
            every { reviewManager.requestReviewFlow() } returns mockSuccessfulTask(mockReviewInfo)
            every { reviewManager.launchReviewFlow(activity, mockReviewInfo) } returns mockSuccessfulTask(null)

            val launched = appReviewManager.launchReviewIfEligible(activity)

            assertTrue(launched)
            coVerify(exactly = 1) { reviewPreferences.recordPromptShown() }
        }

    @Test
    fun `launchDirectReview completes review and records prompt`() =
        runTest {
            val mockReviewInfo: ReviewInfo = mockk()
            every { reviewManager.requestReviewFlow() } returns mockSuccessfulTask(mockReviewInfo)
            every { reviewManager.launchReviewFlow(activity, mockReviewInfo) } returns mockSuccessfulTask(null)

            val launched = appReviewManager.launchDirectReview(activity)

            assertTrue(launched)
            coVerify(exactly = 1) { reviewPreferences.recordPromptShown() }
        }

    @Test
    fun `launchDirectReview returns false gracefully on task failure`() =
        runTest {
            every { reviewManager.requestReviewFlow() } returns mockFailedTask(RuntimeException("Network error"))

            val launched = appReviewManager.launchDirectReview(activity)

            assertFalse(launched)
            coVerify(exactly = 0) { reviewPreferences.recordPromptShown() }
        }

    private fun <T> mockSuccessfulTask(result: T): Task<T> {
        val task: Task<T> = mockk()
        every { task.isSuccessful } returns true
        every { task.result } returns result
        every { task.exception } returns null
        every { task.addOnCompleteListener(any()) } answers {
            val listener = firstArg<OnCompleteListener<T>>()
            listener.onComplete(task)
            task
        }
        return task
    }

    private fun <T> mockFailedTask(exception: Exception): Task<T> {
        val task: Task<T> = mockk()
        every { task.isSuccessful } returns false
        every { task.result } returns null as T
        every { task.exception } returns exception
        every { task.addOnCompleteListener(any()) } answers {
            val listener = firstArg<OnCompleteListener<T>>()
            listener.onComplete(task)
            task
        }
        return task
    }

    @Test
    fun `recordAppOpen delegates to reviewPreferences`() =
        runTest {
            appReviewManager.recordAppOpen()
            coVerify(exactly = 1) { reviewPreferences.recordAppOpen() }
        }

    @Test
    fun `recordSignificantAction delegates to reviewPreferences`() =
        runTest {
            appReviewManager.recordSignificantAction()
            coVerify(exactly = 1) { reviewPreferences.recordSignificantAction() }
        }

    @Test
    fun `openPlayStorePage launches market intent`() {
        val slot = slot<Intent>()
        every { context.startActivity(capture(slot)) } returns Unit

        appReviewManager.openPlayStorePage(context)

        assertEquals("market://details?id=must.kdroiders.hustlehub", slot.captured.data.toString())
    }

    @Test
    fun `openPlayStorePage falls back to web URL if Play Store app is not installed`() {
        val intents = mutableListOf<Intent>()
        every { context.startActivity(any()) } throws ActivityNotFoundException() andThenAnswer { }

        appReviewManager.openPlayStorePage(context)

        verify(exactly = 2) { context.startActivity(capture(intents)) }
        assertEquals(
            "market://details?id=must.kdroiders.hustlehub",
            intents.first().data.toString(),
        )
        assertEquals(
            "https://play.google.com/store/apps/details?id=must.kdroiders.hustlehub",
            intents.last().data.toString(),
        )
    }
}
