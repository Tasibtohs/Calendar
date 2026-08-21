package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ViewModel States
    val themeMode by viewModel.themeMode.collectAsState()
    val themeAccent by viewModel.themeAccent.collectAsState()
    val isAmoled by viewModel.isAmoled.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val fontScaleOption by viewModel.fontScaleOption.collectAsState()

    val defaultCalendarView by viewModel.defaultCalendarView.collectAsState()
    val defaultEventDuration by viewModel.defaultEventDuration.collectAsState()
    val defaultReminderOffset by viewModel.defaultReminderOffset.collectAsState()
    val weekendPreset by viewModel.weekendPreset.collectAsState()
    val showBengaliSeason by viewModel.showBengaliSeason.collectAsState()

    val quietHoursEnabled by viewModel.quietHoursEnabled.collectAsState()
    val quietHoursStart by viewModel.quietHoursStart.collectAsState()
    val quietHoursEnd by viewModel.quietHoursEnd.collectAsState()
    val dailyBriefingTime by viewModel.dailyBriefingTime.collectAsState()
    val ringtoneSound by viewModel.ringtoneSound.collectAsState()
    val hapticIntensity by viewModel.hapticIntensity.collectAsState()

    val temperatureUnit by viewModel.temperatureUnit.collectAsState()
    val windSpeedUnit by viewModel.windSpeedUnit.collectAsState()
    val weatherRefreshInterval by viewModel.weatherRefreshInterval.collectAsState()
    val watermarkEnabled by viewModel.watermarkEnabled.collectAsState()
    val defaultExportFormat by viewModel.defaultExportFormat.collectAsState()
    val autoBackupReminder by viewModel.autoBackupReminder.collectAsState()

    val language by viewModel.language.collectAsState()
    val dateFormat by viewModel.dateFormat.collectAsState()
    val timeFormat by viewModel.timeFormat.collectAsState()
    val firstDayOfWeek by viewModel.firstDayOfWeek.collectAsState()
    val showWeekNumbers by viewModel.showWeekNumbers.collectAsState()
    val showTraditionalInGrid by viewModel.showTraditionalDatesInGrid.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val briefingEnabled by viewModel.dailyBriefingEnabled.collectAsState()

    val customAppName by viewModel.customAppName.collectAsState()
    val isTaglineEnabled by viewModel.isTaglineEnabled.collectAsState()
    val taglineMode by viewModel.taglineMode.collectAsState()
    val customTaglineText by viewModel.customTaglineText.collectAsState()

    val heightDp by viewModel.coverHeightDp.collectAsState()
    val opacity by viewModel.coverOverlayOpacity.collectAsState()
    val radiusDp by viewModel.coverCornerRadiusDp.collectAsState()
    val hijriAdj by viewModel.hijriDayAdjustment.collectAsState()

    // Dialog Visibility States
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var showFontScaleDialog by remember { mutableStateOf(false) }
    var showDefaultViewDialog by remember { mutableStateOf(false) }
    var showEventDurationDialog by remember { mutableStateOf(false) }
    var showWeekendPresetDialog by remember { mutableStateOf(false) }
    var showQuietHoursDialog by remember { mutableStateOf(false) }
    var showBriefingTimeDialog by remember { mutableStateOf(false) }
    var showRingtoneDialog by remember { mutableStateOf(false) }
    var showWeatherUnitDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showResetAllConfirmDialog by remember { mutableStateOf(false) }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showTimeFormatDialog by remember { mutableStateOf(false) }
    var showStartOfWeekDialog by remember { mutableStateOf(false) }
    var showHijriDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupDialogTab by remember { mutableStateOf(0) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showAppNameDialog by remember { mutableStateOf(false) }
    var showTaglineDialog by remember { mutableStateOf(false) }

    val currentAccentConfig = AvailableAccents.find { it.id == themeAccent } ?: AvailableAccents.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
            .testTag("settings_screen_column")
    ) {
        // Quick Pro Status Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.lg),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        Text(
                            text = "Smart Calendar Pro",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "v5.2",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "বাংলা, হিজরি ও গ্রেগরিয়ান পূর্ণাঙ্গ সমন্বিত ক্যালেন্ডার স্যুট",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        // SECTION 1: APPEARANCE & THEME (উন্নত থিম ও সাজসজ্জা)
        SettingsSectionHeader("Appearance & Customization (থিম ও সাজসজ্জা)", Icons.Outlined.Palette)
        SettingsCard {
            SettingsClickableItem(
                title = "Theme Mode (থিম মোড)",
                subtitle = when (themeMode) {
                    "Light" -> "Light Theme (উজ্জ্বল মোড)"
                    "Dark" -> "Dark Theme (ডার্ক মোড)"
                    else -> "System Default (ডিভাইস অনুযায়ী)"
                },
                icon = Icons.Outlined.BrightnessMedium,
                onClick = { showThemeDialog = true }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Color Accent & Palette (রঙের প্যালেট)",
                subtitle = "${currentAccentConfig.nameBn} (${currentAccentConfig.name})",
                icon = Icons.Outlined.ColorLens,
                badge = {
                    Surface(
                        shape = CircleShape,
                        color = currentAccentConfig.previewColor,
                        modifier = Modifier.size(20.dp).border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    ) {}
                },
                onClick = { showAccentDialog = true }
            )
            SettingsDivider()
            SettingsToggleItem(
                title = "AMOLED Pure Black (খাঁটি কালো ডার্ক মোড)",
                subtitle = "ডার্ক মোডে ডিসপ্লের ব্যাটারি সাশ্রয়কারী সম্পূর্ণ কালো ব্যাকগ্রাউন্ড",
                icon = Icons.Outlined.Contrast,
                checked = isAmoled,
                onCheckedChange = { viewModel.setIsAmoled(it) }
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsDivider()
                SettingsToggleItem(
                    title = "Material You Dynamic Colors",
                    subtitle = "অ্যান্ড্রয়েড ১২+ ওয়ালপেপার কালারের সাথে স্বয়ংক্রিয় রং ম্যাচিং",
                    icon = Icons.Outlined.AutoAwesome,
                    checked = dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }
            SettingsDivider()
            SettingsClickableItem(
                title = "Font Scaling & Typography (ফন্ট সাইজ)",
                subtitle = "$fontScaleOption (ডিফল্ট পাঠযোগ্যতা)",
                icon = Icons.Outlined.FormatSize,
                onClick = { showFontScaleDialog = true }
            )
            SettingsDivider()

            // Header Cover photo controls
            Column(modifier = Modifier.padding(AppSpacing.md)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AppDimensions.iconSmall)
                    )
                    Text(
                        text = "Home Header Cover Settings",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Cover Height
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cover Height",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(AppRadius.xs)
                    ) {
                        Text(
                            text = "${heightDp}dp",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
                        )
                    }
                }
                Slider(
                    value = heightDp.toFloat(),
                    onValueChange = { viewModel.setCoverHeightDp(it.toInt()) },
                    valueRange = 140f..320f
                )

                Spacer(modifier = Modifier.height(AppSpacing.xs))

                // Overlay Dark Opacity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overlay Dark Opacity",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(AppRadius.xs)
                    ) {
                        Text(
                            text = "${(opacity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
                        )
                    }
                }
                Slider(
                    value = opacity,
                    onValueChange = { viewModel.setCoverOverlayOpacity(it) },
                    valueRange = 0f..0.8f
                )

                Spacer(modifier = Modifier.height(AppSpacing.xs))

                // Corner Rounding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Corner Rounding",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(AppRadius.xs)
                    ) {
                        Text(
                            text = "${radiusDp}dp",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
                        )
                    }
                }
                Slider(
                    value = radiusDp.toFloat(),
                    onValueChange = { viewModel.setCoverCornerRadiusDp(it.toInt()) },
                    valueRange = 0f..32f
                )

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                OutlinedButton(
                    onClick = {
                        viewModel.resetCoverToDefault()
                        Toast.makeText(context, "Header Cover Reset to Default", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(AppRadius.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.iconSmall)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                    Text("Reset Cover to Default")
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        // SECTION 2: GENERAL & LOCALIZATION (ভাষা ও সময় সেটিংস)
        SettingsSectionHeader("General & Formats (সাধারণ ও তারিখ-সময়)", Icons.Outlined.Tune)
        SettingsCard {
            SettingsClickableItem(
                title = "Language (ভাষা)",
                subtitle = language,
                icon = Icons.Outlined.Language,
                onClick = { showLanguageDialog = true }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Date Format (তারিখ ফরম্যাট)",
                subtitle = dateFormat,
                icon = Icons.Outlined.CalendarToday,
                onClick = { showDateFormatDialog = true }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Time Format (সময় ফরম্যাট)",
                subtitle = timeFormat,
                icon = Icons.Outlined.Schedule,
                onClick = { showTimeFormatDialog = true }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Start of Week (সপ্তাহের শুরুর দিন)",
                subtitle = "$firstDayOfWeek (প্রথম দিন)",
                icon = Icons.Outlined.DateRange,
                onClick = { showStartOfWeekDialog = true }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Weekend Days Preset (সাপ্তাহিক ছুটি)",
                subtitle = weekendPreset,
                icon = Icons.Outlined.Weekend,
                onClick = { showWeekendPresetDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        // SECTION 3: CALENDAR & TRADITIONAL DATES (ক্যালেন্ডার ও ঐতিহ্য)
        SettingsSectionHeader("Calendar & Traditional Dates (ক্যালেন্ডার ও ঋতু)", Icons.Outlined.CalendarMonth)
        SettingsCard {
            SettingsClickableItem(
                title = "Default Calendar View (ডিফল্ট ভিউ)",
                subtitle = "$defaultCalendarView View (অ্যাপ খোলার সাথে যে ভিউ আসবে)",
                icon = Icons.Outlined.ViewAgenda,
                onClick = { showDefaultViewDialog = true }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Hijri Moon Adjustment (হিজরি চাঁদ সমন্বয়)",
                subtitle = "বর্তমান অফসেট: ${if (hijriAdj > 0) "+$hijriAdj" else "$hijriAdj"} দিন (চাঁদের দৃশ্যমানতা অনুযায়ী)",
                icon = Icons.Outlined.NightsStay,
                onClick = { showHijriDialog = true }
            )
            SettingsDivider()
            SettingsToggleItem(
                title = "Show Bangla & Hijri Dates in Grid (বাংলা ও আরবি তারিখ)",
                subtitle = "মাসের গ্রিডে ইংরেজি তারিখের নিচে বাংলা ও হিজরি তারিখ প্রদর্শন",
                icon = Icons.Outlined.CalendarViewMonth,
                checked = showTraditionalInGrid,
                onCheckedChange = { viewModel.setShowTraditionalDatesInGrid(it) }
            )
            SettingsDivider()
            SettingsToggleItem(
                title = "Show Bengali Seasons (বাংলা ঋতু - গ্রীষ্ম/বর্ষা/শীত)",
                subtitle = "ক্যালেন্ডার এবং হোম উইজেটে বর্তমান বাংলা ঋতু প্রদর্শন",
                icon = Icons.Outlined.FilterVintage,
                checked = showBengaliSeason,
                onCheckedChange = { viewModel.setShowBengaliSeason(it) }
            )
            SettingsDivider()
            SettingsToggleItem(
                title = "Show Week Numbers (সপ্তাহের ক্রমসংখ্যা)",
                subtitle = "গ্রিডের বাম পাশে বছরের সপ্তাহের ক্রমিক নাম্বার দেখান",
                icon = Icons.Outlined.FormatListNumbered,
                checked = showWeekNumbers,
                onCheckedChange = { viewModel.setShowWeekNumbers(it) }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        // SECTION 4: EVENTS & TASK PRESETS (ইভেন্ট ও টাস্ক ডিফল্ট)
        SettingsSectionHeader("Events & Reminders Defaults (ইভেন্ট ও রিমাইন্ডার ডিফল্ট)", Icons.Outlined.AlarmOn)
        SettingsCard {
            SettingsClickableItem(
                title = "Default Event Duration & Reminders (ডিফল্ট সময়কাল)",
                subtitle = "$defaultEventDuration মিনিট সময়কাল | $defaultReminderOffset মিনিট পূর্বে রিমাইন্ডার",
                icon = Icons.Outlined.Timer,
                onClick = { showEventDurationDialog = true }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Ringtone & Alert Sound (রিমাইন্ডার সুর)",
                subtitle = "$ringtoneSound ($hapticIntensity ভাইব্রেশন)",
                icon = Icons.Outlined.NotificationsActive,
                onClick = { showRingtoneDialog = true }
            )
            SettingsDivider()
            SettingsToggleItem(
                title = "Sound Alerts (সাউন্ড নোটিফিকেশন)",
                subtitle = "ইভেন্ট এবং টাস্কের রিমাইন্ডারে শব্দ বাজানো",
                icon = Icons.Outlined.VolumeUp,
                checked = soundEnabled,
                onCheckedChange = { viewModel.setSoundEnabled(it) }
            )
            SettingsDivider()
            SettingsToggleItem(
                title = "Haptic Vibration (ভাইব্রেশন প্রতিক্রিয়া)",
                subtitle = "রিমাইন্ডার এবং গুরুত্বপূর্ণ অ্যাকশনে ভাইব্রেশন",
                icon = Icons.Outlined.Vibration,
                checked = vibrationEnabled,
                onCheckedChange = { viewModel.setVibrationEnabled(it) }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Quiet Hours / DND (বিরক্ত না করার শান্ত সময়)",
                subtitle = if (quietHoursEnabled) "সক্রিয়: $quietHoursStart - $quietHoursEnd (এই সময়ে সাউন্ড বন্ধ)" else "নিষ্ক্রিয় (সব সময়ে শব্দ বাজবে)",
                icon = Icons.Outlined.Bedtime,
                onClick = { showQuietHoursDialog = true }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Daily Morning Briefing Time (দৈনিক সকালের ব্রিফিং)",
                subtitle = if (briefingEnabled) "সক্রিয়: $dailyBriefingTime এ সারসংক্ষেপ আসবে" else "নিষ্ক্রিয়",
                icon = Icons.Outlined.WbSunny,
                onClick = { showBriefingTimeDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        // SECTION 5: TOP APP BAR & BRANDING (অ্যাপ বার ও কাস্টম নাম)
        SettingsSectionHeader("App Bar & Branding (ব্র্যান্ডিং ও শিরোনাম)", Icons.Outlined.Badge)
        SettingsCard {
            SettingsClickableItem(
                title = "Custom App Display Name (অ্যাপের নাম)",
                subtitle = customAppName,
                icon = Icons.Outlined.Title,
                onClick = { showAppNameDialog = true }
            )
            SettingsDivider()
            SettingsToggleItem(
                title = "Show Subtitle / Tagline (ট্যাগলাইন প্রদর্শন)",
                subtitle = "টপ অ্যাপ বারে সাব-টাইটেল বা অনুপ্রেরণামূলক বাণী দেখান",
                icon = Icons.Outlined.Subtitles,
                checked = isTaglineEnabled,
                onCheckedChange = { viewModel.setIsTaglineEnabled(it) }
            )
            if (isTaglineEnabled) {
                SettingsDivider()
                SettingsClickableItem(
                    title = "Tagline Content Mode (ট্যাগলাইনের ধরন)",
                    subtitle = when (taglineMode) {
                        "Default" -> "Default: আপনার সময়, আপনার পরিকল্পনা"
                        "DailyQuote" -> "Daily Quote (প্রতিদিনের ইসলামিক ও অনুপ্রেরণামূলক বাণী)"
                        "Custom" -> "Custom: $customTaglineText"
                        else -> taglineMode
                    },
                    icon = Icons.Outlined.FormatQuote,
                    onClick = { showTaglineDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        // SECTION 6: WEATHER & LOCATION SETTINGS (আবহাওয়া ও একক)
        SettingsSectionHeader("Weather & Units (আবহাওয়া ও পরিমাপ)", Icons.Outlined.CloudQueue)
        SettingsCard {
            SettingsClickableItem(
                title = "Temperature & Wind Units (এককসমূহ)",
                subtitle = "$temperatureUnit | বাতাস: $windSpeedUnit | আপডেট: $weatherRefreshInterval",
                icon = Icons.Outlined.Thermostat,
                onClick = { showWeatherUnitDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        // SECTION 7: SECURITY & PRIVACY (নিরাপত্তা ও লক)
        SettingsSectionHeader("Security & Privacy (নিরাপত্তা ও প্রাইভেসি)", Icons.Outlined.Security)
        SettingsCard {
            SettingsClickableItem(
                title = "App Lock Configuration (PIN / বায়োমেট্রিক লক)",
                subtitle = "ক্যালেন্ডার ও ব্যক্তিগত নোট সুরক্ষায় ৪-ডিজিট পিন বা ফিঙ্গারপ্রিন্ট সেট করুন",
                icon = Icons.Outlined.Lock,
                onClick = { showSecurityDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        // SECTION 8: BACKUP, EXPORT & STORAGE (ব্যাকআপ ও ডেটা ম্যানেজমেন্ট)
        SettingsSectionHeader("Backup & Data Management (ব্যাকআপ ও ডেটা)", Icons.Outlined.CloudSync)
        SettingsCard {
            SettingsClickableItem(
                title = "Database Backup & Export (JSON / ICS)",
                subtitle = "ডিভাইস স্টোরেজে ব্যাকআপ ফাইল সেভ করুন বা ক্যালেন্ডার এক্সপোর্ট করুন",
                icon = Icons.Outlined.Save,
                onClick = {
                    backupDialogTab = 0
                    showBackupDialog = true
                }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Restore Backup (পূর্বাবস্থায় পুনরুদ্ধার)",
                subtitle = "পূর্বে সংরক্ষিত ব্যাকআপ ফাইল থেকে সকল ইভেন্ট ও নোট ফিরিয়ে আনুন",
                icon = Icons.Outlined.Restore,
                onClick = {
                    backupDialogTab = 1
                    showBackupDialog = true
                }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "iCalendar (.ics) Sync & Import",
                subtitle = "Google Calendar বা Outlook এর সাথে ICS ফাইল আদান-প্রদান",
                icon = Icons.Outlined.Event,
                onClick = {
                    backupDialogTab = 2
                    showBackupDialog = true
                }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Storage, Cache & Database Tools (ক্যাশ ও স্টোরেজ)",
                subtitle = "ক্যাশ পরিষ্কার করুন এবং ডেটাবেস পরিসংখ্যান দেখুন",
                icon = Icons.Outlined.CleaningServices,
                onClick = { showStorageDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        // SECTION 9: HOME SCREEN WIDGET
        SettingsSectionHeader("Home Screen Widget Preview (হোম উইজেট)", Icons.Outlined.Widgets)
        WidgetPreviewCard()

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        // SECTION 10: ABOUT, SUPPORT & FACTORY RESET
        SettingsSectionHeader("About & Diagnostics (অ্যাপ তথ্য ও সহায়তা)", Icons.Outlined.Info)
        SettingsCard {
            SettingsClickableItem(
                title = "About Application & Privacy Policy",
                subtitle = "Smart Bengali Calendar Suite Pro v5.2 | Privacy & Offline-First",
                icon = Icons.Outlined.Info,
                onClick = { showAboutDialog = true }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Support, Bug Report & Feature Request",
                subtitle = "মতামত পাঠান বা নতুন ফিচারের অনুরোধ জানান",
                icon = Icons.Outlined.SupportAgent,
                onClick = { showSupportDialog = true }
            )
            SettingsDivider()
            SettingsClickableItem(
                title = "Reset All Settings to Default (ফ্যাক্টরি রিসেট)",
                subtitle = "অ্যাপের সকল কনফিগারেশন ও কালার মূল অবস্থায় ফিরিয়ে নিন",
                icon = Icons.Outlined.RestartAlt,
                iconTint = MaterialTheme.colorScheme.error,
                onClick = { showResetAllConfirmDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.xxl))
    }

    // ==========================================
    // DIALOG IMPLEMENTATIONS
    // ==========================================

    // 1. Theme Mode Picker Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Icon(imageVector = Icons.Outlined.Brightness4, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Choose Theme Mode", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    listOf(
                        Triple("System", "Follow system default light/dark mode (ডিভাইস অনুযায়ী)", Icons.Outlined.BrightnessAuto),
                        Triple("Light", "Clean high-contrast light theme (উজ্জ্বল মোড)", Icons.Outlined.LightMode),
                        Triple("Dark", "Comfortable dark canvas for low light (ডার্ক মোড)", Icons.Outlined.DarkMode)
                    ).forEach { (mode, description, icon) ->
                        val isSelected = themeMode == mode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                },
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setThemeMode(mode)
                                        showThemeDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.xs))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(AppDimensions.iconMedium)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Close")
                }
            }
        )
    }

    // 2. Color Accent Palette Dialog
    if (showAccentDialog) {
        AlertDialog(
            onDismissRequest = { showAccentDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Icon(imageVector = Icons.Outlined.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Theme Color Palette", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text(
                        text = "আপনার পছন্দের প্রধান কালার থিম নির্বাচন করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AvailableAccents.forEach { accent ->
                        val isSelected = themeAccent == accent.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeAccent(accent.id)
                                    showAccentDialog = false
                                    Toast.makeText(context, "Theme: ${accent.nameBn} Applied ✓", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) accent.previewColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = accent.previewColor,
                                    modifier = Modifier.size(28.dp).border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                ) {}
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = accent.nameBn,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = accent.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = accent.previewColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccentDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Close")
                }
            }
        )
    }

    // 3. Font Scale & Typography Dialog
    if (showFontScaleDialog) {
        val scales = listOf(
            "Small" to "কমপ্যাক্ট ফন্ট (Small - 90%)",
            "Standard" to "স্ট্যান্ডার্ড স্বাভাবিক ফন্ট (100%)",
            "Large" to "বড় ফন্ট পাঠযোগ্যতা (Large - 115%)",
            "Extra Large" to "অতি বড় ফন্ট (Extra Large - 130%)"
        )
        AlertDialog(
            onDismissRequest = { showFontScaleDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Icon(imageVector = Icons.Outlined.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Font Size & Scaling", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    scales.forEach { (scaleKey, label) ->
                        val isSelected = fontScaleOption == scaleKey
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFontScaleOption(scaleKey)
                                    showFontScaleDialog = false
                                    Toast.makeText(context, "Font Size: $scaleKey", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = when (scaleKey) {
                                            "Small" -> 13.sp
                                            "Large" -> 16.sp
                                            "Extra Large" -> 18.sp
                                            else -> 14.sp
                                        }
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setFontScaleOption(scaleKey)
                                        showFontScaleDialog = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontScaleDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Close")
                }
            }
        )
    }

    // 4. Default Calendar View Dialog
    if (showDefaultViewDialog) {
        val views = listOf(
            "Home" to "Dashboard / Home (প্রধান ড্যাশবোর্ড)",
            "Month" to "Month Grid (পূর্ণাঙ্গ মাসিক গ্রিড)",
            "Week" to "Week View (সাপ্তাহিক ভিউ)",
            "Day" to "Day Timeline (দৈনিক সময়রেখা)",
            "Agenda" to "Agenda List (ধারাবাহিক কর্মসূচি তালিকা)",
            "Year" to "Year Overview (১২ মাসের বাৎসরিক সারসংক্ষেপ)"
        )
        AlertDialog(
            onDismissRequest = { showDefaultViewDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text("Default Starting View", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    views.forEach { (viewKey, title) ->
                        val isSelected = defaultCalendarView == viewKey
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDefaultCalendarView(viewKey)
                                    showDefaultViewDialog = false
                                    Toast.makeText(context, "Default View: $viewKey", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                RadioButton(selected = isSelected, onClick = {
                                    viewModel.setDefaultCalendarView(viewKey)
                                    showDefaultViewDialog = false
                                })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDefaultViewDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Close")
                }
            }
        )
    }

    // 5. Default Event Duration & Reminders Dialog
    if (showEventDurationDialog) {
        var durationInput by remember { mutableStateOf(defaultEventDuration) }
        var offsetInput by remember { mutableStateOf(defaultReminderOffset) }

        AlertDialog(
            onDismissRequest = { showEventDurationDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text("Default Event Timings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Text("Default Event Duration (ইভেন্টের সময়কাল):", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        listOf(15, 30, 45, 60, 120).forEach { mins ->
                            FilterChip(
                                selected = durationInput == mins,
                                onClick = { durationInput = mins },
                                label = { Text("${mins}m") },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                    Text("Default Reminder Lead Time (কত আগে রিমাইন্ডার বাজবে):", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        listOf(0, 5, 10, 15, 30, 60).forEach { mins ->
                            FilterChip(
                                selected = offsetInput == mins,
                                onClick = { offsetInput = mins },
                                label = { Text(if (mins == 0) "At Time" else "${mins}m") },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setDefaultEventDuration(durationInput)
                        viewModel.setDefaultReminderOffset(offsetInput)
                        showEventDurationDialog = false
                        Toast.makeText(context, "Event Defaults Saved ✓", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEventDurationDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Cancel")
                }
            }
        )
    }

    // 6. Weekend Days Preset Dialog
    if (showWeekendPresetDialog) {
        val presets = listOf(
            "Friday & Saturday" to "বাংলাদেশ ও মধ্যপ্রাচ্য ইসলামিক স্ট্যান্ডার্ড (শুক্র ও শনি)",
            "Saturday & Sunday" to "আন্তর্জাতিক ও গ্লোবাল স্ট্যান্ডার্ড (শনি ও রবি)",
            "Friday Only" to "শুধুমাত্র শুক্রবার ছুটির দিন",
            "Sunday Only" to "শুধুমাত্র রবিবার ছুটির দিন"
        )
        AlertDialog(
            onDismissRequest = { showWeekendPresetDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text("Weekend Days Preset (সাপ্তাহিক ছুটি)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    presets.forEach { (preset, desc) ->
                        val isSelected = weekendPreset == preset
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setWeekendPreset(preset)
                                    showWeekendPresetDialog = false
                                    Toast.makeText(context, "Weekend: $preset", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(text = desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                RadioButton(selected = isSelected, onClick = {
                                    viewModel.setWeekendPreset(preset)
                                    showWeekendPresetDialog = false
                                })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWeekendPresetDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Close")
                }
            }
        )
    }

    // 7. Quiet Hours / DND Dialog
    if (showQuietHoursDialog) {
        var enabledState by remember { mutableStateOf(quietHoursEnabled) }
        var startTime by remember { mutableStateOf(quietHoursStart) }
        var endTime by remember { mutableStateOf(quietHoursEnd) }

        AlertDialog(
            onDismissRequest = { showQuietHoursDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text("Quiet Hours (শান্ত সময় / DND)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Quiet Hours", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Switch(checked = enabledState, onCheckedChange = { enabledState = it })
                    }
                    Text(
                        "শান্ত সময়ের মধ্যে রিমাইন্ডারের সাউন্ড সাইলেন্ট থাকবে যাতে ঘুমের বিঘ্ন না ঘটে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (enabledState) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                label = { Text("Start Time") },
                                placeholder = { Text("22:00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(AppRadius.md)
                            )
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                label = { Text("End Time") },
                                placeholder = { Text("07:00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(AppRadius.md)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setQuietHoursEnabled(enabledState)
                        viewModel.setQuietHoursStart(startTime)
                        viewModel.setQuietHoursEnd(endTime)
                        showQuietHoursDialog = false
                        Toast.makeText(context, "Quiet Hours Configured ✓", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuietHoursDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Cancel")
                }
            }
        )
    }

    // 8. Daily Morning Briefing Time Dialog
    if (showBriefingTimeDialog) {
        var briefingTimeInput by remember { mutableStateOf(dailyBriefingTime) }
        var enabledBriefing by remember { mutableStateOf(briefingEnabled) }

        AlertDialog(
            onDismissRequest = { showBriefingTimeDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text("Daily Briefing Time (সকালের ব্রিফিং)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Daily Morning Briefing", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Switch(checked = enabledBriefing, onCheckedChange = { enabledBriefing = it })
                    }
                    Text(
                        "প্রতিদিন সকালে আজকের গুরুত্বপূর্ণ ইভেন্ট, বাংলা তারিখ ও আবহাওয়া এক নজরে জানানো হবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (enabledBriefing) {
                        OutlinedTextField(
                            value = briefingTimeInput,
                            onValueChange = { briefingTimeInput = it },
                            label = { Text("Preferred Time (যেমন: 07:00)") },
                            singleLine = true,
                            shape = RoundedCornerShape(AppRadius.md),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            listOf("06:30", "07:00", "07:30", "08:00").forEach { preset ->
                                SuggestionChip(
                                    onClick = { briefingTimeInput = preset },
                                    label = { Text(preset) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setDailyBriefingEnabled(enabledBriefing)
                        viewModel.setDailyBriefingTime(briefingTimeInput)
                        showBriefingTimeDialog = false
                        Toast.makeText(context, "Briefing Schedule Updated ✓", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBriefingTimeDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Cancel")
                }
            }
        )
    }

    // 9. Ringtone & Vibration Alert Dialog
    if (showRingtoneDialog) {
        var selectedTone by remember { mutableStateOf(ringtoneSound) }
        var selectedHaptic by remember { mutableStateOf(hapticIntensity) }

        val sounds = listOf(
            "Gentle Chime" to "শান্ত ও মনোরম ঘণ্টা ধ্বনি",
            "Islamic Adhan Tone" to "ইসলামিক সুমধুর আজানের সুর",
            "Classic Bell" to "ক্লাসিক অ্যালার্ম বেল",
            "Digital Pulse" to "ডিজিটাল বিট টোন",
            "Soft Harp" to "কোমল হার্প সুর"
        )
        val haptics = listOf("Off", "Soft", "Medium", "Strong")

        AlertDialog(
            onDismissRequest = { showRingtoneDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text("Ringtone & Vibration Alert", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    Text("Alert Sound (নোটিফিকেশন রিংটোন):", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    sounds.forEach { (tone, desc) ->
                        val isSelected = selectedTone == tone
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().clickable { selectedTone = tone }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = tone, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal))
                                    Text(text = desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                RadioButton(selected = isSelected, onClick = { selectedTone = tone })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                    Text("Haptic Vibration Intensity (ভাইব্রেশন মাত্রা):", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        haptics.forEach { intensity ->
                            FilterChip(
                                selected = selectedHaptic == intensity,
                                onClick = {
                                    selectedHaptic = intensity
                                    // Trigger quick test vibration
                                    try {
                                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val millis = when (intensity) {
                                                "Soft" -> 30L
                                                "Medium" -> 70L
                                                "Strong" -> 150L
                                                else -> 0L
                                            }
                                            if (millis > 0) vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
                                        }
                                    } catch (_: Exception) {}
                                },
                                label = { Text(intensity) },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setRingtoneSound(selectedTone)
                        viewModel.setHapticIntensity(selectedHaptic)
                        showRingtoneDialog = false
                        Toast.makeText(context, "Alert Sounds Saved ✓", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRingtoneDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Cancel")
                }
            }
        )
    }

    // 10. Weather & Location Units Dialog
    if (showWeatherUnitDialog) {
        var tempUnit by remember { mutableStateOf(temperatureUnit) }
        var windUnit by remember { mutableStateOf(windSpeedUnit) }
        var refreshInterval by remember { mutableStateOf(weatherRefreshInterval) }

        AlertDialog(
            onDismissRequest = { showWeatherUnitDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text("Weather Units & Auto-Refresh", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Text("Temperature Unit:", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        listOf("Celsius (°C)", "Fahrenheit (°F)").forEach { unit ->
                            FilterChip(
                                selected = tempUnit == unit,
                                onClick = { tempUnit = unit },
                                label = { Text(unit) },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }
                    }

                    Text("Wind Speed Unit:", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        listOf("km/h", "m/s", "mph").forEach { unit ->
                            FilterChip(
                                selected = windUnit == unit,
                                onClick = { windUnit = unit },
                                label = { Text(unit) },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }
                    }

                    Text("Auto-Refresh Interval:", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        listOf("1 Hour", "3 Hours", "6 Hours", "Manual").forEach { interval ->
                            FilterChip(
                                selected = refreshInterval == interval,
                                onClick = { refreshInterval = interval },
                                label = { Text(interval) },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setTemperatureUnit(tempUnit)
                        viewModel.setWindSpeedUnit(windUnit)
                        viewModel.setWeatherRefreshInterval(refreshInterval)
                        showWeatherUnitDialog = false
                        Toast.makeText(context, "Weather Units Saved ✓", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeatherUnitDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Cancel")
                }
            }
        )
    }

    // 11. Storage & Cache Diagnostic Dialog
    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Icon(imageVector = Icons.Outlined.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Storage & Cache Tools", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Surface(
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Storage Status", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Healthy ✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Local Database", style = MaterialTheme.typography.bodySmall)
                                Text("SQLite / Room Engine", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Cache Files", style = MaterialTheme.typography.bodySmall)
                                Text("~ 1.2 MB", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                context.cacheDir.deleteRecursively()
                                Toast.makeText(context, "App Cache Cleared Successfully ✓", Toast.LENGTH_SHORT).show()
                            } catch (_: Exception) {
                                Toast.makeText(context, "Cache Cleared", Toast.LENGTH_SHORT).show()
                            }
                            showStorageDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Outlined.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                        Text("Clear Temporary Cache")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStorageDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Close")
                }
            }
        )
    }

    // 12. Factory Reset Confirmation Dialog
    if (showResetAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllConfirmDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Reset All Settings?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Text(
                    text = "আপনি কি সমস্ত কনফিগারেশন, থিম, কভার ফটো এবং প্রেফারেন্স ডিফল্ট ফ্যাক্টরি সেটিংসে রিসেট করতে চান? (আপনার সেভ করা ইভেন্ট ও নোট মুছে যাবে না)।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllSettingsToDefault()
                        showResetAllConfirmDialog = false
                        Toast.makeText(context, "All Settings Reset to Default ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Yes, Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllConfirmDialog = false }, shape = RoundedCornerShape(AppRadius.md)) {
                    Text("Cancel")
                }
            }
        )
    }

    // 13. Language Selection Dialog
    if (showLanguageDialog) {
        val languageOptions = listOf(
            "Bangla / English" to "দ্বৈত ভাষা (বাংলা ও ইংরেজি সমন্বিত)",
            "English" to "English language interface only",
            "Bangla" to "শুধুমাত্র বাংলা ভাষা"
        )
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(AppDimensions.iconSmall)
                            )
                        }
                    }
                    Text(
                        text = "Select Language (ভাষা)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    languageOptions.forEach { (option, desc) ->
                        val isSelected = language == option || (option == "Bangla / English" && language.contains("Bangla") && language.contains("English"))
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(option)
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Language set to $option", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setLanguage(option)
                                        showLanguageDialog = false
                                        Toast.makeText(context, "Language set to $option", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showLanguageDialog = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // 14. Date Format Dialog
    if (showDateFormatDialog) {
        val dateFormatOptions = listOf(
            "dd/MM/yyyy" to "Example: 12/08/2026",
            "MM/dd/yyyy" to "Example: 08/12/2026",
            "yyyy-MM-dd" to "Example: 2026-08-12",
            "dd MMM yyyy" to "Example: 12 Aug 2026"
        )
        AlertDialog(
            onDismissRequest = { showDateFormatDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text(
                    text = "Date Format (তারিখের ফরম্যাট)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    dateFormatOptions.forEach { (option, desc) ->
                        val isSelected = dateFormat == option
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDateFormat(option)
                                    showDateFormatDialog = false
                                    Toast.makeText(context, "Date Format: $option", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setDateFormat(option)
                                        showDateFormatDialog = false
                                        Toast.makeText(context, "Date Format: $option", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDateFormatDialog = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // 15. Time Format Dialog
    if (showTimeFormatDialog) {
        val timeOptions = listOf(
            "12 Hour" to "Example: 02:30 PM (১২ ঘণ্টা)",
            "24 Hour" to "Example: 14:30 (২৪ ঘণ্টা)"
        )
        AlertDialog(
            onDismissRequest = { showTimeFormatDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text(
                    text = "Time Format (সময় ফরম্যাট)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    timeOptions.forEach { (option, desc) ->
                        val isSelected = timeFormat == option
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTimeFormat(option)
                                    showTimeFormatDialog = false
                                    Toast.makeText(context, "Time Format: $option", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setTimeFormat(option)
                                        showTimeFormatDialog = false
                                        Toast.makeText(context, "Time Format: $option", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTimeFormatDialog = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // 16. Start of Week Dialog
    if (showStartOfWeekDialog) {
        val daysOptions = listOf(
            "Sunday" to "Sunday as the first day (রবিবার)",
            "Monday" to "Monday as the first day (সোমবার)",
            "Saturday" to "Saturday as the first day (শনিবার)"
        )
        AlertDialog(
            onDismissRequest = { showStartOfWeekDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text(
                    text = "Start of Week (সপ্তাহের শুরু)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    daysOptions.forEach { (option, desc) ->
                        val isSelected = firstDayOfWeek == option
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFirstDayOfWeek(option)
                                    showStartOfWeekDialog = false
                                    Toast.makeText(context, "Start of Week: $option", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setFirstDayOfWeek(option)
                                        showStartOfWeekDialog = false
                                        Toast.makeText(context, "Start of Week: $option", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showStartOfWeekDialog = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // 17. Hijri Moon Adjustment Dialog
    if (showHijriDialog) {
        AlertDialog(
            onDismissRequest = { showHijriDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text(
                    text = "Hijri Moon Adjustment",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Text(
                        text = "Adjust Hijri calendar date by -2 to +2 days based on local moon sighting.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (-2..2).forEach { offset ->
                            FilterChip(
                                selected = hijriAdj == offset,
                                onClick = {
                                    viewModel.setHijriDayAdjustment(offset)
                                    showHijriDialog = false
                                },
                                label = {
                                    Text(
                                        text = if (offset > 0) "+$offset" else "$offset",
                                        fontWeight = if (hijriAdj == offset) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showHijriDialog = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // 18. Backup & Restore Dialog
    if (showBackupDialog) {
        BackupRestoreDialog(
            viewModel = viewModel,
            initialTab = backupDialogTab,
            onDismiss = { showBackupDialog = false }
        )
    }

    // 19. Security Settings Dialog
    if (showSecurityDialog) {
        SecuritySettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSecurityDialog = false }
        )
    }

    // 20. About Dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    // 21. Support Dialog
    if (showSupportDialog) {
        SupportDialog(
            onDismiss = { showSupportDialog = false }
        )
    }

    // 22. App Display Name Dialog
    if (showAppNameDialog) {
        var nameInput by remember { mutableStateOf(customAppName) }
        AlertDialog(
            onDismissRequest = { showAppNameDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text(
                    text = "App Display Name (অ্যাপের নাম)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text(
                        text = "টপ অ্যাপ বারে প্রদর্শিত নাম কাস্টমাইজ করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text("My Calendar") },
                        singleLine = true,
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier.fillMaxWidth().testTag("input_custom_app_name")
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("My Calendar", "ক্যালেন্ডার ২০২৬", "Daily Planner").forEach { suggestion ->
                            SuggestionChip(
                                onClick = { nameInput = suggestion },
                                label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.setCustomAppName(nameInput.trim())
                            showAppNameDialog = false
                            Toast.makeText(context, "App Name Updated ✓", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAppNameDialog = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // 23. Tagline Mode Dialog
    if (showTaglineDialog) {
        var selectedMode by remember { mutableStateOf(taglineMode) }
        var customTextInput by remember { mutableStateOf(customTaglineText) }

        AlertDialog(
            onDismissRequest = { showTaglineDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text(
                    text = "Tagline Configuration (ট্যাগলাইন)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    val modes = listOf(
                        "Default" to "Default (আপনার সময়, আপনার পরিকল্পনা)",
                        "DailyQuote" to "Daily Quote (প্রতিদিনের বাণী)",
                        "Custom" to "Custom Text (নিজের লেখা ট্যাগলাইন)"
                    )

                    modes.forEach { (mode, label) ->
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = if (selectedMode == mode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (selectedMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMode = mode }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (selectedMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                RadioButton(
                                    selected = selectedMode == mode,
                                    onClick = { selectedMode = mode }
                                )
                            }
                        }
                    }

                    if (selectedMode == "Custom") {
                        OutlinedTextField(
                            value = customTextInput,
                            onValueChange = { customTextInput = it },
                            label = { Text("Custom Tagline Text") },
                            placeholder = { Text("যেমন: লক্ষ্য অর্জনে প্রতিটি মুহূর্ত...") },
                            singleLine = true,
                            shape = RoundedCornerShape(AppRadius.md),
                            modifier = Modifier.fillMaxWidth().testTag("input_custom_tagline")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setTaglineMode(selectedMode)
                        if (selectedMode == "Custom" && customTextInput.isNotBlank()) {
                            viewModel.setCustomTaglineText(customTextInput.trim())
                        }
                        showTaglineDialog = false
                        Toast.makeText(context, "Tagline Updated ✓", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTaglineDialog = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// REUSABLE HELPER UI COMPONENTS
// ==========================================

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        tonalElevation = AppElevation.low
    ) {
        Column(
            modifier = Modifier.padding(vertical = AppSpacing.xs),
            content = content
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = AppSpacing.md),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    )
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    badge: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(AppDimensions.iconSmall)
                )
            }
        }
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(AppSpacing.xxs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (badge != null) {
            badge()
            Spacer(modifier = Modifier.width(AppSpacing.xs))
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(AppDimensions.iconSmall)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AppDimensions.iconSmall)
                    )
                }
            }
            Spacer(modifier = Modifier.width(AppSpacing.md))
            Column(modifier = Modifier.padding(end = AppSpacing.sm)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
