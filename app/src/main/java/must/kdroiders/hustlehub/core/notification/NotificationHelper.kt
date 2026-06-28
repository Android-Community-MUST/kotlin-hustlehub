package must.kdroiders.hustlehub.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.activities.MainActivity
import timber.log.Timber

/**
 * Manages local push notifications for incoming chat messages.
 *
 * Why this exists:
 * On Android 8.0+ (API 26), the OS drives the app-icon badge entirely from active
 * notifications. By posting a local notification each time a message arrives from
 * another user, we get:
 *   - App icon badge count (free, via OS)
 *   - A visible notification in the drawer (ready for FCM to replace later)
 *
 * When FCM is integrated, FCM will trigger [postMessageNotification] from the
 * FirebaseMessagingService instead of from the WebSocket listener, and this class
 * requires zero changes.
 */
object NotificationHelper {
    const val CHANNEL_ID = "hustlehub_messages"
    private const val CHANNEL_NAME = "Messages"
    private const val CHANNEL_DESCRIPTION = "Incoming chat message notifications"

    /**
     * Creates the notification channel on Android 8.0+.
     * Safe to call multiple times — the OS is idempotent.
     * Call once from [Application.onCreate] or [MainActivity.onCreate].
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Posts a heads-up notification for a new message.
     * On Android 8+, this automatically increments the app icon badge.
     *
     * @param notificationId  stable ID — use a hash of conversationId so all
     *                        messages from the same conversation update one slot.
     * @param senderName      display name shown as the notification title.
     * @param messagePreview  short preview of message content for the notification body.
     */
    fun postMessageNotification(
        context: Context,
        notificationId: Int,
        senderName: String,
        messagePreview: String,
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(senderName)
                .setContentText(messagePreview)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted (Android 13+). Silently ignore.
            Timber.w(e, "POST_NOTIFICATIONS permission not granted; skipping notification")
        } catch (e: Exception) {
            Timber.e(e, "Failed to post message notification")
        }
    }

    /**
     * Cancels all notifications for a given conversation.
     * Call when the user opens a conversation — clears the app icon badge slot for
     * that conversation.
     */
    fun cancelConversationNotification(
        context: Context,
        conversationId: String,
    ) {
        NotificationManagerCompat.from(context).cancel(conversationId.hashCode())
    }

    /**
     * Cancels ALL chat notifications (e.g. when the user reads everything).
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
