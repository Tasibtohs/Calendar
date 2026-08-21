package com.example.util

import com.example.data.model.Event
import java.util.Calendar

object RecurringEventUtils {

    /**
     * Generates a list of occurrence events based on masterEvent's repeat parameters.
     * Note: masterEvent must already have an assigned id.
     */
    fun generateOccurrences(masterEvent: Event): List<Event> {
        if (masterEvent.repeatType == "NONE") return emptyList()

        val occurrences = mutableListOf<Event>()
        val durationMs = masterEvent.endDate - masterEvent.startDate

        val calStart = Calendar.getInstance().apply { timeInMillis = masterEvent.startDate }
        val calUntil = masterEvent.repeatUntilDate?.let { Calendar.getInstance().apply { timeInMillis = it } }

        var count = 0
        val maxCount = when (masterEvent.repeatEndType) {
            "COUNT" -> masterEvent.repeatCount.coerceAtLeast(1)
            "DATE" -> 365 // Safety cap for date-bound repeats
            else -> 30 // Default 30 occurrences for "NEVER"
        }

        // Loop generating occurrences after the master event (index 1 to maxCount - 1)
        while (count < maxCount - 1) {
            count++

            when (masterEvent.repeatType) {
                "DAILY" -> calStart.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> calStart.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> calStart.add(Calendar.MONTH, 1)
                "YEARLY" -> calStart.add(Calendar.YEAR, 1)
                "CUSTOM" -> calStart.add(Calendar.DAY_OF_YEAR, 1) // default daily for custom
            }

            val nextStart = calStart.timeInMillis

            // Stop if DATE end type is reached
            if (masterEvent.repeatEndType == "DATE" && calUntil != null && nextStart > calUntil.timeInMillis) {
                break
            }

            val nextEnd = nextStart + durationMs

            occurrences.add(
                masterEvent.copy(
                    id = 0, // Auto-generate new primary key
                    startDate = nextStart,
                    endDate = nextEnd,
                    parentEventId = masterEvent.id
                )
            )
        }

        return occurrences
    }
}
