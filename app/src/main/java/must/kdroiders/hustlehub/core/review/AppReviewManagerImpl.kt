package must.kdroiders.hustlehub.core.review

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import must.kdroiders.hustlehub.BuildConfig
import must.kdroiders.hustlehub.datastore.ReviewPreferences
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AppReviewManagerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val reviewPreferences: ReviewPreferences,
        private val reviewManager: ReviewManager,
    ) : AppReviewManager {
        internal companion object {
            const val MIN_DAYS_SINCE_INSTALL = 3L
            const val MIN_APP_OPENS = 5
            const val MIN_SIGNIFICANT_ACTIONS = 2
            const val MIN_DAYS_BETWEEN_PROMPTS = 60L

            const val PLAY_STORE_MARKET_SCHEME = "market://details?id="
            const val PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id="
        }

        override suspend fun isEligibleForReview(): Boolean {
            val metrics = reviewPreferences.reviewMetrics.first()
            if (metrics.hasRatedApp) {
                Timber.d("AppReviewManager: User has already rated the app.")
                return false
            }

            val now = System.currentTimeMillis()
            if (metrics.firstInstallTime <= 0L) {
                Timber.d("AppReviewManager: First install timestamp not initialized yet.")
                return false
            }

            val daysSinceInstall = TimeUnit.MILLISECONDS.toDays(now - metrics.firstInstallTime)
            if (daysSinceInstall < MIN_DAYS_SINCE_INSTALL) {
                Timber.d(
                    "AppReviewManager: Days since install ($daysSinceInstall) < required ($MIN_DAYS_SINCE_INSTALL).",
                )
                return false
            }

            if (metrics.appOpenCount < MIN_APP_OPENS) {
                Timber.d(
                    "AppReviewManager: App opens (${metrics.appOpenCount}) < required ($MIN_APP_OPENS).",
                )
                return false
            }

            if (metrics.significantActionsCount < MIN_SIGNIFICANT_ACTIONS) {
                Timber.d(
                    "AppReviewManager: Actions (${metrics.significantActionsCount}) < required ($MIN_SIGNIFICANT_ACTIONS).",
                )
                return false
            }

            if (metrics.lastPromptTime > 0L) {
                val daysSinceLastPrompt = TimeUnit.MILLISECONDS.toDays(now - metrics.lastPromptTime)
                if (daysSinceLastPrompt < MIN_DAYS_BETWEEN_PROMPTS) {
                    Timber.d(
                        "AppReviewManager: Days since prompt ($daysSinceLastPrompt) < required ($MIN_DAYS_BETWEEN_PROMPTS).",
                    )
                    return false
                }
            }

            return true
        }

        override suspend fun recordAppOpen() {
            reviewPreferences.recordAppOpen()
        }

        override suspend fun recordSignificantAction() {
            reviewPreferences.recordSignificantAction()
        }

        override suspend fun launchReviewIfEligible(activity: Activity): Boolean {
            if (!isEligibleForReview()) {
                Timber.d("AppReviewManager: Review criteria not met; skipping prompt.")
                return false
            }
            return executeReviewFlow(activity)
        }

        override suspend fun launchDirectReview(activity: Activity): Boolean {
            return executeReviewFlow(activity)
        }

        override fun openPlayStorePage(context: Context) {
            val packageName = context.packageName
            val marketUri = Uri.parse("$PLAY_STORE_MARKET_SCHEME$packageName")
            val marketIntent =
                Intent(Intent.ACTION_VIEW, marketUri).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK,
                    )
                }

            try {
                context.startActivity(marketIntent)
            } catch (e: ActivityNotFoundException) {
                Timber.d(e, "Play Store app not found; falling back to web browser.")
                val webUri = Uri.parse("$PLAY_STORE_WEB_URL$packageName")
                val webIntent =
                    Intent(Intent.ACTION_VIEW, webUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(webIntent)
            }
        }

        private suspend fun executeReviewFlow(activity: Activity): Boolean {
            return try {
                val reviewInfo =
                    suspendCancellableCoroutine<ReviewInfo> { continuation ->
                        val request = reviewManager.requestReviewFlow()
                        request.addOnCompleteListener { task ->
                            if (task.isSuccessful && task.result != null) {
                                continuation.resume(task.result)
                            } else {
                                continuation.resumeWithException(
                                    task.exception
                                        ?: IllegalStateException("Failed to retrieve ReviewInfo"),
                                )
                            }
                        }
                    }

                suspendCancellableCoroutine<Unit> { continuation ->
                    val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        continuation.resume(Unit)
                    }
                }

                reviewPreferences.recordPromptShown()
                Timber.d("AppReviewManager: In-app review flow completed.")
                if (BuildConfig.DEBUG) {
                    activity.runOnUiThread {
                        Toast
                            .makeText(
                                activity,
                                "Debug: In-app review flow triggered successfully",
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
                true
            } catch (e: Exception) {
                Timber.w(e, "AppReviewManager: In-app review flow failed.")
                false
            }
        }
    }
