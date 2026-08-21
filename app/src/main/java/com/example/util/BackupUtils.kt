package com.example.util

import com.example.data.model.*
import com.example.data.repository.CalendarRepository
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object BackupUtils {

    suspend fun createFullBackupJson(
        events: List<Event>,
        tasks: List<Task>,
        notes: List<Note>,
        birthdays: List<Birthday>,
        anniversaries: List<Anniversary>,
        holidays: List<Holiday>,
        categories: List<Category>,
        countdowns: List<Countdown>,
        settings: Map<String, String>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "Personal Calendar & Planner")
        root.put("timestamp", System.currentTimeMillis())

        // Events
        val eventsArray = JSONArray()
        events.forEach { e ->
            val obj = JSONObject().apply {
                put("id", e.id)
                put("title", e.title)
                put("description", e.description)
                put("startDate", e.startDate)
                put("endDate", e.endDate)
                put("isAllDay", e.isAllDay)
                put("location", e.location)
                put("colorHex", e.colorHex)
                put("categoryId", e.categoryId)
                put("repeatType", e.repeatType)
                put("reminderMinutes", e.reminderMinutes)
                put("isArchived", e.isArchived)
            }
            eventsArray.put(obj)
        }
        root.put("events", eventsArray)

        // Tasks
        val tasksArray = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("description", t.description)
                put("dueDate", t.dueDate ?: JSONObject.NULL)
                put("dueTime", t.dueTime ?: JSONObject.NULL)
                put("priority", t.priority)
                put("isCompleted", t.isCompleted)
                put("categoryId", t.categoryId)
                put("status", t.status)
                put("isArchived", t.isArchived)
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Notes
        val notesArray = JSONArray()
        notes.forEach { n ->
            val obj = JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("content", n.content)
                put("colorHex", n.colorHex)
                put("isPinned", n.isPinned)
                put("categoryId", n.categoryId)
                put("associatedDate", n.associatedDate ?: JSONObject.NULL)
                put("isChecklist", n.isChecklist)
                put("checklistJson", n.checklistJson)
                put("isLocked", n.isLocked)
                put("pinCode", n.pinCode)
                put("tags", n.tags)
                put("reminderTime", n.reminderTime ?: JSONObject.NULL)
                put("drawingData", n.drawingData ?: JSONObject.NULL)
                put("isDeleted", n.isDeleted)
                put("deletedAt", n.deletedAt ?: JSONObject.NULL)
                put("updatedAt", n.updatedAt)
            }
            notesArray.put(obj)
        }
        root.put("notes", notesArray)

        // Birthdays
        val birthdaysArray = JSONArray()
        birthdays.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("personName", b.personName)
                put("birthDate", b.birthDate)
                put("birthYear", b.birthYear ?: JSONObject.NULL)
                put("notes", b.notes)
            }
            birthdaysArray.put(obj)
        }
        root.put("birthdays", birthdaysArray)

        // Anniversaries
        val anniversariesArray = JSONArray()
        anniversaries.forEach { a ->
            val obj = JSONObject().apply {
                put("id", a.id)
                put("title", a.title)
                put("date", a.date)
                put("year", a.year ?: JSONObject.NULL)
                put("notes", a.notes)
            }
            anniversariesArray.put(obj)
        }
        root.put("anniversaries", anniversariesArray)

        // Holidays
        val holidaysArray = JSONArray()
        holidays.forEach { h ->
            val obj = JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("date", h.date)
                put("type", h.type)
                put("calendarType", h.calendarType)
            }
            holidaysArray.put(obj)
        }
        root.put("holidays", holidaysArray)

        // Categories
        val categoriesArray = JSONArray()
        categories.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("colorHex", c.colorHex)
                put("iconName", c.iconName)
            }
            categoriesArray.put(obj)
        }
        root.put("categories", categoriesArray)

        // Countdowns
        val countdownsArray = JSONArray()
        countdowns.forEach { cd ->
            val obj = JSONObject().apply {
                put("id", cd.id)
                put("title", cd.title)
                put("targetDate", cd.targetDate)
                put("category", cd.category)
                put("colorHex", cd.colorHex)
                put("notes", cd.notes)
            }
            countdownsArray.put(obj)
        }
        root.put("countdowns", countdownsArray)

        // Settings
        val settingsObj = JSONObject()
        settings.forEach { (k, v) ->
            settingsObj.put(k, v)
        }
        root.put("settings", settingsObj)

        return root.toString(2)
    }

    suspend fun restoreFullBackupJson(
        jsonString: String,
        repository: CalendarRepository
    ): Boolean {
        return try {
            val root = JSONObject(jsonString)

            if (root.has("events")) {
                val arr = root.getJSONArray("events")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val event = Event(
                        id = obj.optLong("id", 0L),
                        title = obj.optString("title", "Restored Event"),
                        description = obj.optString("description", ""),
                        startDate = obj.optLong("startDate", System.currentTimeMillis()),
                        endDate = obj.optLong("endDate", System.currentTimeMillis() + 3600000),
                        isAllDay = obj.optBoolean("isAllDay", false),
                        location = obj.optString("location", ""),
                        colorHex = obj.optString("colorHex", "#3F51B5"),
                        categoryId = obj.optLong("categoryId", 1L),
                        repeatType = obj.optString("repeatType", "NONE"),
                        reminderMinutes = obj.optInt("reminderMinutes", 15),
                        isArchived = obj.optBoolean("isArchived", false)
                    )
                    repository.insertEvent(event)
                }
            }

            if (root.has("tasks")) {
                val arr = root.getJSONArray("tasks")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val task = Task(
                        id = obj.optLong("id", 0L),
                        title = obj.optString("title", "Restored Task"),
                        description = obj.optString("description", ""),
                        dueDate = if (obj.isNull("dueDate")) null else obj.optLong("dueDate"),
                        dueTime = if (obj.isNull("dueTime")) null else obj.optString("dueTime"),
                        priority = obj.optString("priority", "Medium"),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        categoryId = obj.optLong("categoryId", 1L),
                        status = obj.optString("status", "Pending"),
                        isArchived = obj.optBoolean("isArchived", false)
                    )
                    repository.insertTask(task)
                }
            }

            if (root.has("notes")) {
                val arr = root.getJSONArray("notes")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val note = Note(
                        id = obj.optLong("id", 0L),
                        title = obj.optString("title", "Restored Note"),
                        content = obj.optString("content", ""),
                        colorHex = obj.optString("colorHex", "#2D3748"),
                        isPinned = obj.optBoolean("isPinned", false),
                        categoryId = obj.optLong("categoryId", 1L),
                        associatedDate = if (obj.isNull("associatedDate")) null else obj.optLong("associatedDate"),
                        isChecklist = obj.optBoolean("isChecklist", false),
                        checklistJson = obj.optString("checklistJson", ""),
                        isLocked = obj.optBoolean("isLocked", false),
                        pinCode = obj.optString("pinCode", ""),
                        tags = obj.optString("tags", ""),
                        reminderTime = if (obj.isNull("reminderTime")) null else obj.optLong("reminderTime"),
                        drawingData = if (obj.isNull("drawingData")) null else obj.optString("drawingData"),
                        isDeleted = obj.optBoolean("isDeleted", false),
                        deletedAt = if (obj.isNull("deletedAt")) null else obj.optLong("deletedAt"),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    repository.insertNote(note)
                }
            }

            if (root.has("birthdays")) {
                val arr = root.getJSONArray("birthdays")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val birthday = Birthday(
                        id = obj.optLong("id", 0L),
                        personName = obj.optString("personName", "Restored Birthday"),
                        birthDate = obj.optLong("birthDate", System.currentTimeMillis()),
                        birthYear = if (obj.isNull("birthYear")) null else obj.optInt("birthYear"),
                        notes = obj.optString("notes", "")
                    )
                    repository.insertBirthday(birthday)
                }
            }

            if (root.has("anniversaries")) {
                val arr = root.getJSONArray("anniversaries")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val anniversary = Anniversary(
                        id = obj.optLong("id", 0L),
                        title = obj.optString("title", "Restored Anniversary"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        year = if (obj.isNull("year")) null else obj.optInt("year"),
                        reminderMinutes = obj.optInt("reminderMinutes", 1440),
                        notes = obj.optString("notes", "")
                    )
                    repository.insertAnniversary(anniversary)
                }
            }

            if (root.has("holidays")) {
                val arr = root.getJSONArray("holidays")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val holiday = Holiday(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", "Restored Holiday"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        type = obj.optString("type", "Custom Holiday"),
                        calendarType = obj.optString("calendarType", "Gregorian"),
                        isCustom = obj.optBoolean("isCustom", true)
                    )
                    repository.insertHoliday(holiday)
                }
            }

            if (root.has("categories")) {
                val arr = root.getJSONArray("categories")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val category = Category(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", "General"),
                        colorHex = obj.optString("colorHex", "#4F46E5"),
                        iconName = obj.optString("iconName", "Star"),
                        isCustom = obj.optBoolean("isCustom", true)
                    )
                    repository.insertCategory(category)
                }
            }

            if (root.has("countdowns")) {
                val arr = root.getJSONArray("countdowns")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val countdown = Countdown(
                        id = obj.optLong("id", 0L),
                        title = obj.optString("title", "Restored Countdown"),
                        targetDate = obj.optLong("targetDate", System.currentTimeMillis()),
                        category = obj.optString("category", "Event"),
                        colorHex = obj.optString("colorHex", "#3F51B5"),
                        notes = obj.optString("notes", "")
                    )
                    repository.insertCountdown(countdown)
                }
            }

            if (root.has("settings")) {
                val settingsObj = root.getJSONObject("settings")
                val keys = settingsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    repository.setSetting(key, settingsObj.getString(key))
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportEventsToIcs(events: List<Event>): String {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sb = StringBuilder()
        sb.append("BEGIN:VCALENDAR\r\n")
        sb.append("VERSION:2.0\r\n")
        sb.append("PRODID:-//Personal Calendar App//EN\r\n")
        sb.append("CALSCALE:GREGORIAN\r\n")

        events.forEach { e ->
            sb.append("BEGIN:VEVENT\r\n")
            sb.append("UID:event-${e.id}@calendar.app\r\n")
            sb.append("SUMMARY:${escapeIcsText(e.title)}\r\n")
            if (e.description.isNotBlank()) {
                sb.append("DESCRIPTION:${escapeIcsText(e.description)}\r\n")
            }
            if (e.location.isNotBlank()) {
                sb.append("LOCATION:${escapeIcsText(e.location)}\r\n")
            }
            sb.append("DTSTART:${sdf.format(Date(e.startDate))}\r\n")
            sb.append("DTEND:${sdf.format(Date(e.endDate))}\r\n")
            sb.append("END:VEVENT\r\n")
        }

        sb.append("END:VCALENDAR\r\n")
        return sb.toString()
    }

    suspend fun importEventsFromIcs(icsString: String, repository: CalendarRepository): Int {
        var importedCount = 0
        try {
            val lines = icsString.lines()
            var inEvent = false
            var summary = ""
            var description = ""
            var location = ""
            var dtStart = System.currentTimeMillis()
            var dtEnd = System.currentTimeMillis() + 3600000

            val sdfUtc = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val sdfLocal = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
            val sdfDateOnly = SimpleDateFormat("yyyyMMdd", Locale.US)

            fun parseIcsDate(rawVal: String): Long? {
                val clean = rawVal.trim().replace("-", "").replace(":", "")
                return try {
                    if (rawVal.endsWith("Z", ignoreCase = true)) {
                        sdfUtc.parse(rawVal.trim())?.time
                    } else if (rawVal.contains("T")) {
                        sdfLocal.parse(rawVal.trim())?.time
                    } else {
                        sdfDateOnly.parse(rawVal.trim())?.time
                    }
                } catch (e: Exception) {
                    null
                }
            }

            lines.forEach { rawLine ->
                val line = rawLine.trim()
                when {
                    line.startsWith("BEGIN:VEVENT", ignoreCase = true) -> {
                        inEvent = true
                        summary = ""
                        description = ""
                        location = ""
                        dtStart = System.currentTimeMillis()
                        dtEnd = System.currentTimeMillis() + 3600000
                    }
                    line.startsWith("END:VEVENT", ignoreCase = true) -> {
                        if (inEvent && summary.isNotBlank()) {
                            repository.insertEvent(
                                Event(
                                    title = summary,
                                    description = description,
                                    location = location,
                                    startDate = dtStart,
                                    endDate = dtEnd
                                )
                            )
                            importedCount++
                        }
                        inEvent = false
                    }
                    inEvent -> {
                        when {
                            line.startsWith("SUMMARY:", ignoreCase = true) -> {
                                summary = unescapeIcsText(line.substringAfter(":"))
                            }
                            line.startsWith("DESCRIPTION:", ignoreCase = true) -> {
                                description = unescapeIcsText(line.substringAfter(":"))
                            }
                            line.startsWith("LOCATION:", ignoreCase = true) -> {
                                location = unescapeIcsText(line.substringAfter(":"))
                            }
                            line.startsWith("DTSTART", ignoreCase = true) -> {
                                val value = line.substringAfter(":")
                                parseIcsDate(value)?.let { dtStart = it }
                            }
                            line.startsWith("DTEND", ignoreCase = true) -> {
                                val value = line.substringAfter(":")
                                parseIcsDate(value)?.let { dtEnd = it }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return importedCount
    }

    private fun escapeIcsText(text: String): String {
        return text.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }

    private fun unescapeIcsText(text: String): String {
        return text.replace("\\n", "\n")
            .replace("\\,", ",")
            .replace("\\;", ";")
            .replace("\\\\", "\\")
    }
}
