package must.kdroiders.hustlehub.core.notification

import android.content.Context
import me.leolin.shortcutbadger.ShortcutBadger
import timber.log.Timber

object AppBadgeHelper {
    /**
     * Updates the app launcher icon badge count across device launchers (Android 8+ & OEM launchers).
     * If [count] is > 0, sets badge count; otherwise removes the badge.
     */
    fun applyBadgeCount(
        context: Context,
        count: Int,
    ) {
        try {
            if (count > 0) {
                ShortcutBadger.applyCount(context, count)
            } else {
                ShortcutBadger.removeCount(context)
            }
        } catch (e: Throwable) {
            Timber.w(e, "Failed to update launcher app icon badge count: $count")
        }
    }
}
