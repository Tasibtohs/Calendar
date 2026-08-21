package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.Event
import com.example.util.CalendarUtils

object ReminderScheduler {

    const val EXTRA_EVENT_ID = "extra_event_id"
    const val EXTRA_EVENT_TITLE = "extra_event_title"
    const val EXTRA_EVENT_LOCATION = "extra_event_location"
    const val EXTRA_EVENT_TIME = "extra_event_time"
    const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"

    private fun getRequestCode(eventId: Long, offsetIndex: Int): Int {
        return ((eventId.hashCode() and 0x7FFF) * 100 + (offsetIndex and 0x3F))
    }

    fun scheduleEventReminders(context: Context, event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Parse reminder minutes list (e.g. "0,15,60")
        val reminderOffsets = parseReminderMinutes(event.reminderMinutesList)

        reminderOffsets.forEachIndexed { index, minutesBefore ->
            val triggerTimeMs = event.startDate - (minutesBefore * 60 * 1000L)
            if (triggerTimeMs > System.currentTimeMillis()) {
                val requestCode = getRequestCode(event.id, index)

                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra(EXTRA_EVENT_ID, event.id)
                    putExtra(EXTRA_EVENT_TITLE, event.title)
                    putExtra(EXTRA_EVENT_LOCATION, event.location)
                    putExtra(EXTRA_EVENT_TIME, CalendarUtils.formatDate(event.startDate, "hh:mm a, d MMM yyyy"))
                }

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

                try {
                    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        alarmManager.canScheduleExactAlarms()
                    } else {
                        true
                    }

                    if (canScheduleExact) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ReminderScheduler", "Permission error setting alarm", e)
                    try {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun cancelEventReminders(context: Context, eventId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        (0..10).forEach { index ->
            val requestCode = getRequestCode(eventId, index)
            val intent = Intent(context, AlarmReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
            try {
                alarmManager.cancel(pendingIntent)
            } catch (_: Exception) {}
        }
    }

    fun scheduleSnooze(context: Context, eventId: Long, eventTitle: String, snoozeMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTimeMs = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val requestCode = getRequestCode(eventId, 99)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_EVENT_ID, eventId)
            putExtra(EXTRA_EVENT_TITLE, "$eventTitle (Snoozed)")
            putExtra(EXTRA_EVENT_LOCATION, "")
            putExtra(EXTRA_EVENT_TIME, "Snoozed reminder")
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        try {
            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canScheduleExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
                }
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed setting snooze alarm", e)
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            } catch (_: Exception) {}
        }
    }

    fun parseReminderMinutes(reminderStr: String): List<Int> {
        if (reminderStr.isBlank()) return listOf(15)
        return reminderStr.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .ifEmpty { listOf(15) }
    }
}
