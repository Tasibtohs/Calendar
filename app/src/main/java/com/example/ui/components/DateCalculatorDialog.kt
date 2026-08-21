package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.util.CalendarUtils
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCalculatorScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Diff, 1: Add/Sub, 2: Age, 3: Bangla/Hijri

    val tabs = listOf(
        "ব্যবধান (Difference)",
        "যোগ/বিয়োগ (+ / -)",
        "বয়স গণনা (Age)",
        "বাংলা ও হিজরি (Converter)"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("date_calculator_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(AppSpacing.xs))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "তারিখ হিসাব ও ক্যালকুলেটর",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ব্যবধান, যোগ-বিয়োগ, নিখুঁত বয়স ও ক্যালেন্ডার রূপান্তর",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = AppSpacing.xs)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Date Tools",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Horizontal Tabs Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        tabs.forEachIndexed { index, label ->
                            FilterChip(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                },
                                leadingIcon = {
                                    val icon = when (index) {
                                        0 -> Icons.Outlined.CompareArrows
                                        1 -> Icons.Outlined.Exposure
                                        2 -> Icons.Outlined.Cake
                                        else -> Icons.Outlined.CalendarMonth
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedTab == index,
                                    borderColor = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            when (selectedTab) {
                0 -> DateDifferenceTabContent(context)
                1 -> DateAddSubtractTabContent(context)
                2 -> AgeCalculatorTabContent(context)
                3 -> CalendarConverterTabContent(context)
            }
        }
    }
}

