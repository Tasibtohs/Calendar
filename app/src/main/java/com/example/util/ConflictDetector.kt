package com.example.util

import com.example.data.model.Event

object ConflictDetector {

    /**
     * Finds events in [allEvents] that overlap in time with [startDate] to [endDate].
     * Excludes [excludeEventId] if provided (useful during editing).
     */
    fun findConflicts(
        allEvents: List<Event>,
        startDate: Long,
        endDate: Long,
        excludeEventId: Long? = null
    ): List<Event> {
        if (startDate >= endDate) return emptyList()

        return allEvents.filter { event ->
            if (excludeEventId != null && event.id == excludeEventId) return@filter false

            // Check interval overlap: NOT (event.endDate <= startDate || event.startDate >= endDate)
            val overlaps = !(event.endDate <= startDate || event.startDate >= endDate)
            overlaps
        }
    }
}
