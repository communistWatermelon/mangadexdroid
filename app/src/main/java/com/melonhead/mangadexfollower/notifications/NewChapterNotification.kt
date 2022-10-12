package com.melonhead.mangadexfollower.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.melonhead.mangadexfollower.R
import com.melonhead.mangadexfollower.logs.Clog
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import kotlinx.coroutines.delay

object NewChapterNotification {
    private val TAG = NewChapterNotification::class.simpleName!!
    private const val CHANNEL_ID = "new_chapters"
    private const val CHANNEL_NAME = "New Chapter"

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun pendingIntent(context: Context, uiChapter: UIChapter): PendingIntent? {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(uiChapter.webAddress)
        }

        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }
    }

    private fun buildNotification(context: Context, pendingIntent: PendingIntent, uiManga: UIManga, uiChapter: UIChapter): Notification {
        val text = if (uiChapter.title.isNullOrBlank()) uiChapter.chapter else "${uiChapter.chapter} - ${uiChapter.title}"
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(uiManga.title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    suspend fun post(context: Context, series: List<UIManga>, installDateSeconds: Long) {
        // set up channel
        createNotificationChannel(context)

        val notificationManager = NotificationManagerCompat.from(context)

        Clog.i(TAG, "post: New chapters for ${series.count()} manga")
        series.forEach { manga ->
            manga.chapters.filter { it.createdDate.epochSeconds >= installDateSeconds }.forEach chapters@{ uiChapter ->
                val pendingIntent = pendingIntent(context, uiChapter) ?: return@chapters
                val notification = buildNotification(context, pendingIntent, manga, uiChapter)
                notificationManager.notify(manga.id.hashCode() + uiChapter.id.hashCode(), notification)
                // ensures android actually posts all notifications
                delay(1000)
            }
        }
    }
}