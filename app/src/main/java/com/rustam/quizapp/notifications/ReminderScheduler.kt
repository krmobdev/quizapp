package com.rustam.quizapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rustam.quizapp.MainActivity
import com.rustam.quizapp.R
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules and posts the daily "don't lose your streak" reminder.
 *
 * A unique periodic [WorkManager] job wakes up once a day around [REMINDER_HOUR] and, if
 * the player hasn't finished a quiz that day, posts a local notification.
 */
object ReminderScheduler {

    private const val CHANNEL_ID = "streak_reminders"
    private const val WORK_NAME = "streak_reminder_daily"
    private const val NOTIFICATION_ID = 1001
    private const val REMINDER_HOUR = 20

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.reminder_channel_desc) }
        manager.createNotificationChannel(channel)
    }

    fun scheduleDaily(context: Context) {
        val request = PeriodicWorkRequestBuilder<StreakReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMinutes(), TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /** Minutes from now until the next occurrence of [REMINDER_HOUR]:00 local time. */
    private fun initialDelayMinutes(): Long {
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(REMINDER_HOUR, 0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMinutes().coerceAtLeast(1)
    }

    fun showReminder(context: Context, streak: Int) {
        val notifier = NotificationManagerCompat.from(context)
        if (!notifier.areNotificationsEnabled()) return
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val text = if (streak > 0) {
            context.getString(R.string.reminder_streak_text, streak)
        } else {
            context.getString(R.string.reminder_text)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            notifier.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission was revoked between the check and notify; nothing to do.
        }
    }
}
