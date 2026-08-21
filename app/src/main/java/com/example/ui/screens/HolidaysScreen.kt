package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Holiday
import com.example.ui.components.CustomHolidayEditorDialog
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidaysScreen(
    viewModel: CalendarViewModel,
    onBack: (() -> Unit)? = null,
    onQuickAdd: (() -> Unit)? = null
) {
    val allHolidays by viewModel.allHolidays.collectAsState()
    var selectedCategory by remember { mutableStateOf("সবগুলো") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddHolidayDialog by remember { mutableStateOf(false) }
    var selectedSignificanceHoliday by remember { mutableStateOf<Holiday?>(null) }

    val filterChips = listOf("সবগুলো", "জাতীয় দিবস", "ইসলামিক ছুটি", "সরকারি ছুটি")

    val todayMidnight = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Default fallback holidays if list is empty
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val displayHolidays = remember(allHolidays, currentYear) {
        if (allHolidays.isNotEmpty()) {
            allHolidays
        } else {
            getDefaultFallbackHolidays(currentYear)
        }
    }

    // Filtering logic
    val filteredHolidays = remember(displayHolidays, selectedCategory, searchQuery) {
        val sorted = displayHolidays.sortedBy { it.date }
        val categoryFiltered = when (selectedCategory) {
            "জাতীয় দিবস" -> sorted.filter {
                it.type.contains("National", ignoreCase = true) || it.type.contains("জাতীয়", ignoreCase = true)
            }
            "ইসলামিক ছুটি" -> sorted.filter {
                it.type.contains("Islamic", ignoreCase = true) ||
                it.type.contains("Hijri", ignoreCase = true) ||
                it.calendarType.equals("Hijri", ignoreCase = true)
            }
            "সরকারি ছুটি" -> sorted.filter {
                it.type.contains("Public", ignoreCase = true) ||
                it.type.contains("Government", ignoreCase = true) ||
                it.type.contains("Bangladesh", ignoreCase = true) ||
                it.type.contains("সরকারি", ignoreCase = true)
            }
            else -> sorted
        }

        if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.nameBn.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("holidays_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Celebration Icon Badge
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🎉", fontSize = 18.sp)
                                }
                            }

                            Column {
                                Text(
                                    text = "বার্ষিক ছুটির তালিকা (Holidays)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$currentYear সালের সরকারি ও ধর্মীয় ছুটির তালিকা",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Right Total Count Badge
                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = MaterialTheme.colorScheme.primary,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = CalendarUtils.toBanglaDigit(displayHolidays.size),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "টি",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // Horizontal Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = AppSpacing.md, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        filterChips.forEach { chipName ->
                            val isSelected = selectedCategory == chipName
                            Surface(
                                shape = RoundedCornerShape(AppRadius.full),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.clickable { selectedCategory = chipName }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val chipIcon = when (chipName) {
                                        "জাতীয় দিবস" -> "🇧🇩"
                                        "ইসলামিক ছুটি" -> "🌙"
                                        "সরকারি ছুটি" -> "🏛️"
                                        else -> "🗓️"
                                    }
                                    Text(text = chipIcon, fontSize = 12.sp)
                                    Text(
                                        text = chipName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.5.sp
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (onQuickAdd != null) {
                        onQuickAdd()
                    } else {
                        showAddHolidayDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(AppRadius.full),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("+ Quick Add", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_quick_add_holidays")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            if (filteredHolidays.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xl),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(AppRadius.lg),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.xl),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("🏖️", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                            Text(
                                text = "কোনো ছুটি পাওয়া যায়নি",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ফিল্টার পরিবর্তন করে আবার চেষ্টা করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredHolidays, key = { it.id }) { holiday ->
                    HolidayCardItem(
                        holiday = holiday,
                        todayMidnight = todayMidnight,
                        onSignificanceClick = {
                            selectedSignificanceHoliday = holiday
                        }
                    )
                }
            }

            // Bottom Spacing for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Significance & History Bottom Sheet / Dialog
    selectedSignificanceHoliday?.let { holiday ->
        HolidaySignificanceDialog(
            holiday = holiday,
            onDismiss = { selectedSignificanceHoliday = null }
        )
    }

    // Add Custom Holiday Dialog
    if (showAddHolidayDialog) {
        CustomHolidayEditorDialog(
            onDismiss = { showAddHolidayDialog = false },
            onSave = {
                viewModel.saveHoliday(it)
                showAddHolidayDialog = false
            }
        )
    }
}

/**
 * Premium Holiday Card with Left Date Block + Right Type Badge + Bangla/English Names + Dates + Significance Button
 */
@Composable
fun HolidayCardItem(
    holiday: Holiday,
    todayMidnight: Long,
    onSignificanceClick: () -> Unit
) {
    val holidayCal = remember(holiday.date) {
        Calendar.getInstance().apply { timeInMillis = holiday.date }
    }

    val monthShort = remember(holiday.date) {
        SimpleDateFormat("MMM", Locale.ENGLISH).format(Date(holiday.date)).uppercase(Locale.ENGLISH)
    }
    val dayNum = remember(holiday.date) {
        holidayCal.get(Calendar.DAY_OF_MONTH).toString()
    }
    val dayOfWeek = remember(holiday.date) {
        SimpleDateFormat("EEE", Locale.ENGLISH).format(Date(holiday.date))
    }

    val daysDiff = remember(holiday.date, todayMidnight) {
        val targetMidnight = Calendar.getInstance().apply {
            timeInMillis = holiday.date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        ((targetMidnight - todayMidnight) / (1000 * 60 * 60 * 24)).toInt()
    }

    val isToday = daysDiff == 0
    val banglaDate = remember(holiday.date) { CalendarUtils.getBanglaDate(holidayCal) }

    // Categorization badge
    val isNational = holiday.type.contains("National", ignoreCase = true) || holiday.type.contains("জাতীয়", ignoreCase = true)
    val isIslamic = holiday.type.contains("Islamic", ignoreCase = true) || holiday.type.contains("Hijri", ignoreCase = true)
    val isInternational = holiday.type.contains("International", ignoreCase = true)

    val (badgeText, badgeColor, badgeBg) = remember(holiday.type, isNational, isIslamic, isInternational) {
        when {
            isNational -> Triple("🇧🇩 জাতীয় দিবস", null, null)
            isIslamic -> Triple("🌙 ইসলামিক ছুটি", null, null)
            isInternational -> Triple("🌍 আন্তর্জাতিক দিবস", null, null)
            else -> Triple("🏛️ সরকারি ছুটি", null, null)
        }
    }

    val badgeContainerColor = when {
        isNational -> MaterialTheme.colorScheme.secondaryContainer
        isIslamic -> MaterialTheme.colorScheme.tertiaryContainer
        isInternational -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val badgeTextColor = when {
        isNational -> MaterialTheme.colorScheme.onSecondaryContainer
        isIslamic -> MaterialTheme.colorScheme.onTertiaryContainer
        isInternational -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("holiday_card_${holiday.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isToday) 4.dp else 1.dp,
        shadowElevation = if (isToday) 3.dp else 1.dp,
        border = BorderStroke(
            if (isToday) 1.5.dp else 1.dp,
            if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md)
        ) {
            // Main Top Section: Left Large Date Block + Right Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // LEFT: Large Date Block (Month + Day + Day of Week, e.g. "JAN 1 Thu")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.size(width = 62.dp, height = 74.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Month (e.g. "JAN")
                        Text(
                            text = monthShort,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp
                            ),
                            color = if (isToday) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                        )

                        // Day (e.g. "1" or "21")
                        Text(
                            text = dayNum,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                lineHeight = 22.sp
                            ),
                            color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )

                        // Weekday (e.g. "Thu" or "Sat")
                        Text(
                            text = dayOfWeek,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            color = if (isToday) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // RIGHT: Holiday Details
                Column(modifier = Modifier.weight(1f)) {
                    // Top Row: Type Badge + Days Remaining Countdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Holiday Type Badge
                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = badgeContainerColor
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = badgeTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                            )
                        }

                        // Days Remaining
                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = when {
                                isToday -> MaterialTheme.colorScheme.errorContainer
                                daysDiff in 1..30 -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            }
                        ) {
                            Text(
                                text = when {
                                    isToday -> "🎉 আজ ছুটি!"
                                    daysDiff == 1 -> "আগামীকাল"
                                    daysDiff in 2..365 -> "${CalendarUtils.toBanglaDigit(daysDiff)} দিন বাকি"
                                    else -> "অতিক্রান্ত"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = when {
                                    isToday -> MaterialTheme.colorScheme.onErrorContainer
                                    daysDiff in 1..30 -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bengali Name
                    Text(
                        text = holiday.nameBn.ifBlank { holiday.name },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = (-0.1).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // English Name
                    if (holiday.name.isNotBlank() && holiday.name != holiday.nameBn) {
                        Text(
                            text = holiday.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Full Date + Bangla Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📅 ${CalendarUtils.formatDate(holiday.date, "d MMMM yyyy")}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("•", fontSize = 9.sp, color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "🌾 ${CalendarUtils.toBanglaDigit(banglaDate.day)} ${banglaDate.monthNameBn} ${CalendarUtils.toBanglaDigit(banglaDate.year)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Light-Colored Button: "ছুটির কারণ ও তাৎপর্য"
            Surface(
                onClick = onSignificanceClick,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ছুটির কারণ ও তাৎপর্য",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Holiday Significance & Background Information Dialog
 */
@Composable
fun HolidaySignificanceDialog(
    holiday: Holiday,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val holidayCal = remember(holiday.date) { Calendar.getInstance().apply { timeInMillis = holiday.date } }
    val banglaDate = remember(holiday.date) { CalendarUtils.getBanglaDate(holidayCal) }
    val hijriDate = remember(holiday.date) { CalendarUtils.getHijriDate(holidayCal) }

    val significanceContent = remember(holiday) {
        if (holiday.description.isNotBlank()) {
            holiday.description
        } else {
            getDefaultHolidaySignificance(holiday.name, holiday.nameBn, holiday.type)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📖", fontSize = 20.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = holiday.nameBn.ifBlank { holiday.name },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ছুটির ঐতিহাসিক ও সামাজিক তাৎপর্য",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                // Calendar Date Pills
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📅", fontSize = 12.sp)
                            Text(
                                text = "ইংরেজি তারিখ: ${CalendarUtils.formatDate(holiday.date, "EEEE, d MMMM yyyy")}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🌾", fontSize = 12.sp)
                            Text(
                                text = "বাংলা তারিখ: ${CalendarUtils.toBanglaDigit(banglaDate.day)} ${banglaDate.monthNameBn} ${CalendarUtils.toBanglaDigit(banglaDate.year)} বঙ্গাব্দ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🌙", fontSize = 12.sp)
                            Text(
                                text = "হিজরী সন: ${CalendarUtils.toBanglaDigit(hijriDate.day)} ${hijriDate.monthNameEn} ${CalendarUtils.toBanglaDigit(hijriDate.year)} হিজরী",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Significance Body
                Text(
                    text = "পটভূমি ও তাৎপর্য:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = significanceContent,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        fontSize = 13.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("বুঝেছি (Close)")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    val shareText = "${holiday.nameBn} (${holiday.name})\nতারিখ: ${CalendarUtils.formatDate(holiday.date, "d MMMM yyyy")}\n\nতাৎপর্য:\n$significanceContent"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Holiday Info"))
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("শেয়ার করুন", fontSize = 12.sp)
            }
        }
    )
}

/**
 * Fallback significance dictionary for Bangladesh national and religious holidays
 */
private fun getDefaultHolidaySignificance(name: String, nameBn: String, type: String): String {
    return when {
        name.contains("New Year", ignoreCase = true) || nameBn.contains("নববর্ষ") ->
            "খ্রিষ্টীয় গ্রেগরীয় নতুন বর্ষের প্রথম দিন। বিশ্বজুড়ে নতুন আশা, সম্প্রীতি ও ইতিবাচক সংকল্পের মাধ্যমে বর্ষবরণ করা হয়।"

        name.contains("Shaheed", ignoreCase = true) || nameBn.contains("শহীদ দিবস") || nameBn.contains("মাতৃভাষা") ->
            "১৯৫২ সালের ২১ ফেব্রুয়ারি বাংলা ভাষার মর্যাদার দাবিতে সালাম, বরকত, রফিক, জব্বারসহ অসংখ্য বীর সন্তানের আত্মত্যাগের অমর স্মারক। ইউনেস্কো কর্তৃক দিনটি 'আন্তর্জাতিক মাতৃভাষা দিবস' হিসেবে বিশ্বব্যাপী মর্যাদাপ্রাপ্ত।"

        name.contains("Barat", ignoreCase = true) || nameBn.contains("শবে বরাত") ->
            "পবিত্র ১৫ শাবান রজনী—মুসলিম উম্মাহর নিকট মহিমান্বিত ক্ষমার রাত (লায়লাতুল বরাত)। মহান আল্লাহর সন্তুষ্টি অর্জন, দোয়া এবং রাতব্যাপী বিশেষ নফল ইবাদতের মাধ্যমে দিনটি পালিত হয়।"

        name.contains("Independence", ignoreCase = true) || nameBn.contains("স্বাধীনতা") ->
            "১৯৭১ সালের ২৬ মার্চ বাংলাদেশের মহান স্বাধীনতা ঘোষণা করা হয়। দীর্ঘ নয় মাসের রক্তক্ষয়ী মুক্তিযুদ্ধের সূচনা ও ত্রিশ লাখ শহীদের আত্মত্যাগের স্মরণে এটি আমাদের গৌরবময় জাতীয় দিবস।"

        name.contains("Boishakh", ignoreCase = true) || nameBn.contains("পহেলা বৈশাখ") ->
            "বাঙালি সংস্কৃতির প্রধান সার্বজনীন উৎসব। বাংলা নববর্ষ উপলক্ষে মঙ্গল শোভাযাত্রা, লোকমেলা ও হালখাতার মাধ্যমে নতুন বছরকে বরণ করা হয়।"

        name.contains("Fitr", ignoreCase = true) || nameBn.contains("ঈদুল ফিতর") ->
            "এক মাস সিয়াম সাধনা ও আত্মশুদ্ধির পর মুসলিম জাতির প্রধান ধর্মীয় আনন্দোৎসব। গরিব-দুঃখীদের ফিতরা প্রদান ও ভ্রাতৃত্বের মহামিলন ঘটে এই দিনে।"

        name.contains("Adha", ignoreCase = true) || nameBn.contains("ঈদুল আযহা") || nameBn.contains("কোরবানি") ->
            "হযরত ইব্রাহিম (আ.)-এর মহান আত্মত্যাগ ও আল্লাহর প্রতি পরম আনুগত্যের স্মারক। পশু কোরবানির মাধ্যমে ত্যাগের মহিমা ও সাম্যের বার্তা ছড়িয়ে দেওয়া হয়।"

        else ->
            "গণপ্রজাতন্ত্রী বাংলাদেশ সরকারের নির্বাহী আদেশে প্রজ্ঞাপিত রাষ্ট্রীয় ছুটির দিন। দিবসটি জাতীয়, ধর্মীয় ও সাংবিধানিক মূলবোধের সাথে গভীর তাৎপর্য বহন করে।"
    }
}

/**
 * Fallback holidays list ensuring 19+ standard holidays including New Year, Shaheed Day, Shab-e-Barat
 */
private fun getDefaultFallbackHolidays(year: Int): List<Holiday> {
    fun createTimestamp(month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    return listOf(
        Holiday(
            id = 1,
            name = "New Year's Day",
            nameBn = "ইংরেজি নববর্ষ",
            date = createTimestamp(0, 1),
            type = "International Day",
            calendarType = "Gregorian",
            description = "খ্রিষ্টীয় গ্রেগরীয় নতুন বছরের প্রথম দিন ও বিশ্বব্যাপী উৎসব।"
        ),
        Holiday(
            id = 2,
            name = "Shaheed Day & International Mother Language Day",
            nameBn = "শহীদ দিবস ও আন্তর্জাতিক মাতৃভাষা দিবস",
            date = createTimestamp(1, 21),
            type = "National Day",
            calendarType = "Gregorian",
            description = "১৯৫২ সালের মহান ভাষা আন্দোলনে মাতৃভাষা বাংলার অধিকার প্রতিষ্ঠার অমর শহীদদের স্মরণে জাতীয় ও আন্তর্জাতিক দিবস।"
        ),
        Holiday(
            id = 3,
            name = "Shab-e-Barat",
            nameBn = "পবিত্র শবে বরাত",
            date = createTimestamp(1, 25),
            type = "Islamic Holiday",
            calendarType = "Hijri",
            description = "১৫ শাবান বরকত ও মাগফিরাতের রজনী, মুসলিম উম্মাহর অন্যতম পুণ্যময় ইবাদত ও ক্ষমার রাত।"
        ),
        Holiday(
            id = 4,
            name = "Sheikh Mujibur Rahman Birthday & National Children's Day",
            nameBn = "বঙ্গবন্ধুর জন্মবার্ষিকী ও জাতীয় শিশু দিবস",
            date = createTimestamp(2, 17),
            type = "National Day",
            calendarType = "Gregorian",
            description = "জাতির জনক বঙ্গবন্ধু শেখ মুজিবুর রহমানের শুভ জন্মবার্ষিকী এবং জাতীয় শিশু দিবস।"
        ),
        Holiday(
            id = 5,
            name = "Independence Day & National Day",
            nameBn = "স্বাধীনতা ও জাতীয় দিবস",
            date = createTimestamp(2, 26),
            type = "National Day",
            calendarType = "Gregorian",
            description = "১৯৭১ সালের ২৬ মার্চ বাংলাদেশের মহান স্বাধীনতা ঘোষণার ঐতিহাসিক দিবস ও জাতীয় গৌরব।"
        ),
        Holiday(
            id = 6,
            name = "Shab-e-Qadr (Laylat al-Qadr)",
            nameBn = "পবিত্র শবে কদর",
            date = createTimestamp(2, 27),
            type = "Islamic Holiday",
            calendarType = "Hijri",
            description = "পবিত্র কুরআন অবতীর্ণ হওয়ার মহিমান্বিত ও হাজার মাসের চেয়ে শ্রেষ্ঠ পুণ্যময় রাত।"
        ),
        Holiday(
            id = 7,
            name = "Jumatul Wida",
            nameBn = "জুমাতুল বিদা",
            date = createTimestamp(2, 29),
            type = "Islamic Holiday",
            calendarType = "Hijri",
            description = "পবিত্র রমজানুল মোবারকের শেষ জুমা, ক্ষমা ও দোয়ার দিন।"
        ),
        Holiday(
            id = 8,
            name = "Eid-ul-Fitr",
            nameBn = "পবিত্র ঈদুল ফিতর",
            date = createTimestamp(2, 31),
            type = "Islamic Holiday",
            calendarType = "Hijri",
            description = "মাসব্যাপী সিয়াম সাধনার পর মুসলমানদের প্রধান ধর্মীয় আনন্দের উৎসব।"
        ),
        Holiday(
            id = 9,
            name = "Pohela Boishakh (Bengali New Year)",
            nameBn = "পহেলা বৈশাখ (বাংলা নববর্ষ)",
            date = createTimestamp(3, 14),
            type = "Bangladesh Public Holiday",
            calendarType = "Bangla",
            description = "বাঙালির সার্বজনীন প্রাণের উৎসব ও নতুন বাংলা বছরের প্রথম দিন (১ বৈশাখ)।"
        ),
        Holiday(
            id = 10,
            name = "May Day / International Workers' Day",
            nameBn = "মে দিবস (আন্তর্জাতিক শ্রমিক দিবস)",
            date = createTimestamp(4, 1),
            type = "International Day",
            calendarType = "Gregorian",
            description = "শ্রমজীবী মানুষের অধিকার আদায়ের ঐতিহাসিক আন্দোলনের স্মরণে বিশ্ব শ্রমিক দিবস।"
        ),
        Holiday(
            id = 11,
            name = "Buddha Purnima (Vesak)",
            nameBn = "বুদ্ধ পূর্ণিমা (বৈশাখী পূর্ণিমা)",
            date = createTimestamp(4, 12),
            type = "Bangladesh Public Holiday",
            calendarType = "Gregorian",
            description = "গৌতম বুদ্ধের শুভ জন্ম, বুদ্ধত্ব লাভ এবং মহাপরিনির্বাণ লাভের ত্রি-স্মৃতিবিজড়িত দিন।"
        ),
        Holiday(
            id = 12,
            name = "Eid-ul-Adha",
            nameBn = "পবিত্র ঈদুল আযহা (কোরবানি ঈদ)",
            date = createTimestamp(5, 7),
            type = "Islamic Holiday",
            calendarType = "Hijri",
            description = "হযরত ইব্রাহিম (আ.)-এর ত্যাগের স্মরণে পশু কোরবানি ও ত্যাগের মহোৎসব।"
        ),
        Holiday(
            id = 13,
            name = "Holy Ashura",
            nameBn = "পবিত্র আশুরা",
            date = createTimestamp(6, 6),
            type = "Islamic Holiday",
            calendarType = "Hijri",
            description = "১০ মহররম কারবালার শোকাবহ স্মৃতি ও ইসলামের ঐতিহাসিক দিন।"
        ),
        Holiday(
            id = 14,
            name = "National Mourning Day",
            nameBn = "জাতীয় শোক দিবস",
            date = createTimestamp(7, 15),
            type = "National Day",
            calendarType = "Gregorian",
            description = "১৫ আগস্ট ১৯৭৫ সালে নিহত সকল শহীদের প্রতি বিনম্র শ্রদ্ধা ও জাতীয় শোক পালন।"
        ),
        Holiday(
            id = 15,
            name = "Janmashtami",
            nameBn = "শুভ জন্মাষ্টমী",
            date = createTimestamp(7, 25),
            type = "Bangladesh Public Holiday",
            calendarType = "Gregorian",
            description = "সনাতন ধর্মাবলম্বীদের পরম পুরুষ ভগবান শ্রীকৃষ্ণের শুভ আবির্ভাব তিথি।"
        ),
        Holiday(
            id = 16,
            name = "Eid-e-Miladunnabi (PBUH)",
            nameBn = "পবিত্র ঈদে মিলাদুন্নবী (সা.)",
            date = createTimestamp(8, 15),
            type = "Islamic Holiday",
            calendarType = "Hijri",
            description = "মানবতার মুক্তির দূত বিশ্বনবী হযরত মুহাম্মদ (সা.)-এর শুভ জন্ম ও ওফাত দিবস।"
        ),
        Holiday(
            id = 17,
            name = "Durga Puja (Bijoya Dashami)",
            nameBn = "দূর্গাপূজা (বিজয়া দশমী)",
            date = createTimestamp(9, 21),
            type = "Bangladesh Public Holiday",
            calendarType = "Gregorian",
            description = "হিন্দু ধর্মাবলম্বীদের প্রধান ধর্মীয় উৎসব শারদীয় দুর্গোৎসবের শুভ বিজয়া দশমী।"
        ),
        Holiday(
            id = 18,
            name = "Victory Day",
            nameBn = "মহান বিজয় দিবস",
            date = createTimestamp(11, 16),
            type = "National Day",
            calendarType = "Gregorian",
            description = "১৯৭১ সালের ১৬ ডিসেম্বর পাকিস্তানি হানাদার বাহিনীকে পরাজিত করে বীর বাঙালির ঐতিহাসিক বিজয়।"
        ),
        Holiday(
            id = 19,
            name = "Christmas Day",
            nameBn = "যীশু খ্রিষ্টের জন্মদিন (বড়দিন)",
            date = createTimestamp(11, 25),
            type = "Bangladesh Public Holiday",
            calendarType = "Gregorian",
            description = "খ্রিষ্টান ধর্মাবলম্বীদের প্রধান ধর্মীয় উৎসব প্রভু যীশু খ্রিষ্টের শুভ জন্মদিন।"
        )
    )
}
