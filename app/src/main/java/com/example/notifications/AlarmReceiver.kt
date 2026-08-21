package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(ReminderScheduler.EXTRA_EVENT_ID, 0L)
        val eventTitle = intent.getStringExtra(ReminderScheduler.EXTRA_EVENT_TITLE) ?: "Calendar Event"
        val eventLocation = intent.getStringExtra(ReminderScheduler.EXTRA_EVENT_LOCATION) ?: ""
        val eventTime = intent.getStringExtra(ReminderScheduler.EXTRA_EVENT_TIME) ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "calendar_event_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Calendar Event Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming scheduled calendar events"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Content intent to open MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            contentIntent,
            flags
        )

        // Snooze 5 mins Action Intent
        val snooze5Intent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra(ReminderScheduler.EXTRA_EVENT_ID, eventId)
            putExtra(ReminderScheduler.EXTRA_EVENT_TITLE, eventTitle)
            putExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, 5)
        }
        val snooze5PendingIntent = PendingIntent.getBroadcast(
            context,
            (eventId * 10 + 1).toInt(),
            snooze5Intent,
            flags
        )

        // Snooze 15 mins Action Intent
        val snooze15Intent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra(ReminderScheduler.EXTRA_EVENT_ID, eventId)
            putExtra(ReminderScheduler.EXTRA_EVENT_TITLE, eventTitle)
            putExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, 15)
        }
        val snooze15PendingIntent = PendingIntent.getBroadcast(
            context,
            (eventId * 10 + 2).toInt(),
            snooze15Intent,
            flags
        )

        // Snooze 1 Hour Action Intent
        val snooze60Intent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra(ReminderScheduler.EXTRA_EVENT_ID, eventId)
            putExtra(ReminderScheduler.EXTRA_EVENT_TITLE, eventTitle)
            putExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, 60)
        }
        val snooze60PendingIntent = PendingIntent.getBroadcast(
            context,
            (eventId * 10 + 3).toInt(),
            snooze60Intent,
            flags
        )

        val subText = if (eventLocation.isNotBlank()) "$eventTime • 📍 $eventLocation" else eventTime

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("📅 $eventTitle")
            .setContentText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$subText\nReminder for your upcoming scheduled event."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Snooze 5m", snooze5PendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Snooze 15m", snooze15PendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Snooze 1h", snooze60PendingIntent)
            .build()

        notificationManager.notify(eventId.toInt(), notification)
    }
}
