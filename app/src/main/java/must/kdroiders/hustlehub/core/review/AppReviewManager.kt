package must.kdroiders.hustlehub.core.review

import android.app.Activity
import android.content.Context

interface AppReviewManager {
    suspend fun isEligibleForReview(): Boolean

    suspend fun recordAppOpen()

    suspend fun recordSignificantAction()

    suspend fun launchReviewIfEligible(activity: Activity): Boolean

    suspend fun launchDirectReview(activity: Activity): Boolean

    fun openPlayStorePage(context: Context)
}

fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}
