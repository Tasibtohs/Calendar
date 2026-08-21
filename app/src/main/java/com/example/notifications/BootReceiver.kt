package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.CalendarDatabase
import com.example.data.repository.CalendarRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = CalendarDatabase.getInstance(context)
            val repository = CalendarRepository(db)

            CoroutineScope(Dispatchers.IO).launch {
                val now = System.currentTimeMillis()
                val events = repository.getUpcomingEvents(now, limit = 100).first()
                events.forEach { event ->
                    ReminderScheduler.scheduleEventReminders(context, event)
                }
            }
        }
    }
}
