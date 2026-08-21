package com.example.data.repository

import com.example.data.db.CalendarDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class CalendarRepository(private val db: CalendarDatabase) {

    val allEvents: Flow<List<Event>> = db.eventDao().getAllEvents()
    val allTasks: Flow<List<Task>> = db.taskDao().getAllTasks()
    val allNotes: Flow<List<Note>> = db.noteDao().getAllNotes()
    val deletedNotes: Flow<List<Note>> = db.noteDao().getDeletedNotes()
    val allBirthdays: Flow<List<Birthday>> = db.birthdayDao().getAllBirthdays()
    val allAnniversaries: Flow<List<Anniversary>> = db.anniversaryDao().getAllAnniversaries()
    val allHolidays: Flow<List<Holiday>> = db.holidayDao().getAllHolidays()
    val allCategories: Flow<List<Category>> = db.categoryDao().getAllCategories()
    val allSettings: Flow<List<AppSettings>> = db.appSettingsDao().getAllSettingsFlow()
    val allCountdowns: Flow<List<Countdown>> = db.countdownDao().getAllCountdowns()
    val allNotifications: Flow<List<AppNotification>> = db.notificationDao().getAllNotifications()
    val unreadNotificationCount: Flow<Int> = db.notificationDao().getUnreadNotificationCount()
    val archivedEvents: Flow<List<Event>> = db.eventDao().getArchivedEvents()
    val archivedTasks: Flow<List<Task>> = db.taskDao().getArchivedTasks()


    fun getEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<Event>> =
        db.eventDao().getEventsForDay(startOfDay, endOfDay)

    fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<Task>> =
        db.taskDao().getTasksForDay(startOfDay, endOfDay)

    fun getUpcomingEvents(startTimestamp: Long, limit: Int = 10): Flow<List<Event>> =
        db.eventDao().getUpcomingEvents(startTimestamp, limit)

    fun searchEvents(query: String): Flow<List<Event>> = db.eventDao().searchEvents(query)
    fun searchTasks(query: String): Flow<List<Task>> = db.taskDao().searchTasks(query)
    fun searchNotes(query: String): Flow<List<Note>> = db.noteDao().searchNotes(query)

    suspend fun getEventById(id: Long): Event? = withContext(Dispatchers.IO) {
        db.eventDao().getEventById(id)
    }

    suspend fun insertEvent(event: Event): Long = withContext(Dispatchers.IO) {
        db.eventDao().insertEvent(event)
    }

    suspend fun updateEvent(event: Event) = withContext(Dispatchers.IO) {
        db.eventDao().updateEvent(event)
    }

    suspend fun deleteEvent(event: Event) = withContext(Dispatchers.IO) {
        db.eventDao().deleteEvent(event)
    }

    suspend fun deleteEventById(id: Long) = withContext(Dispatchers.IO) {
        db.eventDao().deleteEventById(id)
    }

    suspend fun deleteSeriesByParentId(parentId: Long) = withContext(Dispatchers.IO) {
        db.eventDao().deleteSeriesByParentId(parentId)
    }

    suspend fun deleteSeriesFromDate(parentId: Long, fromDate: Long) = withContext(Dispatchers.IO) {
        db.eventDao().deleteSeriesFromDate(parentId, fromDate)
    }

    suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        db.taskDao().insertTask(task)
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        db.taskDao().updateTask(task)
    }

    suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        db.taskDao().deleteTask(task)
    }

    suspend fun getNoteById(id: Long): Note? = withContext(Dispatchers.IO) {
        db.noteDao().getNoteById(id)
    }

    suspend fun insertNote(note: Note) = withContext(Dispatchers.IO) {
        db.noteDao().insertNote(note)
    }

    suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) {
        db.noteDao().updateNote(note)
    }

    suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        db.noteDao().deleteNote(note)
    }

    suspend fun deleteNoteById(id: Long) = withContext(Dispatchers.IO) {
        db.noteDao().deleteNoteById(id)
    }

    suspend fun emptyTrashNotes() = withContext(Dispatchers.IO) {
        db.noteDao().emptyTrash()
    }

    suspend fun insertBirthday(birthday: Birthday) = withContext(Dispatchers.IO) {
        db.birthdayDao().insertBirthday(birthday)
    }

    suspend fun updateBirthday(birthday: Birthday) = withContext(Dispatchers.IO) {
        db.birthdayDao().updateBirthday(birthday)
    }

    suspend fun deleteBirthday(birthday: Birthday) = withContext(Dispatchers.IO) {
        db.birthdayDao().deleteBirthday(birthday)
    }

    suspend fun insertAnniversary(anniversary: Anniversary) = withContext(Dispatchers.IO) {
        db.anniversaryDao().insertAnniversary(anniversary)
    }

    suspend fun updateAnniversary(anniversary: Anniversary) = withContext(Dispatchers.IO) {
        db.anniversaryDao().updateAnniversary(anniversary)
    }

    suspend fun deleteAnniversary(anniversary: Anniversary) = withContext(Dispatchers.IO) {
        db.anniversaryDao().deleteAnniversary(anniversary)
    }

    suspend fun insertHoliday(holiday: Holiday) = withContext(Dispatchers.IO) {
        db.holidayDao().insertHoliday(holiday)
    }

    suspend fun getSetting(key: String): String? = withContext(Dispatchers.IO) {
        db.appSettingsDao().getSetting(key)
    }

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        db.appSettingsDao().setSetting(AppSettings(key, value))
    }

    suspend fun deleteSetting(key: String) = withContext(Dispatchers.IO) {
        db.appSettingsDao().deleteSetting(key)
    }

    suspend fun insertCategory(category: Category) = withContext(Dispatchers.IO) {
        db.categoryDao().insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        db.categoryDao().deleteCategory(category)
    }

    suspend fun deleteHoliday(holiday: Holiday) = withContext(Dispatchers.IO) {
        db.holidayDao().deleteHoliday(holiday)
    }

    suspend fun insertCountdown(countdown: Countdown): Long = withContext(Dispatchers.IO) {
        db.countdownDao().insertCountdown(countdown)
    }

    suspend fun updateCountdown(countdown: Countdown) = withContext(Dispatchers.IO) {
        db.countdownDao().updateCountdown(countdown)
    }

    suspend fun deleteCountdown(countdown: Countdown) = withContext(Dispatchers.IO) {
        db.countdownDao().deleteCountdown(countdown)
    }

    suspend fun archiveEvent(event: Event) = withContext(Dispatchers.IO) {
        db.eventDao().updateEvent(event.copy(isArchived = true))
    }

    suspend fun unarchiveEvent(event: Event) = withContext(Dispatchers.IO) {
        db.eventDao().updateEvent(event.copy(isArchived = false))
    }

    suspend fun archiveTask(task: Task) = withContext(Dispatchers.IO) {
        db.taskDao().updateTask(task.copy(isArchived = true))
    }

    suspend fun unarchiveTask(task: Task) = withContext(Dispatchers.IO) {
        db.taskDao().updateTask(task.copy(isArchived = false))
    }

    suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        if (db.categoryDao().getCategoryCount() == 0) {
            val seedCategories = listOf(
                Category(name = "Study", colorHex = "#3F51B5", iconName = "School"),
                Category(name = "Work", colorHex = "#E91E63", iconName = "Work"),
                Category(name = "Personal", colorHex = "#4CAF50", iconName = "Person"),
                Category(name = "Family", colorHex = "#FF9800", iconName = "Family"),
                Category(name = "Birthday", colorHex = "#9C27B0", iconName = "Cake"),
                Category(name = "Travel", colorHex = "#009688", iconName = "Flight"),
                Category(name = "Appointment", colorHex = "#00BCD4", iconName = "Event"),
                Category(name = "Religious", colorHex = "#795548", iconName = "Mosque"),
                Category(name = "Important", colorHex = "#F44336", iconName = "Star")
            )
            db.categoryDao().insertCategories(seedCategories)
        }

        val existingHolidays = db.holidayDao().getAllHolidaysList()
        val needsHolidayRefresh = existingHolidays.isEmpty() || existingHolidays.any { it.description.isBlank() || it.nameBn.isBlank() }

        if (needsHolidayRefresh) {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            fun createTimestamp(month: Int, day: Int): Long {
                val cal = Calendar.getInstance()
                cal.set(currentYear, month, day, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return cal.timeInMillis
            }

            val seedHolidays = listOf(
                Holiday(
                    name = "New Year's Day",
                    nameBn = "ইংরেজি নববর্ষ",
                    date = createTimestamp(0, 1),
                    type = "International Day",
                    calendarType = "Gregorian",
                    description = "খ্রিষ্টীয় গ্রেগরীয় নতুন বছরের প্রথম দিন ও বিশ্বব্যাপী উৎসব।"
                ),
                Holiday(
                    name = "Shaheed Day & International Mother Language Day",
                    nameBn = "শহীদ দিবস ও আন্তর্জাতিক মাতৃভাষা দিবস",
                    date = createTimestamp(1, 21),
                    type = "National Day",
                    calendarType = "Gregorian",
                    description = "১৯৫২ সালের মহান ভাষা আন্দোলনে মাতৃভাষা বাংলার অধিকার প্রতিষ্ঠার অমর শহীদদের স্মরণে জাতীয় ও আন্তর্জাতিক দিবস।"
                ),
                Holiday(
                    name = "Shab-e-Barat",
                    nameBn = "পবিত্র শবে বরাত",
                    date = createTimestamp(1, 25),
                    type = "Islamic Holiday",
                    calendarType = "Hijri",
                    description = "১৫ শাবান বরকত ও মাগফিরাতের রজনী, মুসলিম উম্মাহর অন্যতম পুণ্যময় ইবাদতের রাত।"
                ),
                Holiday(
                    name = "Sheikh Mujibur Rahman Birthday & National Children's Day",
                    nameBn = "বঙ্গবন্ধুর জন্মবার্ষিকী ও জাতীয় শিশু দিবস",
                    date = createTimestamp(2, 17),
                    type = "National Day",
                    calendarType = "Gregorian",
                    description = "স্বাধীন বাংলাদেশের স্থপতি বঙ্গবন্ধু শেখ মুজিবুর রহমানের জন্মবার্ষিকী ও ভবিষ্যৎ প্রজন্মের অধিকার সুরক্ষায় জাতীয় শিশু দিবস।"
                ),
                Holiday(
                    name = "Independence Day & National Day",
                    nameBn = "স্বাধীনতা ও জাতীয় দিবস",
                    date = createTimestamp(2, 26),
                    type = "National Day",
                    calendarType = "Gregorian",
                    description = "১৯৭১ সালের ২৬ মার্চ বাংলাদেশের মহান স্বাধীনতা ঘোষণা ও ত্রিশ লাখ বীর শহীদের আত্মত্যাগের স্মারক।"
                ),
                Holiday(
                    name = "Shab-e-Qadr (Laylat al-Qadr)",
                    nameBn = "পবিত্র শবে কদর",
                    date = createTimestamp(2, 27),
                    type = "Islamic Holiday",
                    calendarType = "Hijri",
                    description = "পবিত্র কুরআন নাজিলের মহিমান্বিত রজনী যা এক হাজার মাসের চেয়েও শ্রেষ্ঠ ও কল্যাণময়।"
                ),
                Holiday(
                    name = "Jumatul Wida",
                    nameBn = "জুমাতুল বিদা",
                    date = createTimestamp(2, 29),
                    type = "Islamic Holiday",
                    calendarType = "Hijri",
                    description = "পবিত্র মাহে রমজানের বিদায়ী জুমার দিন, শান্তি ও ক্ষমার বিশেষ মোনাজাত।"
                ),
                Holiday(
                    name = "Eid-ul-Fitr",
                    nameBn = "পবিত্র ঈদুল ফিতর",
                    date = createTimestamp(2, 31),
                    type = "Islamic Holiday",
                    calendarType = "Hijri",
                    description = "এক মাস সিয়াম সাধনার পর মুসলিম উম্মাহর প্রধান ধর্মীয় উৎসব ও আনন্দের দিন।"
                ),
                Holiday(
                    name = "Pohela Boishakh (Bengali New Year)",
                    nameBn = "পহেলা বৈশাখ (বাংলা নববর্ষ)",
                    date = createTimestamp(3, 14),
                    type = "Bangladesh Public Holiday",
                    calendarType = "Bangla",
                    description = "বাংলা বর্ষপঞ্জির প্রথম দিন (১ বৈশাখ), বাঙালি জাতির সার্বজনীন বর্ণাঢ্য সাংস্কৃতিক বর্ষবরণ উৎসব।"
                ),
                Holiday(
                    name = "May Day / International Workers' Day",
                    nameBn = "মে দিবস (আন্তর্জাতিক শ্রমিক দিবস)",
                    date = createTimestamp(4, 1),
                    type = "International Day",
                    calendarType = "Gregorian",
                    description = "শ্রমজীবী মানুষের আট ঘণ্টা কাজের অধিকার আদায়ের ঐতিহাসিক শিকাগো সংগ্রামের স্মারক ও বিশ্ব শ্রমিক দিবস।"
                ),
                Holiday(
                    name = "Buddha Purnima (Vesak)",
                    nameBn = "বুদ্ধ পূর্ণিমা (বৈশাখী পূর্ণিমা)",
                    date = createTimestamp(4, 12),
                    type = "Bangladesh Public Holiday",
                    calendarType = "Gregorian",
                    description = "বৌদ্ধ ধর্মের প্রবর্তক গৌতম বুদ্ধের শুভ জন্ম, বোধিলাভ (বুদ্ধত্ব) ও মহাপরিনির্বাণ তিথি।"
                ),
                Holiday(
                    name = "Eid-ul-Adha",
                    nameBn = "পবিত্র ঈদুল আযহা (কোরবানি ঈদ)",
                    date = createTimestamp(5, 7),
                    type = "Islamic Holiday",
                    calendarType = "Hijri",
                    description = "হযরত ইব্রাহিম (আ.) ও হযরত ইসমাইল (আ.)-এর ঐতিহাসিক মহান আত্মত্যাগের স্মরণে পশু কোরবানি ও ত্যাগের মহোৎসব।"
                ),
                Holiday(
                    name = "Holy Ashura",
                    nameBn = "পবিত্র আশুরা",
                    date = createTimestamp(6, 6),
                    type = "Islamic Holiday",
                    calendarType = "Hijri",
                    description = "১০ মহররম কারবালার প্রান্তরে হযরত ইমাম হোসাইন (রা.)-এর ঐতিহাসিক শাহাদত ও অন্যায়ের বিরুদ্ধে সত্যের বিজয়ের প্রতীক।"
                ),
                Holiday(
                    name = "National Mourning Day",
                    nameBn = "জাতীয় শোক দিবস",
                    date = createTimestamp(7, 15),
                    type = "National Day",
                    calendarType = "Gregorian",
                    description = "১৯৭৫ সালের ১৫ আগস্টে নির্মম হত্যাকাণ্ডের শিকার সকল শহীদদের স্মরণে বিনম্র শ্রদ্ধা ও জাতীয় শোক পালন।"
                ),
                Holiday(
                    name = "Janmashtami",
                    nameBn = "শুভ জন্মাষ্টমী",
                    date = createTimestamp(8, 4),
                    type = "Bangladesh Public Holiday",
                    calendarType = "Gregorian",
                    description = "সনাতন ধর্মাবলম্বীদের পরম পুরুষ ভগবান শ্রীকৃষ্ণের শুভ আবির্ভাব তিথি উদযাপন।"
                ),
                Holiday(
                    name = "Eid-e-Miladunnabi (PBUH)",
                    nameBn = "পবিত্র ঈদে মিলাদুন্নবী (সা.)",
                    date = createTimestamp(8, 16),
                    type = "Islamic Holiday",
                    calendarType = "Hijri",
                    description = "আখেরি নবী হযরত মুহাম্মদ (সা.)-এর শুভ বেলাদত (জন্ম) ও ওফাত দিবস স্মরণ।"
                ),
                Holiday(
                    name = "Durga Puja (Bijoya Dashami)",
                    nameBn = "শ্রী শ্রী দুর্গাপূজা (বিজয়া দশমী)",
                    date = createTimestamp(9, 2),
                    type = "Bangladesh Public Holiday",
                    calendarType = "Gregorian",
                    description = "সনাতন ধর্মাবলম্বীদের দেবী দুর্গার মর্ত্যলোক ত্যাগ ও অশুভ শক্তির বিনাশে শুভ বিজয়ার মহাশোভাযাত্রা।"
                ),
                Holiday(
                    name = "Victory Day",
                    nameBn = "মহান বিজয় দিবস",
                    date = createTimestamp(11, 16),
                    type = "National Day",
                    calendarType = "Gregorian",
                    description = "দীর্ঘ ৯ মাসের রক্তক্ষয়ী মুক্তিযুদ্ধের পর ১৯৭১ সালের ১৬ ডিসেম্বর ঐতিহাসিক চূড়ান্ত বিজয় অর্জন।"
                ),
                Holiday(
                    name = "Christmas Day",
                    nameBn = "যীশু খ্রীষ্টের জন্মদিন (শুভ বড়দিন)",
                    date = createTimestamp(11, 25),
                    type = "International Day",
                    calendarType = "Gregorian",
                    description = "খ্রিষ্টধর্মের প্রবর্তক যীশু খ্রীষ্টের শুভ জন্মোৎসব ও শান্তি-সৌহার্দ্যের মহামিলন।"
                )
            )
            db.holidayDao().deleteSystemHolidays()
            db.holidayDao().insertHolidays(seedHolidays)
        }

        if (getSetting("SEEDED_NOTIFICATIONS") == null) {
            val now = System.currentTimeMillis()
            val seedNotifications = listOf(
                AppNotification(
                    title = "স্বাগতম (Welcome to My Calendar)",
                    message = "আপনার সময় ও দৈনন্দিন পরিকল্পনার জন্য সম্পূর্ণ অফলাইন পার্সোনাল ক্যালেন্ডারে স্বাগতম।",
                    timestamp = now - (10 * 60 * 1000),
                    type = "SYSTEM",
                    isRead = false
                ),
                AppNotification(
                    title = "দৈনিক ব্রিফিং ও ছুটির অ্যালার্ট",
                    message = "আজকের দিনটি শুরু করুন আপনার সময়সূচি, টাস্ক ও আসন্ন সরকারি ছুটির তালিকা দেখে।",
                    timestamp = now - (60 * 60 * 1000),
                    type = "HOLIDAY",
                    isRead = false
                )
            )
            db.notificationDao().insertNotifications(seedNotifications)
            setSetting("SEEDED_NOTIFICATIONS", "true")
        }
    }

    suspend fun insertNotification(notification: AppNotification): Long = withContext(Dispatchers.IO) {
        db.notificationDao().insertNotification(notification)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        db.notificationDao().markAllAsRead()
    }

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        db.notificationDao().markAsRead(id)
    }

    suspend fun deleteNotification(id: Long) = withContext(Dispatchers.IO) {
        db.notificationDao().deleteNotification(id)
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        db.notificationDao().clearAllNotifications()
    }
}