@Composable
private fun DateDifferenceTabContent(context: Context) {
    var date1 by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }
    var date2 by remember {
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.timeInMillis)
    }

    var showPicker1 by remember { mutableStateOf(false) }
    var showPicker2 by remember { mutableStateOf(false) }

    val diffMs = kotlin.math.abs(date2 - date1)
    val totalDays = TimeUnit.MILLISECONDS.toDays(diffMs)
    val totalWeeks = totalDays / 7
    val remDays = totalDays % 7
    val totalMonths = (totalDays / 30.4375).toInt()
    val totalHours = totalDays * 24

    // Working days (excluding Fridays and Saturdays)
    val workingDays = remember(date1, date2) {
        val start = minOf(date1, date2)
        val end = maxOf(date1, date2)
        val c = Calendar.getInstance().apply { timeInMillis = start }
        val endCal = Calendar.getInstance().apply { timeInMillis = end }
        var count = 0
        while (!c.after(endCal)) {
            val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.FRIDAY && dayOfWeek != Calendar.SATURDAY) {
                count++
            }
            c.add(Calendar.DAY_OF_YEAR, 1)
        }
        count
    }

    Surface(
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(
                text = "তারিখ নির্বাচন করুন (Select Dates)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Start Date Picker Card
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPicker1 = true },
                shape = RoundedCornerShape(AppRadius.md),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Column {
                            Text("প্রথম তারিখ (Start Date)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CalendarUtils.formatDate(date1, "EEEE, d MMMM yyyy"), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                    TextButton(onClick = { showPicker1 = true }) { Text("পরিবর্তন") }
                }
            }

            // End Date Picker Card
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPicker2 = true },
                shape = RoundedCornerShape(AppRadius.md),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EventAvailable, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Column {
                            Text("দ্বিতীয় তারিখ (End Date)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CalendarUtils.formatDate(date2, "EEEE, d MMMM yyyy"), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                    TextButton(onClick = { showPicker2 = true }) { Text("পরিবর্তন") }
                }
            }
        }
    }

    // Result Card
    Surface(
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "মোট ব্যবধানের ফলাফল",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                IconButton(
                    onClick = {
                        val text = "তারিখ ব্যবধান: $totalDays দিন ($totalWeeks সপ্তাহ $remDays দিন), কর্মদিবস: $workingDays দিন"
                        val clip = ClipData.newPlainText("Date Diff", text)
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                        Toast.makeText(context, "ফলাফল কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Text(
                text = "$totalDays দিন",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "অথবা $totalWeeks সপ্তাহ $remDays দিন (প্রায় $totalMonths মাস / $totalHours ঘণ্টা)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("আনুমানিক কর্মদিবস (শুক্র-শনি বাদে):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("$workingDays দিন", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showPicker1) {
        DatePickerModal(
            initialSelectedDateMillis = date1,
            onDateSelected = { date1 = it; showPicker1 = false },
            onDismiss = { showPicker1 = false }
        )
    }

    if (showPicker2) {
        DatePickerModal(
            initialSelectedDateMillis = date2,
            onDateSelected = { date2 = it; showPicker2 = false },
            onDismiss = { showPicker2 = false }
        )
    }
}

@Composable
private fun DateAddSubtractTabContent(context: Context) {
    var baseDate by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }
    var daysCountText by remember { mutableStateOf("15") }
    var isAddition by remember { mutableStateOf(true) }
    var unitType by remember { mutableStateOf("Days") } // Days, Weeks, Months, Years
    var showPicker by remember { mutableStateOf(false) }

    val amount = daysCountText.toIntOrNull() ?: 0
    val resultCal = Calendar.getInstance().apply {
        timeInMillis = baseDate
        val mult = if (isAddition) 1 else -1
        when (unitType) {
            "Days" -> add(Calendar.DAY_OF_YEAR, amount * mult)
            "Weeks" -> add(Calendar.WEEK_OF_YEAR, amount * mult)
            "Months" -> add(Calendar.MONTH, amount * mult)
            "Years" -> add(Calendar.YEAR, amount * mult)
        }
    }

    Surface(
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(
                text = "তারিখ ও যোগ-বিয়োগ পরিমাণ",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Base Date Picker Card
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPicker = true },
                shape = RoundedCornerShape(AppRadius.md),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Column {
                            Text("মূল তারিখ (Base Date)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CalendarUtils.formatDate(baseDate, "EEEE, d MMMM yyyy"), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                    TextButton(onClick = { showPicker = true }) { Text("পরিবর্তন") }
                }
            }

            // Operation Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                FilterChip(
                    selected = isAddition,
                    onClick = { isAddition = true },
                    label = { Text("দিন যোগ (+) Add") },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !isAddition,
                    onClick = { isAddition = false },
                    label = { Text("দিন বিয়োগ (-) Sub") },
                    leadingIcon = { Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Units Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Days" to "দিন", "Weeks" to "সপ্তাহ", "Months" to "মাস", "Years" to "বছর").forEach { (type, bn) ->
                    FilterChip(
                        selected = unitType == type,
                        onClick = { unitType = type },
                        label = { Text(bn, fontSize = 11.sp) }
                    )
                }
            }

            OutlinedTextField(
                value = daysCountText,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) daysCountText = it },
                label = { Text("পরিমাণ ($unitType)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(AppRadius.md)
            )
        }
    }

    // Result Card
    Surface(
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "গণনাকৃত ফলাফল তারিখ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                IconButton(
                    onClick = {
                        val text = CalendarUtils.formatDate(resultCal.timeInMillis, "EEEE, d MMMM yyyy")
                        val clip = ClipData.newPlainText("Calculated Date", text)
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                        Toast.makeText(context, "তারিখ কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.secondary)
                }
            }

            Text(
                text = CalendarUtils.formatDate(resultCal.timeInMillis, "EEEE, d MMMM yyyy"),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            val bangla = CalendarUtils.getBanglaDate(resultCal).formattedBn
            val hijri = CalendarUtils.getHijriDate(resultCal, 0).formattedEn

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "বাংলা: $bangla | আরবি: $hijri",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }

    if (showPicker) {
        DatePickerModal(
            initialSelectedDateMillis = baseDate,
            onDateSelected = { baseDate = it; showPicker = false },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun AgeCalculatorTabContent(context: Context) {
    var birthDate by remember {
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.YEAR, -24) }.timeInMillis)
    }
    var showPicker by remember { mutableStateOf(false) }

    val dob = Calendar.getInstance().apply { timeInMillis = birthDate }
    val today = Calendar.getInstance()

    var years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    var months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
    var days = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH)

    if (days < 0) {
        months -= 1
        val prevMonth = (today.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    if (months < 0) {
        years -= 1
        months += 12
    }

    val totalDaysLived = TimeUnit.MILLISECONDS.toDays(today.timeInMillis - dob.timeInMillis)
    val totalHoursLived = totalDaysLived * 24

    val dayOfWeekBorn = remember(birthDate) {
        CalendarUtils.formatDate(birthDate, "EEEE")
    }

    // Next Birthday Calculation
    val nextBday = Calendar.getInstance().apply {
        set(Calendar.MONTH, dob.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, dob.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        if (before(today)) {
            add(Calendar.YEAR, 1)
        }
    }
    val daysToNextBday = TimeUnit.MILLISECONDS.toDays(nextBday.timeInMillis - today.timeInMillis)

    Surface(
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(
                text = "জন্ম তারিখ নির্বাচন (Date of Birth)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPicker = true },
                shape = RoundedCornerShape(AppRadius.md),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Column {
                            Text("জন্ম তারিখ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CalendarUtils.formatDate(birthDate, "d MMMM yyyy"), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                    TextButton(onClick = { showPicker = true }) { Text("পরিবর্তন") }
                }
            }
        }
    }

    // Main Age Card
    Surface(
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "বর্তমান সঠিক বয়স",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Text(
                text = "$years বছর $months মাস $days দিন",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("জন্মের বার:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text(dayOfWeekBorn, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onTertiaryContainer)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("মোট জীবন অতিবাহিত:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("$totalDaysLived দিন ($totalHoursLived ঘণ্টা)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onTertiaryContainer)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("পরবর্তী জন্মদিন:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text("$daysToNextBday দিন বাকি 🎂", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }

    if (showPicker) {
        DatePickerModal(
            initialSelectedDateMillis = birthDate,
            onDateSelected = { birthDate = it; showPicker = false },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun CalendarConverterTabContent(context: Context) {
    var selectedDateMillis by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }
    var showPicker by remember { mutableStateOf(false) }

    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val englishFormatted = CalendarUtils.formatDate(selectedDateMillis, "EEEE, d MMMM yyyy")
    val banglaData = CalendarUtils.getBanglaDate(cal)
    val hijriData = CalendarUtils.getHijriDate(cal, 0)

    Surface(
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(
                text = "ক্যালেন্ডার তারিখ কনভার্টার",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPicker = true },
                shape = RoundedCornerShape(AppRadius.md),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("নির্বাচিত তারিখ (Gregorian)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(englishFormatted, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                    TextButton(onClick = { showPicker = true }) { Text("তারিখ বাছুন") }
                }
            }
        }
    }

    // 3 Calendar Cards
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        // English Card
        Surface(
            shape = RoundedCornerShape(AppRadius.md),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(AppSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column {
                    Text("ইংরেজি (Gregorian Calendar)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(englishFormatted, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Bangla Card
        Surface(
            shape = RoundedCornerShape(AppRadius.md),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(AppSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column {
                    Text("বাংলা বঙ্গাব্দ (Bangla Calendar)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(banglaData.formattedBn, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Text("${banglaData.monthNameBn} মাস • সন ${banglaData.year}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // Hijri Card
        Surface(
            shape = RoundedCornerShape(AppRadius.md),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(AppSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column {
                    Text("হিজরী সন (Islamic Hijri Calendar)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(hijriData.formattedEn, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }

    if (showPicker) {
        DatePickerModal(
            initialSelectedDateMillis = selectedDateMillis,
            onDateSelected = { selectedDateMillis = it; showPicker = false },
            onDismiss = { showPicker = false }
        )
    }
}

// Backwards compatibility alias for Dialog
@Composable
fun DateCalculatorDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DateCalculatorScreen(onBack = onDismiss)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    initialSelectedDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
