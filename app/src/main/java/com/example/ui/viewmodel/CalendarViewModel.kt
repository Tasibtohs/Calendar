package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.CalendarDatabase
import com.example.data.model.*
import com.example.data.repository.CalendarRepository
import com.example.data.repository.WeatherRepository
import com.example.notifications.ReminderScheduler
import com.example.util.CalendarUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class CalendarViewType {
    MONTH, WEEK, DAY, YEAR, AGENDA
}

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    val repository: CalendarRepository
    val weatherRepository: WeatherRepository

    init {
        val database = CalendarDatabase.getInstance(application)
        repository = CalendarRepository(database)
        weatherRepository = WeatherRepository(application)
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
            loadSettings()
        }
    }

    // Weather State Flows & Actions
    val weatherInfo: StateFlow<WeatherInfo> = weatherRepository.weatherState
    val isWeatherLoading: StateFlow<Boolean> = weatherRepository.isLoading
    val weatherErrorMessage: StateFlow<String?> = weatherRepository.errorMessage

    fun refreshWeather() {
        weatherRepository.refreshWeather()
    }

    fun selectWeatherCity(city: CityLocation) {
        weatherRepository.selectCity(city)
    }

    fun fetchGpsWeather() {
        viewModelScope.launch {
            weatherRepository.fetchCurrentGpsLocationWeather()
        }
    }

    // Selected Navigation Tab (0: Home, 1: Calendar, 2: Tasks, 3: Notes, 4: Settings)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Active Selected Date
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    // Calendar View Mode (Month, Week, Day, Year, Agenda)
    private val _calendarViewType = MutableStateFlow(CalendarViewType.MONTH)
    val calendarViewType: StateFlow<CalendarViewType> = _calendarViewType.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Cover Photo Customization Settings
    private val _coverPhotoUri = MutableStateFlow<String?>(null)
    val coverPhotoUri: StateFlow<String?> = _coverPhotoUri.asStateFlow()

    private val _coverHeightDp = MutableStateFlow(210)
    val coverHeightDp: StateFlow<Int> = _coverHeightDp.asStateFlow()

    private val _coverBlurDp = MutableStateFlow(0)
    val coverBlurDp: StateFlow<Int> = _coverBlurDp.asStateFlow()

    private val _coverOverlayOpacity = MutableStateFlow(0.25f)
    val coverOverlayOpacity: StateFlow<Float> = _coverOverlayOpacity.asStateFlow()

    private val _coverCornerRadiusDp = MutableStateFlow(16)
    val coverCornerRadiusDp: StateFlow<Int> = _coverCornerRadiusDp.asStateFlow()

    private val _coverBadgeText = MutableStateFlow("Personal Dashboard")
    val coverBadgeText: StateFlow<String> = _coverBadgeText.asStateFlow()

    private val _coverBorderStyle = MutableStateFlow("None") // "None", "Solid", "Gradient"
    val coverBorderStyle: StateFlow<String> = _coverBorderStyle.asStateFlow()

    private val _coverBorderWidth = MutableStateFlow(2) // 1..4 dp
    val coverBorderWidth: StateFlow<Int> = _coverBorderWidth.asStateFlow()

    private val _coverFilterPreset = MutableStateFlow("Natural")
    val coverFilterPreset: StateFlow<String> = _coverFilterPreset.asStateFlow()

    private val _showTraditionalDatesInGrid = MutableStateFlow(true)
    val showTraditionalDatesInGrid: StateFlow<Boolean> = _showTraditionalDatesInGrid.asStateFlow()

    private val _themeMode = MutableStateFlow("System") // "System", "Light", "Dark"
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _themeAccent = MutableStateFlow("RoyalBlue")
    val themeAccent: StateFlow<String> = _themeAccent.asStateFlow()

    private val _isAmoled = MutableStateFlow(false)
    val isAmoled: StateFlow<Boolean> = _isAmoled.asStateFlow()

    private val _dynamicColor = MutableStateFlow(false)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _fontScaleOption = MutableStateFlow("Standard") // "Small", "Standard", "Large", "Extra Large"
    val fontScaleOption: StateFlow<String> = _fontScaleOption.asStateFlow()

    private val _defaultCalendarView = MutableStateFlow("Home") // "Home", "Month", "Week", "Day", "Year", "Agenda"
    val defaultCalendarView: StateFlow<String> = _defaultCalendarView.asStateFlow()

    private val _defaultEventDuration = MutableStateFlow(30) // Minutes
    val defaultEventDuration: StateFlow<Int> = _defaultEventDuration.asStateFlow()

    private val _defaultReminderOffset = MutableStateFlow(15) // Minutes
    val defaultReminderOffset: StateFlow<Int> = _defaultReminderOffset.asStateFlow()

    private val _weekendPreset = MutableStateFlow("Friday & Saturday") // "Friday & Saturday", "Saturday & Sunday", "Friday Only", "Sunday Only"
    val weekendPreset: StateFlow<String> = _weekendPreset.asStateFlow()

    private val _showBengaliSeason = MutableStateFlow(true)
    val showBengaliSeason: StateFlow<Boolean> = _showBengaliSeason.asStateFlow()

    private val _quietHoursEnabled = MutableStateFlow(false)
    val quietHoursEnabled: StateFlow<Boolean> = _quietHoursEnabled.asStateFlow()

    private val _quietHoursStart = MutableStateFlow("22:00")
    val quietHoursStart: StateFlow<String> = _quietHoursStart.asStateFlow()

    private val _quietHoursEnd = MutableStateFlow("07:00")
    val quietHoursEnd: StateFlow<String> = _quietHoursEnd.asStateFlow()

    private val _dailyBriefingTime = MutableStateFlow("07:00")
    val dailyBriefingTime: StateFlow<String> = _dailyBriefingTime.asStateFlow()

    private val _ringtoneSound = MutableStateFlow("Gentle Chime")
    val ringtoneSound: StateFlow<String> = _ringtoneSound.asStateFlow()

    private val _hapticIntensity = MutableStateFlow("Medium") // "Off", "Soft", "Medium", "Strong"
    val hapticIntensity: StateFlow<String> = _hapticIntensity.asStateFlow()

    private val _temperatureUnit = MutableStateFlow("Celsius (°C)")
    val temperatureUnit: StateFlow<String> = _temperatureUnit.asStateFlow()

    private val _windSpeedUnit = MutableStateFlow("km/h")
    val windSpeedUnit: StateFlow<String> = _windSpeedUnit.asStateFlow()

    private val _weatherRefreshInterval = MutableStateFlow("3 Hours")
    val weatherRefreshInterval: StateFlow<String> = _weatherRefreshInterval.asStateFlow()

    private val _watermarkEnabled = MutableStateFlow(true)
    val watermarkEnabled: StateFlow<Boolean> = _watermarkEnabled.asStateFlow()

    private val _defaultExportFormat = MutableStateFlow("JSON")
    val defaultExportFormat: StateFlow<String> = _defaultExportFormat.asStateFlow()

    private val _autoBackupReminder = MutableStateFlow("Weekly")
    val autoBackupReminder: StateFlow<String> = _autoBackupReminder.asStateFlow()

    private val _hijriDayAdjustment = MutableStateFlow(0)
    val hijriDayAdjustment: StateFlow<Int> = _hijriDayAdjustment.asStateFlow()

    // Data streams
    val allEvents: StateFlow<List<Event>> = repository.allEvents.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allTasks: StateFlow<List<Task>> = repository.allTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allNotes: StateFlow<List<Note>> = repository.allNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val deletedNotes: StateFlow<List<Note>> = repository.deletedNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allBirthdays: StateFlow<List<Birthday>> = repository.allBirthdays.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allAnniversaries: StateFlow<List<Anniversary>> = repository.allAnniversaries.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allHolidays: StateFlow<List<Holiday>> = repository.allHolidays.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allCategories: StateFlow<List<Category>> = repository.allCategories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allCountdowns: StateFlow<List<Countdown>> = repository.allCountdowns.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val archivedEvents: StateFlow<List<Event>> = repository.archivedEvents.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val archivedTasks: StateFlow<List<Task>> = repository.archivedTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _dailyBriefingEnabled = MutableStateFlow(true)
    val dailyBriefingEnabled: StateFlow<Boolean> = _dailyBriefingEnabled.asStateFlow()

    private val _language = MutableStateFlow("Bangla / English")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _dateFormat = MutableStateFlow("dd/MM/yyyy")
    val dateFormat: StateFlow<String> = _dateFormat.asStateFlow()

    private val _timeFormat = MutableStateFlow("12 Hour")
    val timeFormat: StateFlow<String> = _timeFormat.asStateFlow()

    private val _firstDayOfWeek = MutableStateFlow("Sunday")
    val firstDayOfWeek: StateFlow<String> = _firstDayOfWeek.asStateFlow()

    private val _showWeekNumbers = MutableStateFlow(false)
    val showWeekNumbers: StateFlow<Boolean> = _showWeekNumbers.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    // App Branding & Tagline
    private val _customAppName = MutableStateFlow("My Calendar")
    val customAppName: StateFlow<String> = _customAppName.asStateFlow()

    private val _isTaglineEnabled = MutableStateFlow(true)
    val isTaglineEnabled: StateFlow<Boolean> = _isTaglineEnabled.asStateFlow()

    private val _taglineMode = MutableStateFlow("Default") // "Default", "Custom", "DailyQuote"
    val taglineMode: StateFlow<String> = _taglineMode.asStateFlow()

    private val _customTaglineText = MutableStateFlow("আপনার সময়, আপনার পরিকল্পনা")
    val customTaglineText: StateFlow<String> = _customTaglineText.asStateFlow()

    val dailyQuotes = listOf(
        "আপনার সময়, আপনার পরিকল্পনা",
        "প্রতিটি নতুন দিন নতুন সম্ভাবনার সূচনা",
        "পরিকল্পিত জীবনই সফলতার মূল চাবিকাঠি",
        "সময়ের সদ্ব্যবহারই আপনার শ্রেষ্ঠ বিনিয়োগ",
        "আজকের ছোট পদক্ষেপ আগামীর বড় সাফল্য",
        "প্রতিটি মুহূর্তকে অর্থপূর্ণ ও লক্ষ্যমুখী করুন",
        "শৃঙ্খলা ও ধারাবাহিকতা লক্ষ্যে পৌঁছায়",
        "আজকের কাজ আজই সম্পন্ন করার সংকল্প রাখুন",
        "সঠিক সময়ে সঠিক সিদ্ধান্ত জীবন বদলে দেয়",
        "আপনার স্বপ্নের পথে আজ আরও এক ধাপ এগিয়ে যান",
        "মনোযোগ ও একাগ্রতা সাফল্যের আসল শক্তি",
        "নিজেকে প্রতিদিন একটু একটু করে উন্নত করুন",
        "সময় কাউকে অপেক্ষা করায় না, সময়ের মূল্য দিন",
        "সততা ও নিষ্ঠায় গড়ে ওঠে সুন্দর ভবিষ্যৎ",
        "সুপরিকল্পিত রুটিন নিয়ে আসে মানসিক শান্তি"
    )

    val activeTagline: StateFlow<String> = combine(
        _isTaglineEnabled, _taglineMode, _customTaglineText, _selectedDate
    ) { enabled, mode, custom, date ->
        if (!enabled) ""
        else when (mode) {
            "Custom" -> custom.ifBlank { "আপনার সময়, আপনার পরিকল্পনা" }
            "DailyQuote" -> {
                val dayOfYear = date.get(Calendar.DAY_OF_YEAR)
                dailyQuotes[dayOfYear % dailyQuotes.size]
            }
            else -> "আপনার সময়, আপনার পরিকল্পনা"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "আপনার সময়, আপনার পরিকল্পনা")

    // Notifications data stream
    val allNotifications: StateFlow<List<AppNotification>> = repository.allNotifications.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val unreadNotificationCount: StateFlow<Int> = repository.unreadNotificationCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    // Undo Cache for Recently Deleted Item
    sealed class DeletedEntity {
        data class DeletedEvent(val event: Event) : DeletedEntity()
        data class DeletedTask(val task: Task) : DeletedEntity()
        data class DeletedNote(val note: Note) : DeletedEntity()
        data class DeletedBirthday(val birthday: Birthday) : DeletedEntity()
        data class DeletedAnniversary(val anniversary: Anniversary) : DeletedEntity()
        data class DeletedCountdown(val countdown: Countdown) : DeletedEntity()
    }

    private val _lastDeletedEntity = MutableStateFlow<DeletedEntity?>(null)
    val lastDeletedEntity: StateFlow<DeletedEntity?> = _lastDeletedEntity.asStateFlow()

    // Events for selected date
    val todaysEvents: StateFlow<List<Event>> = combine(allEvents, _selectedDate) { events, selCal ->
        val startOfDay = (selCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = (selCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        events.filter { it.startDate in startOfDay..endOfDay }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Upcoming events
    val upcomingEvents: StateFlow<List<Event>> = combine(allEvents, _selectedDate) { events, selCal ->
        val startOfToday = (selCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        events.filter { it.startDate >= startOfToday }
            .sortedBy { it.startDate }
            .take(10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun setSelectedDate(calendar: Calendar) {
        _selectedDate.value = calendar.clone() as Calendar
    }

    fun setToday() {
        _selectedDate.value = Calendar.getInstance()
    }

    fun navigateMonth(delta: Int) {
        val newCal = _selectedDate.value.clone() as Calendar
        newCal.add(Calendar.MONTH, delta)
        _selectedDate.value = newCal
    }

    fun setCalendarViewType(type: CalendarViewType) {
        _calendarViewType.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Settings actions
    private suspend fun loadSettings() {
        _coverPhotoUri.value = repository.getSetting("COVER_PHOTO_URI")
        _coverHeightDp.value = repository.getSetting("COVER_HEIGHT_DP")?.toIntOrNull() ?: 210
        _coverBlurDp.value = repository.getSetting("COVER_BLUR_DP")?.toIntOrNull() ?: 0
        _coverOverlayOpacity.value = repository.getSetting("COVER_OVERLAY_OPACITY")?.toFloatOrNull() ?: 0.25f
        _coverCornerRadiusDp.value = repository.getSetting("COVER_CORNER_RADIUS_DP")?.toIntOrNull() ?: 16
        _coverBadgeText.value = repository.getSetting("COVER_BADGE_TEXT") ?: "Personal Dashboard"
        _coverBorderStyle.value = repository.getSetting("COVER_BORDER_STYLE") ?: "None"
        _coverBorderWidth.value = repository.getSetting("COVER_BORDER_WIDTH")?.toIntOrNull() ?: 2
        _coverFilterPreset.value = repository.getSetting("COVER_FILTER_PRESET") ?: "Natural"
        _showTraditionalDatesInGrid.value = repository.getSetting("SHOW_TRADITIONAL_IN_GRID")?.toBooleanStrictOrNull() ?: true
        _themeMode.value = repository.getSetting("THEME_MODE") ?: "System"
        _themeAccent.value = repository.getSetting("THEME_ACCENT") ?: "RoyalBlue"
        _isAmoled.value = repository.getSetting("IS_AMOLED")?.toBooleanStrictOrNull() ?: false
        _dynamicColor.value = repository.getSetting("DYNAMIC_COLOR")?.toBooleanStrictOrNull() ?: false
        _fontScaleOption.value = repository.getSetting("FONT_SCALE_OPTION") ?: "Standard"
        _defaultCalendarView.value = repository.getSetting("DEFAULT_CALENDAR_VIEW") ?: "Home"
        _defaultEventDuration.value = repository.getSetting("DEFAULT_EVENT_DURATION")?.toIntOrNull() ?: 30
        _defaultReminderOffset.value = repository.getSetting("DEFAULT_REMINDER_OFFSET")?.toIntOrNull() ?: 15
        _weekendPreset.value = repository.getSetting("WEEKEND_PRESET") ?: "Friday & Saturday"
        _showBengaliSeason.value = repository.getSetting("SHOW_BENGALI_SEASON")?.toBooleanStrictOrNull() ?: true
        _quietHoursEnabled.value = repository.getSetting("QUIET_HOURS_ENABLED")?.toBooleanStrictOrNull() ?: false
        _quietHoursStart.value = repository.getSetting("QUIET_HOURS_START") ?: "22:00"
        _quietHoursEnd.value = repository.getSetting("QUIET_HOURS_END") ?: "07:00"
        _dailyBriefingTime.value = repository.getSetting("DAILY_BRIEFING_TIME") ?: "07:00"
        _ringtoneSound.value = repository.getSetting("RINGTONE_SOUND") ?: "Gentle Chime"
        _hapticIntensity.value = repository.getSetting("HAPTIC_INTENSITY") ?: "Medium"
        _temperatureUnit.value = repository.getSetting("TEMPERATURE_UNIT") ?: "Celsius (°C)"
        _windSpeedUnit.value = repository.getSetting("WIND_SPEED_UNIT") ?: "km/h"
        _weatherRefreshInterval.value = repository.getSetting("WEATHER_REFRESH_INTERVAL") ?: "3 Hours"
        _watermarkEnabled.value = repository.getSetting("WATERMARK_ENABLED")?.toBooleanStrictOrNull() ?: true
        _defaultExportFormat.value = repository.getSetting("DEFAULT_EXPORT_FORMAT") ?: "JSON"
        _autoBackupReminder.value = repository.getSetting("AUTO_BACKUP_REMINDER") ?: "Weekly"
        _hijriDayAdjustment.value = repository.getSetting("HIJRI_ADJUSTMENT")?.toIntOrNull() ?: 0
        _dailyBriefingEnabled.value = repository.getSetting("DAILY_BRIEFING_ENABLED")?.toBooleanStrictOrNull() ?: true
        _language.value = repository.getSetting("LANGUAGE") ?: "Bangla / English"
        _dateFormat.value = repository.getSetting("DATE_FORMAT") ?: "dd/MM/yyyy"
        _timeFormat.value = repository.getSetting("TIME_FORMAT") ?: "12 Hour"
        _firstDayOfWeek.value = repository.getSetting("FIRST_DAY_OF_WEEK") ?: "Sunday"
        _showWeekNumbers.value = repository.getSetting("SHOW_WEEK_NUMBERS")?.toBooleanStrictOrNull() ?: false
        _soundEnabled.value = repository.getSetting("SOUND_ENABLED")?.toBooleanStrictOrNull() ?: true
        _vibrationEnabled.value = repository.getSetting("VIBRATION_ENABLED")?.toBooleanStrictOrNull() ?: true
        _customAppName.value = repository.getSetting("CUSTOM_APP_NAME") ?: "My Calendar"
        _isTaglineEnabled.value = repository.getSetting("IS_TAGLINE_ENABLED")?.toBooleanStrictOrNull() ?: true
        _taglineMode.value = repository.getSetting("TAGLINE_MODE") ?: "Default"
        _customTaglineText.value = repository.getSetting("CUSTOM_TAGLINE_TEXT") ?: "আপনার সময়, আপনার পরিকল্পনা"
    }

    fun setCustomAppName(name: String) {
        val trimmed = name.trim().ifBlank { "My Calendar" }
        _customAppName.value = trimmed
        viewModelScope.launch { repository.setSetting("CUSTOM_APP_NAME", trimmed) }
    }

    fun setIsTaglineEnabled(enabled: Boolean) {
        _isTaglineEnabled.value = enabled
        viewModelScope.launch { repository.setSetting("IS_TAGLINE_ENABLED", enabled.toString()) }
    }

    fun setTaglineMode(mode: String) {
        _taglineMode.value = mode
        viewModelScope.launch { repository.setSetting("TAGLINE_MODE", mode) }
    }

    fun setCustomTaglineText(text: String) {
        _customTaglineText.value = text
        viewModelScope.launch { repository.setSetting("CUSTOM_TAGLINE_TEXT", text) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsAsRead() }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch { repository.markNotificationAsRead(id) }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch { repository.deleteNotification(id) }
    }

    fun clearAllNotifications() {
        viewModelScope.launch { repository.clearAllNotifications() }
    }

    fun addNotification(title: String, message: String, type: String = "SYSTEM", targetId: Long? = null) {
        viewModelScope.launch {
            repository.insertNotification(
                AppNotification(
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    type = type,
                    targetId = targetId,
                    isRead = false
                )
            )
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            loadSettings()
            // Check if any overdue tasks need a notification
            val pendingOverdue = allTasks.value.filter { !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis() }
            if (pendingOverdue.isNotEmpty()) {
                val hasOverdueNotification = allNotifications.value.any { it.type == "TASK" && !it.isRead }
                if (!hasOverdueNotification) {
                    repository.insertNotification(
                        AppNotification(
                            title = "বকেয়া কাজের অ্যালার্ট (Pending Tasks)",
                            message = "${pendingOverdue.size} টি টাস্কের নির্ধারিত সময় অতিক্রম হয়েছে। সেগুলো সম্পন্ন করুন।",
                            timestamp = System.currentTimeMillis(),
                            type = "TASK",
                            isRead = false
                        )
                    )
                }
            }
        }
    }


    fun setLanguage(lang: String) {
        _language.value = lang
        viewModelScope.launch { repository.setSetting("LANGUAGE", lang) }
    }

    fun setDateFormat(fmt: String) {
        _dateFormat.value = fmt
        viewModelScope.launch { repository.setSetting("DATE_FORMAT", fmt) }
    }

    fun setTimeFormat(fmt: String) {
        _timeFormat.value = fmt
        viewModelScope.launch { repository.setSetting("TIME_FORMAT", fmt) }
    }

    fun setFirstDayOfWeek(day: String) {
        _firstDayOfWeek.value = day
        viewModelScope.launch { repository.setSetting("FIRST_DAY_OF_WEEK", day) }
    }

    fun setShowWeekNumbers(show: Boolean) {
        _showWeekNumbers.value = show
        viewModelScope.launch { repository.setSetting("SHOW_WEEK_NUMBERS", show.toString()) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        viewModelScope.launch { repository.setSetting("SOUND_ENABLED", enabled.toString()) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        viewModelScope.launch { repository.setSetting("VIBRATION_ENABLED", enabled.toString()) }
    }

    fun setCoverPhotoUri(uri: String?) {
        _coverPhotoUri.value = uri
        viewModelScope.launch {
            if (uri != null) {
                repository.setSetting("COVER_PHOTO_URI", uri)
            } else {
                repository.deleteSetting("COVER_PHOTO_URI")
            }
        }
    }

    fun setCoverHeightDp(height: Int) {
        _coverHeightDp.value = height
        viewModelScope.launch { repository.setSetting("COVER_HEIGHT_DP", height.toString()) }
    }

    fun setCoverBlurDp(blur: Int) {
        _coverBlurDp.value = blur
        viewModelScope.launch { repository.setSetting("COVER_BLUR_DP", blur.toString()) }
    }

    fun setCoverOverlayOpacity(opacity: Float) {
        _coverOverlayOpacity.value = opacity
        viewModelScope.launch { repository.setSetting("COVER_OVERLAY_OPACITY", opacity.toString()) }
    }

    fun setCoverCornerRadiusDp(radius: Int) {
        _coverCornerRadiusDp.value = radius
        viewModelScope.launch { repository.setSetting("COVER_CORNER_RADIUS_DP", radius.toString()) }
    }

    fun setCoverBadgeText(text: String) {
        _coverBadgeText.value = text
        viewModelScope.launch { repository.setSetting("COVER_BADGE_TEXT", text) }
    }

    fun setCoverBorderStyle(style: String) {
        _coverBorderStyle.value = style
        viewModelScope.launch { repository.setSetting("COVER_BORDER_STYLE", style) }
    }

    fun setCoverBorderWidth(width: Int) {
        _coverBorderWidth.value = width
        viewModelScope.launch { repository.setSetting("COVER_BORDER_WIDTH", width.toString()) }
    }

    fun setCoverFilterPreset(preset: String) {
        _coverFilterPreset.value = preset
        viewModelScope.launch { repository.setSetting("COVER_FILTER_PRESET", preset) }
    }

    fun setShowTraditionalDatesInGrid(show: Boolean) {
        _showTraditionalDatesInGrid.value = show
        viewModelScope.launch { repository.setSetting("SHOW_TRADITIONAL_IN_GRID", show.toString()) }
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        viewModelScope.launch { repository.setSetting("THEME_MODE", mode) }
    }

    fun setThemeAccent(accentId: String) {
        _themeAccent.value = accentId
        viewModelScope.launch { repository.setSetting("THEME_ACCENT", accentId) }
    }

    fun setIsAmoled(amoled: Boolean) {
        _isAmoled.value = amoled
        viewModelScope.launch { repository.setSetting("IS_AMOLED", amoled.toString()) }
    }

    fun setDynamicColor(dynamic: Boolean) {
        _dynamicColor.value = dynamic
        viewModelScope.launch { repository.setSetting("DYNAMIC_COLOR", dynamic.toString()) }
    }

    fun setFontScaleOption(scale: String) {
        _fontScaleOption.value = scale
        viewModelScope.launch { repository.setSetting("FONT_SCALE_OPTION", scale) }
    }

    fun setDefaultCalendarView(viewName: String) {
        _defaultCalendarView.value = viewName
        viewModelScope.launch { repository.setSetting("DEFAULT_CALENDAR_VIEW", viewName) }
    }

    fun setDefaultEventDuration(minutes: Int) {
        _defaultEventDuration.value = minutes
        viewModelScope.launch { repository.setSetting("DEFAULT_EVENT_DURATION", minutes.toString()) }
    }

    fun setDefaultReminderOffset(minutes: Int) {
        _defaultReminderOffset.value = minutes
        viewModelScope.launch { repository.setSetting("DEFAULT_REMINDER_OFFSET", minutes.toString()) }
    }

    fun setWeekendPreset(preset: String) {
        _weekendPreset.value = preset
        viewModelScope.launch { repository.setSetting("WEEKEND_PRESET", preset) }
    }

    fun setShowBengaliSeason(show: Boolean) {
        _showBengaliSeason.value = show
        viewModelScope.launch { repository.setSetting("SHOW_BENGALI_SEASON", show.toString()) }
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        _quietHoursEnabled.value = enabled
        viewModelScope.launch { repository.setSetting("QUIET_HOURS_ENABLED", enabled.toString()) }
    }

    fun setQuietHoursStart(time: String) {
        _quietHoursStart.value = time
        viewModelScope.launch { repository.setSetting("QUIET_HOURS_START", time) }
    }

    fun setQuietHoursEnd(time: String) {
        _quietHoursEnd.value = time
        viewModelScope.launch { repository.setSetting("QUIET_HOURS_END", time) }
    }

    fun setDailyBriefingTime(time: String) {
        _dailyBriefingTime.value = time
        viewModelScope.launch { repository.setSetting("DAILY_BRIEFING_TIME", time) }
    }

    fun setRingtoneSound(sound: String) {
        _ringtoneSound.value = sound
        viewModelScope.launch { repository.setSetting("RINGTONE_SOUND", sound) }
    }

    fun setHapticIntensity(intensity: String) {
        _hapticIntensity.value = intensity
        viewModelScope.launch { repository.setSetting("HAPTIC_INTENSITY", intensity) }
    }

    fun setTemperatureUnit(unit: String) {
        _temperatureUnit.value = unit
        viewModelScope.launch { repository.setSetting("TEMPERATURE_UNIT", unit) }
    }

    fun setWindSpeedUnit(unit: String) {
        _windSpeedUnit.value = unit
        viewModelScope.launch { repository.setSetting("WIND_SPEED_UNIT", unit) }
    }

    fun setWeatherRefreshInterval(interval: String) {
        _weatherRefreshInterval.value = interval
        viewModelScope.launch { repository.setSetting("WEATHER_REFRESH_INTERVAL", interval) }
    }

    fun setWatermarkEnabled(enabled: Boolean) {
        _watermarkEnabled.value = enabled
        viewModelScope.launch { repository.setSetting("WATERMARK_ENABLED", enabled.toString()) }
    }

    fun setDefaultExportFormat(format: String) {
        _defaultExportFormat.value = format
        viewModelScope.launch { repository.setSetting("DEFAULT_EXPORT_FORMAT", format) }
    }

    fun setAutoBackupReminder(period: String) {
        _autoBackupReminder.value = period
        viewModelScope.launch { repository.setSetting("AUTO_BACKUP_REMINDER", period) }
    }

    fun resetAllSettingsToDefault() {
        viewModelScope.launch {
            setThemeMode("System")
            setThemeAccent("RoyalBlue")
            setIsAmoled(false)
            setDynamicColor(false)
            setFontScaleOption("Standard")
            setDefaultCalendarView("Home")
            setDefaultEventDuration(30)
            setDefaultReminderOffset(15)
            setWeekendPreset("Friday & Saturday")
            setShowBengaliSeason(true)
            setQuietHoursEnabled(false)
            setDailyBriefingTime("07:00")
            setDailyBriefingEnabled(true)
            setRingtoneSound("Gentle Chime")
            setHapticIntensity("Medium")
            setSoundEnabled(true)
            setVibrationEnabled(true)
            setTemperatureUnit("Celsius (°C)")
            setWindSpeedUnit("km/h")
            setWeatherRefreshInterval("3 Hours")
            setWatermarkEnabled(true)
            setDefaultExportFormat("JSON")
            setAutoBackupReminder("Weekly")
            setLanguage("Bangla / English")
            setDateFormat("dd/MM/yyyy")
            setTimeFormat("12 Hour")
            setFirstDayOfWeek("Sunday")
            setShowWeekNumbers(false)
            setHijriDayAdjustment(0)
            setShowTraditionalDatesInGrid(true)
            resetCoverToDefault()
        }
    }

    fun setHijriDayAdjustment(adj: Int) {
        _hijriDayAdjustment.value = adj
        viewModelScope.launch { repository.setSetting("HIJRI_ADJUSTMENT", adj.toString()) }
    }

    fun setDailyBriefingEnabled(enabled: Boolean) {
        _dailyBriefingEnabled.value = enabled
        viewModelScope.launch { repository.setSetting("DAILY_BRIEFING_ENABLED", enabled.toString()) }
    }

    fun resetCoverToDefault() {
        setCoverPhotoUri(null)
        setCoverHeightDp(210)
        setCoverBlurDp(0)
        setCoverOverlayOpacity(0.25f)
        setCoverCornerRadiusDp(16)
    }

    // CRUD operations
    fun saveEvent(event: Event, context: android.content.Context? = null) {
        viewModelScope.launch {
            if (event.id == 0L) {
                val assignedId = repository.insertEvent(event)
                val insertedMaster = event.copy(id = assignedId)

                context?.let { ReminderScheduler.scheduleEventReminders(it, insertedMaster) }

                // Generate recurring occurrences if repeat type is set
                if (event.repeatType != "NONE") {
                    val occurrences = com.example.util.RecurringEventUtils.generateOccurrences(insertedMaster)
                    occurrences.forEach { occ ->
                        val occId = repository.insertEvent(occ)
                        context?.let { ReminderScheduler.scheduleEventReminders(it, occ.copy(id = occId)) }
                    }
                }
            } else {
                repository.updateEvent(event)
                context?.let { ReminderScheduler.scheduleEventReminders(it, event) }
            }
        }
    }

    fun addEvent(
        title: String,
        description: String,
        startDate: Long,
        endDate: Long,
        isAllDay: Boolean,
        location: String,
        categoryId: Long,
        colorHex: String,
        reminderMinutesList: String = "15",
        repeatType: String = "NONE",
        repeatEndType: String = "NEVER",
        repeatUntilDate: Long? = null,
        repeatCount: Int = 0,
        participants: String = "",
        attachmentUri: String? = null,
        linkUrl: String = "",
        notes: String = "",
        context: android.content.Context? = null
    ) {
        saveEvent(
            Event(
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                isAllDay = isAllDay,
                location = location,
                categoryId = categoryId,
                colorHex = colorHex,
                reminderMinutesList = reminderMinutesList,
                repeatType = repeatType,
                repeatEndType = repeatEndType,
                repeatUntilDate = repeatUntilDate,
                repeatCount = repeatCount,
                participants = participants,
                attachmentUri = attachmentUri,
                linkUrl = linkUrl,
                notes = notes
            ),
            context = context
        )
    }

    fun duplicateEvent(event: Event, context: android.content.Context? = null) {
        viewModelScope.launch {
            val duplicate = event.copy(
                id = 0,
                title = "${event.title} (Copy)",
                parentEventId = null
            )
            saveEvent(duplicate, context)
        }
    }

    fun moveEvent(event: Event, newStartDate: Long, context: android.content.Context? = null) {
        viewModelScope.launch {
            val duration = event.endDate - event.startDate
            val updated = event.copy(
                startDate = newStartDate,
                endDate = newStartDate + duration
            )
            saveEvent(updated, context)
        }
    }

    fun deleteEvent(event: Event, context: android.content.Context? = null) {
        _lastDeletedEntity.value = DeletedEntity.DeletedEvent(event)
        viewModelScope.launch {
            repository.deleteEvent(event)
            context?.let { ReminderScheduler.cancelEventReminders(it, event.id) }
        }
    }

    fun deleteEventWithMode(event: Event, mode: String, context: android.content.Context? = null) {
        viewModelScope.launch {
            val masterId = event.parentEventId ?: event.id
            when (mode) {
                "THIS" -> {
                    repository.deleteEvent(event)
                    context?.let { ReminderScheduler.cancelEventReminders(it, event.id) }
                }
                "FOLLOWING" -> {
                    repository.deleteEvent(event)
                    context?.let { ReminderScheduler.cancelEventReminders(it, event.id) }
                    repository.deleteSeriesFromDate(masterId, event.startDate)
                }
                "SERIES" -> {
                    repository.deleteEventById(masterId)
                    context?.let { ReminderScheduler.cancelEventReminders(it, masterId) }
                    repository.deleteSeriesByParentId(masterId)
                }
            }
        }
    }

    // CRUD operations for Tasks
    fun saveTask(task: Task) {
        viewModelScope.launch {
            if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
        }
    }

    fun addTask(
        title: String,
        description: String = "",
        dueDate: Long? = null,
        dueTime: String? = null,
        priority: String = "Medium",
        categoryId: Long = 1,
        reminderMinutes: Int? = null
    ) {
        saveTask(
            Task(
                title = title,
                description = description,
                dueDate = dueDate,
                dueTime = dueTime,
                priority = priority,
                categoryId = categoryId,
                reminderMinutes = reminderMinutes,
                status = "Pending"
            )
        )
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            val newStatus = if (newCompleted) "Completed" else "Pending"
            repository.updateTask(task.copy(isCompleted = newCompleted, status = newStatus))
        }
    }

    fun deleteTask(task: Task) {
        _lastDeletedEntity.value = DeletedEntity.DeletedTask(task)
        viewModelScope.launch { repository.deleteTask(task) }
    }

    // CRUD operations for Notes
    fun saveNote(note: Note) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun addNote(
        title: String,
        content: String,
        colorHex: String = "#2D3748",
        isPinned: Boolean = false,
        categoryId: Long = 1,
        associatedDate: Long? = null
    ) {
        saveNote(
            Note(
                title = title,
                content = content,
                colorHex = colorHex,
                isPinned = isPinned,
                categoryId = categoryId,
                associatedDate = associatedDate
            )
        )
    }

    fun toggleNotePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun moveToTrash(note: Note) {
        _lastDeletedEntity.value = DeletedEntity.DeletedNote(note)
        viewModelScope.launch {
            repository.updateNote(
                note.copy(
                    isDeleted = true,
                    deletedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(
                note.copy(
                    isDeleted = false,
                    deletedAt = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(note: Note) {
        // Move to trash by default
        moveToTrash(note)
    }

    fun deleteNotePermanently(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun emptyNoteTrash() {
        viewModelScope.launch { repository.emptyTrashNotes() }
    }

    // CRUD operations for Birthdays
    fun saveBirthday(birthday: Birthday) {
        viewModelScope.launch {
            if (birthday.id == 0L) {
                repository.insertBirthday(birthday)
            } else {
                repository.updateBirthday(birthday)
            }
        }
    }

    fun addBirthday(
        personName: String,
        birthDate: Long,
        birthYear: Int? = null,
        reminderMinutes: Int = 1440,
        notes: String = "",
        avatarUri: String? = null
    ) {
        saveBirthday(
            Birthday(
                personName = personName,
                birthDate = birthDate,
                birthYear = birthYear,
                reminderMinutes = reminderMinutes,
                notes = notes,
                avatarUri = avatarUri
            )
        )
    }

    fun deleteBirthday(birthday: Birthday) {
        _lastDeletedEntity.value = DeletedEntity.DeletedBirthday(birthday)
        viewModelScope.launch { repository.deleteBirthday(birthday) }
    }

    // CRUD operations for Anniversaries
    fun saveAnniversary(anniversary: Anniversary) {
        viewModelScope.launch {
            if (anniversary.id == 0L) {
                repository.insertAnniversary(anniversary)
            } else {
                repository.updateAnniversary(anniversary)
            }
        }
    }

    fun addAnniversary(
        title: String,
        date: Long,
        year: Int? = null,
        reminderMinutes: Int = 1440,
        notes: String = ""
    ) {
        saveAnniversary(
            Anniversary(
                title = title,
                date = date,
                year = year,
                reminderMinutes = reminderMinutes,
                notes = notes
            )
        )
    }

    fun deleteAnniversary(anniversary: Anniversary) {
        _lastDeletedEntity.value = DeletedEntity.DeletedAnniversary(anniversary)
        viewModelScope.launch { repository.deleteAnniversary(anniversary) }
    }

    // CRUD operations for Holidays & Categories
    fun saveHoliday(holiday: Holiday) {
        viewModelScope.launch {
            repository.insertHoliday(holiday)
        }
    }

    fun addHoliday(
        name: String,
        date: Long,
        type: String = "Custom Holiday",
        calendarType: String = "Gregorian"
    ) {
        saveHoliday(Holiday(name = name, date = date, type = type, calendarType = calendarType, isCustom = true))
    }

    fun deleteHoliday(holiday: Holiday) {
        viewModelScope.launch { repository.deleteHoliday(holiday) }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun addCategory(name: String, colorHex: String, iconName: String = "Star") {
        saveCategory(Category(name = name, colorHex = colorHex, iconName = iconName, isCustom = true))
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            // Reassign any events, tasks, notes using this category to default category (id = 1)
            val eventsToUpdate = allEvents.value.filter { it.categoryId == category.id }
            eventsToUpdate.forEach { event ->
                repository.updateEvent(event.copy(categoryId = 1L))
            }
            val tasksToUpdate = allTasks.value.filter { it.categoryId == category.id }
            tasksToUpdate.forEach { task ->
                repository.updateTask(task.copy(categoryId = 1L))
            }
            val notesToUpdate = allNotes.value.filter { it.categoryId == category.id }
            notesToUpdate.forEach { note ->
                repository.updateNote(note.copy(categoryId = 1L))
            }
            repository.deleteCategory(category)
        }
    }

    // Countdowns CRUD
    fun saveCountdown(countdown: Countdown) {
        viewModelScope.launch { repository.insertCountdown(countdown) }
    }

    fun deleteCountdown(countdown: Countdown) {
        _lastDeletedEntity.value = DeletedEntity.DeletedCountdown(countdown)
        viewModelScope.launch { repository.deleteCountdown(countdown) }
    }

    // Undo restore
    fun undoLastDelete() {
        val last = _lastDeletedEntity.value ?: return
        viewModelScope.launch {
            when (last) {
                is DeletedEntity.DeletedEvent -> repository.insertEvent(last.event)
                is DeletedEntity.DeletedTask -> repository.insertTask(last.task)
                is DeletedEntity.DeletedNote -> repository.insertNote(last.note)
                is DeletedEntity.DeletedBirthday -> repository.insertBirthday(last.birthday)
                is DeletedEntity.DeletedAnniversary -> repository.insertAnniversary(last.anniversary)
                is DeletedEntity.DeletedCountdown -> repository.insertCountdown(last.countdown)
            }
            _lastDeletedEntity.value = null
        }
    }

    fun clearLastDeleted() {
        _lastDeletedEntity.value = null
    }

    // Archiving
    fun archiveEvent(event: Event) {
        viewModelScope.launch { repository.archiveEvent(event) }
    }

    fun unarchiveEvent(event: Event) {
        viewModelScope.launch { repository.unarchiveEvent(event) }
    }

    fun archiveTask(task: Task) {
        viewModelScope.launch { repository.archiveTask(task) }
    }

    fun unarchiveTask(task: Task) {
        viewModelScope.launch { repository.unarchiveTask(task) }
    }

    // Smart Conflict Detection
    fun checkEventOverlap(startDate: Long, endDate: Long, excludeEventId: Long? = null): Event? {
        val currentEvents = allEvents.value.filter { !it.isArchived && (excludeEventId == null || it.id != excludeEventId) }
        return currentEvents.firstOrNull { existing ->
            startDate < existing.endDate && endDate > existing.startDate
        }
    }

    // Free Time Slots Calculation
    fun calculateFreeTimeSlots(selectedCal: Calendar): List<Pair<String, String>> {
        val startOfDay = (selectedCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val endOfDay = (selectedCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val dayEvents = allEvents.value.filter { event ->
            !event.isArchived && event.startDate < endOfDay && event.endDate > startOfDay
        }.sortedBy { it.startDate }

        val freeSlots = mutableListOf<Pair<String, String>>()
        var currentPointer = startOfDay

        for (event in dayEvents) {
            val evStart = maxOf(event.startDate, startOfDay)
            val evEnd = minOf(event.endDate, endOfDay)

            if (evStart > currentPointer + (15 * 60 * 1000)) {
                freeSlots.add(
                    CalendarUtils.formatTime(currentPointer) to CalendarUtils.formatTime(evStart)
                )
            }
            if (evEnd > currentPointer) {
                currentPointer = evEnd
            }
        }

        if (endOfDay > currentPointer + (15 * 60 * 1000)) {
            freeSlots.add(
                CalendarUtils.formatTime(currentPointer) to CalendarUtils.formatTime(endOfDay)
            )
        }

        return freeSlots
    }
}
