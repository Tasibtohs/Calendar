package com.example.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class SnoozeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(ReminderScheduler.EXTRA_EVENT_ID, 0L)
        val eventTitle = intent.getStringExtra(ReminderScheduler.EXTRA_EVENT_TITLE) ?: "Event"
        val snoozeMinutes = intent.getIntExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, 15)

        // Dismiss current notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(eventId.toInt())

        // Schedule snooze alarm
        ReminderScheduler.scheduleSnooze(context, eventId, eventTitle, snoozeMinutes)

        Toast.makeText(
            context,
            "Snoozed '$eventTitle' for $snoozeMinutes minutes",
            Toast.LENGTH_SHORT
        ).show()
    }
}
