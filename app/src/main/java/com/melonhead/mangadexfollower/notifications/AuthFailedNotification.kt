package com.melonhead.mangadexfollower.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.melonhead.mangadexfollower.R
import com.melonhead.lib_logging.Clog
import com.melonhead.mangadexfollower.ui.scenes.MainActivity
import kotlin.random.Random


object AuthFailedNotification {
    private const val CHANNEL_ID = "auth_failed"
    private const val CHANNEL_NAME = "Authentication Issues"

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(context: Context): Notification {
        val contentIntent = PendingIntent.getActivity(
            context, 1, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Authentication Failed")
            .setContentText("Tap to sign in again.")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
    }

    fun postAuthFailed(context: Context) {
        // set up channel
        createNotificationChannel(context)

        val notificationManager = NotificationManagerCompat.from(context)

        Clog.i("postAuthFailed: Auth failed")
        val notification = buildNotification(context)
        notificationManager.notify(Random.nextInt(), notification)
    }
}
