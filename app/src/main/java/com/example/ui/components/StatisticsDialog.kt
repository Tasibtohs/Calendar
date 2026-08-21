package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allEvents by viewModel.allEvents.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val allBirthdays by viewModel.allBirthdays.collectAsState()
    val allAnniversaries by viewModel.allAnniversaries.collectAsState()
    val allHolidays by viewModel.allHolidays.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    val selectedCal by viewModel.selectedDate.collectAsState()
    var selectedPeriod by remember { mutableStateOf("This Month") } // "This Month", "This Year", "All Time"

    // Time ranges based on selected period
    val (periodEvents, periodTasks, periodLabel) = remember(selectedCal, selectedPeriod, allEvents, allTasks) {
        when (selectedPeriod) {
            "This Month" -> {
                val startOfMonth = (selectedCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                }.timeInMillis

                val endOfMonth = (selectedCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                }.timeInMillis

                val events = allEvents.filter { !it.isArchived && it.startDate in startOfMonth..endOfMonth }
                val tasks = allTasks.filter { !it.isArchived }
                val label = CalendarUtils.formatDate(selectedCal.timeInMillis, "MMMM yyyy")
                Triple(events, tasks, label)
            }
            "This Year" -> {
                val year = selectedCal.get(Calendar.YEAR)
                val startOfYear = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                }.timeInMillis
                val endOfYear = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, Calendar.DECEMBER)
                    set(Calendar.DAY_OF_MONTH, 31)
                    set(Calendar.HOUR_OF_DAY, 23)
                }.timeInMillis

                val events = allEvents.filter { !it.isArchived && it.startDate in startOfYear..endOfYear }
                val tasks = allTasks.filter { !it.isArchived }
                Triple(events, tasks, "বছর $year")
            }
            else -> {
                val events = allEvents.filter { !it.isArchived }
                val tasks = allTasks.filter { !it.isArchived }
                Triple(events, tasks, "সর্বকালের সার্বিক হিসাব")
            }
        }
    }

    val completedTasksCount = periodTasks.count { it.isCompleted }
    val pendingTasksCount = periodTasks.count { !it.isCompleted && it.status != "Overdue" }
    val overdueTasksCount = periodTasks.count { it.status == "Overdue" }
    val highPriorityTasksCount = periodTasks.count { it.priority.equals("High", ignoreCase = true) || it.priority.equals("উচ্চ", ignoreCase = true) }

    val totalTaskCount = completedTasksCount + pendingTasksCount + overdueTasksCount
    val completionRate = if (totalTaskCount > 0) ((completedTasksCount.toFloat() / totalTaskCount) * 100).toInt() else 0

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("statistics_screen"),
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
                                text = "পরিসংখ্যান ও অ্যানালিটিক্স",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$periodLabel • কাজের অগ্রগতি ও টাইম ট্র্যাকিং",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                val reportText = buildString {
                                    appendLine("📊 ক্যালেন্ডার ও উৎপাদনশীলতা রিপোর্ট ($periodLabel)")
                                    appendLine("----------------------------------------")
                                    appendLine("• মোট ইভেন্ট: ${periodEvents.size}")
                                    appendLine("• সম্পন্ন টাস্ক: $completedTasksCount ($completionRate%)")
                                    appendLine("• বাকি কাজ: $pendingTasksCount")
                                    appendLine("• মেয়াদোত্তীর্ণ কাজ: $overdueTasksCount")
                                    appendLine("• নোট সংখ্যা: ${allNotes.size}")
                                    appendLine("• জন্মদিন ও বার্ষিকী: ${allBirthdays.size + allAnniversaries.size}")
                                    appendLine("• ছুটির দিন: ${allHolidays.size}")
                                }
                                val clip = ClipData.newPlainText("Stats Report", reportText)
                                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                Toast.makeText(context, "রিপোর্ট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share Report",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Period Selector Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        listOf(
                            "This Month" to "এই মাস (Month)",
                            "This Year" to "এই বছর (Year)",
                            "All Time" to "সর্বকালের (All)"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = selectedPeriod == key,
                                onClick = { selectedPeriod = key },
                                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            // Main Completion Hero Banner
            Surface(
                shape = RoundedCornerShape(AppRadius.lg),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "উৎপাদনশীলতার হার (Productivity)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$completionRate% সম্পন্ন",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xxs))
                        Text(
                            text = if (completionRate >= 80) "দুর্দান্ত অগ্রগতি! আপনি চমৎকার কাজ করছেন 🚀"
                            else if (completionRate >= 50) "ভালো অগ্রগতি, বাকি কাজগুলো দ্রুত শেষ করুন 💪"
                            else "বাকি থাকা কাজগুলোতে আরও মনোযোগ দেওয়া প্রয়োজন 🎯",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { completionRate / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 8.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "$completionRate%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 6 Grid Stat Cards
            Text(
                text = "প্রধান মেট্রিক্স সমূহ (Overview)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                MetricStatCard(
                    title = "ইভেন্ট (Events)",
                    value = "${periodEvents.size}",
                    icon = Icons.Default.Event,
                    color = Color(0xFF3F51B5),
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "সম্পন্ন (Done)",
                    value = "$completedTasksCount",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "চলমান (Pending)",
                    value = "$pendingTasksCount",
                    icon = Icons.Default.PendingActions,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                MetricStatCard(
                    title = "জরুরি কাজ",
                    value = "$highPriorityTasksCount",
                    icon = Icons.Default.PriorityHigh,
                    color = Color(0xFFE91E63),
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "নোটবুক (Notes)",
                    value = "${allNotes.size}",
                    icon = Icons.Default.StickyNote2,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "ছুটির তালিকা",
                    value = "${allHolidays.size}",
                    icon = Icons.Default.Celebration,
                    color = Color(0xFF00BCD4),
                    modifier = Modifier.weight(1f)
                )
            }

            // Task Donut Distribution Card
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
                        text = "টাস্ক স্থিতি বন্টন (Task Distribution)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TaskDonutChart(
                        completed = completedTasksCount,
                        pending = pendingTasksCount,
                        overdue = overdueTasksCount
                    )
                }
            }

            // Category Bar Chart Card
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
                        text = "ক্যাটাগরি ভিত্তিক ইভেন্ট হিসাব (Category Breakdown)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CategoryBarChart(
                        events = periodEvents,
                        categories = allCategories
                    )
                }
            }

            // Birthdays & Anniversaries Summary
            Surface(
                shape = RoundedCornerShape(AppRadius.lg),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Text(
                        text = "স্মরণীয় দিন ও উৎসব (Special Occasions)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(modifier = Modifier.padding(AppSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(AppSpacing.sm))
                                Column {
                                    Text("মোট জন্মদিন", style = MaterialTheme.typography.labelSmall)
                                    Text("${allBirthdays.size} টি সংরক্ষিত", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(modifier = Modifier.padding(AppSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(AppSpacing.sm))
                                Column {
                                    Text("বিবাহবার্ষিকী", style = MaterialTheme.typography.labelSmall)
                                    Text("${allAnniversaries.size} টি সংরক্ষিত", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppRadius.md),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .padding(AppSpacing.sm)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(AppSpacing.xxs))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TaskDonutChart(
    completed: Int,
    pending: Int,
    overdue: Int
) {
    val total = completed + pending + overdue
    if (total == 0) {
        Text("কোনো Task হিসাব করা যায়নি", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        return
    }

    val completedAngle = (completed.toFloat() / total) * 360f
    val pendingAngle = (pending.toFloat() / total) * 360f
    val overdueAngle = (overdue.toFloat() / total) * 360f

    val completedColor = Color(0xFF4CAF50)
    val pendingColor = Color(0xFFFF9800)
    val overdueColor = Color(0xFFF44336)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 20.dp.toPx()
                var startAngle = -90f

                if (completedAngle > 0f) {
                    drawArc(
                        color = completedColor,
                        startAngle = startAngle,
                        sweepAngle = completedAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += completedAngle
                }

                if (pendingAngle > 0f) {
                    drawArc(
                        color = pendingColor,
                        startAngle = startAngle,
                        sweepAngle = pendingAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += pendingAngle
                }

                if (overdueAngle > 0f) {
                    drawArc(
                        color = overdueColor,
                        startAngle = startAngle,
                        sweepAngle = overdueAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${((completed.toFloat() / total) * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("Done", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            LegendItem(color = completedColor, label = "Completed: $completed (${((completed.toFloat()/total)*100).toInt()}%)")
            LegendItem(color = pendingColor, label = "Pending: $pending (${((pending.toFloat()/total)*100).toInt()}%)")
            LegendItem(color = overdueColor, label = "Overdue: $overdue (${((overdue.toFloat()/total)*100).toInt()}%)")
        }
    }
}

@Composable
private fun CategoryBarChart(
    events: List<com.example.data.model.Event>,
    categories: List<com.example.data.model.Category>
) {
    val categoryCounts = categories.map { cat ->
        val count = events.count { it.categoryId == cat.id }
        cat to count
    }.filter { it.second > 0 }

    if (categoryCounts.isEmpty()) {
        Text("চলতি সময়ে কোনো ক্যাটাগরি ইভেন্ট পাওয়া যায়নি", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        return
    }

    val maxCount = categoryCounts.maxOf { it.second }

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        categoryCounts.forEach { (cat, count) ->
            val catColor = try {
                Color(android.graphics.Color.parseColor(cat.colorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cat.name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(90.dp),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(AppSpacing.xs))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val progressFraction = count.toFloat() / maxCount.toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .clip(RoundedCornerShape(9.dp))
                            .background(catColor)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.sm))

                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

// Backwards compatibility alias for Dialog
@Composable
fun StatisticsDialog(
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        StatisticsScreen(
            viewModel = viewModel,
            onBack = onDismiss
        )
    }
}
