package must.kdroiders.hustlehub.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.activities.MainActivity
import timber.log.Timber

object NotificationHelper {
    const val CHANNEL_MESSAGES = "messages"
    const val CHANNEL_REVIEWS = "reviews"
    const val CHANNEL_INQUIRIES = "inquiries"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(CHANNEL_MESSAGES, "Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Incoming chat message notifications"
                },
                NotificationChannel(CHANNEL_REVIEWS, "Reviews", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Reviews and feedback notifications"
                },
                NotificationChannel(CHANNEL_INQUIRIES, "Inquiries", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Service inquiry notifications"
                },
            )
            manager.createNotificationChannels(channels)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create notification channels")
        }
    }

    fun postMessageNotification(
        context: Context,
        conversationId: String,
        senderName: String,
        messagePreview: String,
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                data = Uri.parse("hustlehub://app/chat?conversationId=$conversationId")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                conversationId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat
                .Builder(context, CHANNEL_MESSAGES)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(senderName)
                .setContentText(messagePreview)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(conversationId.hashCode(), notification)
        } catch (e: SecurityException) {
            Timber.w(e, "POST_NOTIFICATIONS permission not granted; skipping notification")
        } catch (e: Exception) {
            Timber.e(e, "Failed to post message notification")
        }
    }

    fun postReviewNotification(
        context: Context,
        title: String,
        body: String,
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                data = Uri.parse("hustlehub://app/profile")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                200,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat
                .Builder(context, CHANNEL_REVIEWS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(200, notification)
        } catch (e: SecurityException) {
            Timber.w(e, "POST_NOTIFICATIONS permission not granted; skipping notification")
        } catch (e: Exception) {
            Timber.e(e, "Failed to post review notification")
        }
    }

    fun postInquiryNotification(
        context: Context,
        title: String,
        body: String,
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                data = Uri.parse("hustlehub://app/inquiries")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                300,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat
                .Builder(context, CHANNEL_INQUIRIES)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(300, notification)
        } catch (e: SecurityException) {
            Timber.w(e, "POST_NOTIFICATIONS permission not granted; skipping notification")
        } catch (e: Exception) {
            Timber.e(e, "Failed to post inquiry notification")
        }
    }

    fun cancelConversationNotification(
        context: Context,
        conversationId: String,
    ) {
        NotificationManagerCompat.from(context).cancel(conversationId.hashCode())
    }

    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
