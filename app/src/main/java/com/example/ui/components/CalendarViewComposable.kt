package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.AppMotion
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Event
import com.example.data.model.Holiday
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppMicroTypography
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.ui.viewmodel.CalendarViewType
import com.example.util.CalendarConverters
import com.example.util.CalendarUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarViewComposable(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier,
    isEmbeddedInHome: Boolean = false,
    onSelectEvent: ((Event) -> Unit)? = null
) {
    val selectedCal by viewModel.selectedDate.collectAsState()
    val viewType by viewModel.calendarViewType.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allHolidays by viewModel.allHolidays.collectAsState()
    val showTraditionalDates by viewModel.showTraditionalDatesInGrid.collectAsState()
    val hijriAdjustment by viewModel.hijriDayAdjustment.collectAsState()

    var showDatePickerDialog by remember { mutableStateOf(false) }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayNameFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
            .testTag("calendar_view_card"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = AppElevation.low,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm)
        ) {
            // View Mode Selector Chips (Month, Week, Day, Year, Agenda)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_view_type_selector")
            ) {
                SegmentedButton(
                    selected = viewType == CalendarViewType.MONTH,
                    onClick = { viewModel.setCalendarViewType(CalendarViewType.MONTH) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 5)
                ) { Text("Month", style = MaterialTheme.typography.labelSmall) }

                SegmentedButton(
                    selected = viewType == CalendarViewType.WEEK,
                    onClick = { viewModel.setCalendarViewType(CalendarViewType.WEEK) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 5)
                ) { Text("Week", style = MaterialTheme.typography.labelSmall) }

                SegmentedButton(
                    selected = viewType == CalendarViewType.DAY,
                    onClick = { viewModel.setCalendarViewType(CalendarViewType.DAY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 5)
                ) { Text("Day", style = MaterialTheme.typography.labelSmall) }

                SegmentedButton(
                    selected = viewType == CalendarViewType.YEAR,
                    onClick = { viewModel.setCalendarViewType(CalendarViewType.YEAR) },
                    shape = SegmentedButtonDefaults.itemShape(index = 3, count = 5)
                ) { Text("Year", style = MaterialTheme.typography.labelSmall) }

                SegmentedButton(
                    selected = viewType == CalendarViewType.AGENDA,
                    onClick = { viewModel.setCalendarViewType(CalendarViewType.AGENDA) },
                    shape = SegmentedButtonDefaults.itemShape(index = 4, count = 5)
                ) { Text("Agenda", style = MaterialTheme.typography.labelSmall) }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            // Calendar Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.sm))
                        .clickable { showDatePickerDialog = true }
                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                        .testTag("month_year_header_title")
                ) {
                    Text(
                        text = if (viewType == CalendarViewType.DAY) dayNameFormat.format(selectedCal.time) else monthYearFormat.format(selectedCal.time),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Pick Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AppDimensions.iconMedium)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    // Today Button
                    FilledTonalButton(
                        onClick = { viewModel.setToday() },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("today_button"),
                        shape = RoundedCornerShape(AppRadius.sm),
                        contentPadding = PaddingValues(horizontal = AppSpacing.md)
                    ) {
                        Text("Today", style = MaterialTheme.typography.labelMedium)
                    }

                    IconButton(
                        onClick = { viewModel.navigateMonth(-1) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(AppDimensions.iconSmall),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateMonth(1) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(AppDimensions.iconSmall),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Prominent Bangla & Arabic Month Range Banner Pills for the currently viewed month
            if (showTraditionalDates) {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                val banglaMonthSpan = remember(selectedCal) { CalendarConverters.getBanglaMonthSpan(selectedCal) }
                val hijriMonthSpan = remember(selectedCal, hijriAdjustment) { CalendarConverters.getHijriMonthSpan(selectedCal, hijriAdjustment) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bangla Month Pill
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppRadius.full),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🌾", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = banglaMonthSpan,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1
                            )
                        }
                    }

                    // Hijri Month Pill
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppRadius.full),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🌙", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = hijriMonthSpan,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Swipe Drag gesture state
            var dragOffset by remember { mutableFloatStateOf(0f) }
            val dragGestureModifier = Modifier.draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    dragOffset += delta
                },
                onDragStopped = {
                    if (dragOffset > 100) {
                        viewModel.navigateMonth(-1)
                    } else if (dragOffset < -100) {
                        viewModel.navigateMonth(1)
                    }
                    dragOffset = 0f
                }
            )

            Box(modifier = dragGestureModifier) {
                when (viewType) {
                    CalendarViewType.MONTH -> MonthViewGrid(
                        selectedCal = selectedCal,
                        events = allEvents,
                        holidays = allHolidays,
                        showTraditionalDates = showTraditionalDates,
                        hijriAdjustment = hijriAdjustment,
                        onSelectDate = { viewModel.setSelectedDate(it) }
                    )
                    CalendarViewType.WEEK -> WeekViewGrid(
                        selectedCal = selectedCal,
                        events = allEvents,
                        holidays = allHolidays,
                        onSelectDate = { viewModel.setSelectedDate(it) }
                    )
                    CalendarViewType.DAY -> DayViewGrid(selectedCal, allEvents, onSelectEvent = onSelectEvent)
                    CalendarViewType.YEAR -> YearViewGrid(
                        selectedCal = selectedCal,
                        events = allEvents,
                        onSelectMonth = {
                            val newCal = selectedCal.clone() as Calendar
                            newCal.set(Calendar.MONTH, it)
                            viewModel.setSelectedDate(newCal)
                            viewModel.setCalendarViewType(CalendarViewType.MONTH)
                        }
                    )
                    CalendarViewType.AGENDA -> AgendaViewList(selectedCal, allEvents, onSelectEvent = onSelectEvent)
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedCal.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            shape = RoundedCornerShape(AppRadius.sheet),
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance().apply { timeInMillis = millis }
                            viewModel.setSelectedDate(cal)
                        }
                        showDatePickerDialog = false
                    },
                    shape = RoundedCornerShape(AppRadius.sm)
                ) { 
                    Text("OK", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)) 
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false },
                    shape = RoundedCornerShape(AppRadius.sm)
                ) { 
                    Text("Cancel", style = MaterialTheme.typography.labelLarge) 
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthViewGrid(
    selectedCal: Calendar,
    events: List<Event>,
    holidays: List<Holiday> = emptyList(),
    showTraditionalDates: Boolean = true,
    hijriAdjustment: Int = 0,
    onSelectDate: (Calendar) -> Unit
) {
    val weekDaysBnEn = listOf(
        Pair("রবি", "SUN"),
        Pair("সোম", "MON"),
        Pair("মঙ্গল", "TUE"),
        Pair("বুধ", "WED"),
        Pair("বৃহ", "THU"),
        Pair("শুক্র", "FRI"),
        Pair("শনি", "SAT")
    )

    val currentMonthCal = (selectedCal.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val daysInMonth = currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = currentMonthCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday

    val todayCal = Calendar.getInstance()
    var detailCalForDialog by remember { mutableStateOf<Calendar?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Weekday Header Row inside a sleek container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.xxs),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(AppRadius.md),
            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                weekDaysBnEn.forEachIndexed { index, (bn, en) ->
                    val isWeekend = index == 5 || index == 6 // Friday & Saturday
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = bn,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isWeekend) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = en,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isWeekend) MaterialTheme.colorScheme.error.copy(alpha = 0.75f) else MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Days Grid (Chunked by 7 into week rows with exact 7-column alignment)
        val cellsList = remember(selectedCal) {
            val list = mutableListOf<Calendar>()
            // Trailing days from previous month
            for (i in 0 until firstDayOfWeek) {
                val cal = (currentMonthCal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - i))
                }
                list.add(cal)
            }
            // Days in current month
            for (day in 1..daysInMonth) {
                val cal = (currentMonthCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, day)
                }
                list.add(cal)
            }
            // Leading days of next month to complete the 7-column rows
            var nextDay = 1
            while (list.size % 7 != 0) {
                val cal = (currentMonthCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, daysInMonth)
                    add(Calendar.DAY_OF_MONTH, nextDay)
                }
                list.add(cal)
                nextDay++
            }
            list
        }

        val weeks = remember(cellsList) { cellsList.chunked(7) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    week.forEachIndexed { dayIndex, dayCal ->
                        val isCurrentMonth = dayCal.get(Calendar.MONTH) == currentMonthCal.get(Calendar.MONTH)
                        val isWeekend = dayIndex == 5 || dayIndex == 6 // Friday & Saturday
                        val isToday = CalendarUtils.isSameDay(dayCal, todayCal)
                        val isSelected = CalendarUtils.isSameDay(dayCal, selectedCal)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Event & Holiday checks for this date
                            val startOfDay = (dayCal.clone() as Calendar).apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            val endOfDay = (dayCal.clone() as Calendar).apply {
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }.timeInMillis

                            val dayEvents = if (isCurrentMonth) events.filter { it.startDate in startOfDay..endOfDay } else emptyList()
                            val dayHolidays = if (isCurrentMonth) holidays.filter {
                                val hCal = Calendar.getInstance().apply { timeInMillis = it.date }
                                CalendarUtils.isSameDay(dayCal, hCal)
                            } else emptyList()
                            val hasHoliday = dayHolidays.isNotEmpty()
                            val hasEvent = dayEvents.isNotEmpty()

                            val targetBgColor = when {
                                !isCurrentMonth -> MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                                hasHoliday -> Color(0xFFFB8C00).copy(alpha = 0.09f)
                                isWeekend -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
                                else -> MaterialTheme.colorScheme.surfaceContainerLowest
                            }
                            val animatedBgColor by animateColorAsState(
                                targetValue = targetBgColor,
                                animationSpec = tween(AppMotion.durationFast),
                                label = "MonthCellBgColor"
                            )

                            val targetScale = if (isSelected) 1.02f else 1.0f
                            val animatedScale by animateFloatAsState(
                                targetValue = targetScale,
                                animationSpec = tween(AppMotion.durationFast),
                                label = "MonthCellScale"
                            )

                            val cellBorder = when {
                                !isCurrentMonth -> BorderStroke(0.3.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                                isSelected -> null
                                isToday -> BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
                                hasHoliday -> BorderStroke(0.6.dp, Color(0xFFFB8C00).copy(alpha = 0.35f))
                                else -> BorderStroke(0.4.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            }

                            // Traditional Calendar conversions
                            val banglaDate = if (showTraditionalDates) CalendarUtils.getBanglaDate(dayCal) else null
                            val hijriDate = if (showTraditionalDates) CalendarUtils.getHijriDate(dayCal, hijriAdjustment) else null

                            val cellShape = RoundedCornerShape(AppRadius.sm)

                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                    }
                                    .clip(cellShape)
                                    .combinedClickable(
                                        onClick = { onSelectDate(dayCal) },
                                        onLongClick = { if (isCurrentMonth) detailCalForDialog = dayCal }
                                    ),
                                shape = cellShape,
                                border = cellBorder,
                                color = animatedBgColor,
                                tonalElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 2.dp, vertical = 2.5.dp)
                                ) {
                                    // 1. Top Row: Gregorian Date + Holiday indicator icon
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isToday || isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            color = when {
                                                !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                                hasHoliday -> Color(0xFFE65100)
                                                isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            maxLines = 1
                                        )
                                        if (hasHoliday && isCurrentMonth) {
                                            Spacer(modifier = Modifier.width(1.dp))
                                            Text(
                                                text = "★",
                                                fontSize = 8.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color(0xFFFB8C00)
                                            )
                                        }
                                    }

                                    // 2. Micro Dual Sub-labels (Bangla & Hijri Dates)
                                    if (showTraditionalDates && banglaDate != null && hijriDate != null) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Bangla Day Number
                                            Text(
                                                text = CalendarUtils.toBanglaDigit(banglaDate.day),
                                                style = AppMicroTypography.microRegular.copy(
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = when {
                                                    !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                                    isToday -> MaterialTheme.colorScheme.secondary
                                                    else -> Color(0xFF2E7D32)
                                                },
                                                maxLines = 1
                                            )

                                            Text(
                                                text = "·",
                                                style = AppMicroTypography.microRegular.copy(fontSize = 7.sp),
                                                color = when {
                                                    !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                                },
                                                modifier = Modifier.padding(horizontal = 1.dp)
                                            )

                                            // Hijri Day Number
                                            Text(
                                                text = CalendarUtils.toBanglaDigit(hijriDate.day),
                                                style = AppMicroTypography.microMedium.copy(
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = when {
                                                    !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                                    isToday -> MaterialTheme.colorScheme.tertiary
                                                    else -> Color(0xFF00796B)
                                                },
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // 3. Multi Indicator Dots (Events & Holidays)
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.5.dp)
                                    ) {
                                        if (hasHoliday && isCurrentMonth) {
                                            Box(
                                                modifier = Modifier
                                                    .size(3.5.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else Color(0xFFFB8C00))
                                            )
                                            if (hasEvent) Spacer(modifier = Modifier.width(2.dp))
                                        }
                                        if (hasEvent && isCurrentMonth) {
                                            Box(
                                                modifier = Modifier
                                                    .size(3.5.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detailed Dialog for Selected Date
    detailCalForDialog?.let { cal ->
        val bnDate = CalendarUtils.getBanglaDate(cal)
        val hjDate = CalendarUtils.getHijriDate(cal, hijriAdjustment)
        val dayNameBn = CalendarUtils.getBanglaDayName(cal.get(Calendar.DAY_OF_WEEK))
        val dateEnFull = CalendarUtils.formatDate(cal.timeInMillis, "EEEE, d MMMM yyyy")

        val startOfDay = (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val dayEvents = events.filter { it.startDate in startOfDay..endOfDay }
        val dayHolidays = holidays.filter {
            val hCal = Calendar.getInstance().apply { timeInMillis = it.date }
            CalendarUtils.isSameDay(cal, hCal)
        }

        AlertDialog(
            onDismissRequest = { detailCalForDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📅", fontSize = 20.sp)
                    Text(
                        text = "তারিখের বিস্তারিত (Date Info)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    // English
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(AppSpacing.sm)) {
                            Text(text = "ইংরেজি (Gregorian):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(text = dateEnFull, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    // Bangla
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(AppSpacing.sm)) {
                            Text(text = "বাংলা সন (Bangla):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text(text = "$dayNameBn, ${bnDate.formattedBn}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    // Hijri
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(AppSpacing.sm)) {
                            Text(text = "হিজরি সন (Hijri):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            Text(text = "${hjDate.formattedEn} (${hjDate.formattedAr})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }

                    // Holidays on this day
                    if (dayHolidays.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFFB8C00).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(AppRadius.md),
                            border = BorderStroke(1.dp, Color(0xFFFB8C00).copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(AppSpacing.sm)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎉 ", fontSize = 14.sp)
                                    Text(
                                        text = "ছুটির দিন (Holiday):",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFE65100)
                                    )
                                }
                                dayHolidays.forEach { h ->
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = h.nameBn.ifBlank { h.name },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (h.description.isNotBlank()) {
                                        Text(
                                            text = h.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Events on this day
                    if (dayEvents.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(AppRadius.md),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(AppSpacing.sm)) {
                                Text(
                                    text = "ইভেন্ট (${dayEvents.size} টি):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                dayEvents.forEach { ev ->
                                    Text(
                                        text = "• ${ev.title}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { detailCalForDialog = null },
                    shape = RoundedCornerShape(AppRadius.sm)
                ) {
                    Text("ঠিক আছে (OK)")
                }
            }
        )
    }
}

@Composable
fun WeekViewGrid(
    selectedCal: Calendar,
    events: List<Event>,
    holidays: List<Holiday> = emptyList(),
    onSelectDate: (Calendar) -> Unit
) {
    val weekCal = (selectedCal.clone() as Calendar).apply {
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    }

    val weekDaysList = remember(selectedCal) {
        (0..6).map { offset ->
            (weekCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
        }
    }

    val todayCal = Calendar.getInstance()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        weekDaysList.forEach { dayCal ->
            val isSelected = CalendarUtils.isSameDay(dayCal, selectedCal)
            val isToday = CalendarUtils.isSameDay(dayCal, todayCal)

            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(dayCal.time)
            val isWeekend = dayName.equals("Fri", ignoreCase = true) || dayName.equals("Sat", ignoreCase = true)

            val startOfDay = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            val endOfDay = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis

            val hasEvents = events.any { it.startDate in startOfDay..endOfDay }
            val hasHoliday = holidays.any {
                val hCal = Calendar.getInstance().apply { timeInMillis = it.date }
                CalendarUtils.isSameDay(dayCal, hCal)
            }

            val targetContainerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.primaryContainer
                hasHoliday -> Color(0xFFFB8C00).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
            val animatedContainerColor by animateColorAsState(
                targetValue = targetContainerColor,
                animationSpec = tween(AppMotion.durationFast),
                label = "WeekCellBgColor"
            )

            val targetScale = if (isSelected) 1.04f else 1.0f
            val animatedScale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = tween(AppMotion.durationFast),
                label = "WeekCellScale"
            )

            val labelColor = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                isWeekend -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            val numColor = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                hasHoliday -> Color(0xFFE65100)
                isWeekend -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }

            val border = when {
                isSelected -> null
                isToday -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                hasHoliday -> BorderStroke(1.dp, Color(0xFFFB8C00).copy(alpha = 0.4f))
                else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .clip(RoundedCornerShape(AppRadius.md))
                    .pressFeedback(pressedScale = 0.94f)
                    .clickable { onSelectDate(dayCal) },
                shape = RoundedCornerShape(AppRadius.md),
                border = border,
                color = animatedContainerColor,
                tonalElevation = if (isSelected) AppElevation.low else AppElevation.none
            ) {
                Column(
                    modifier = Modifier.padding(vertical = AppSpacing.md, horizontal = AppSpacing.xxs),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = labelColor
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = numColor
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasHoliday) {
                            Box(
                                modifier = Modifier
                                    .size(AppSpacing.xs)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else Color(0xFFFB8C00))
                            )
                            if (hasEvents) Spacer(modifier = Modifier.width(2.dp))
                        }
                        if (hasEvents) {
                            Box(
                                modifier = Modifier
                                    .size(AppSpacing.xs)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.primary
                                    )
                            )
                        }
                        if (!hasEvents && !hasHoliday) {
                            Spacer(modifier = Modifier.size(AppSpacing.xs))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayViewGrid(
    selectedCal: Calendar,
    events: List<Event>,
    onSelectEvent: ((Event) -> Unit)? = null
) {
    val startOfDay = (selectedCal.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    val endOfDay = (selectedCal.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.timeInMillis

    val dayEvents = events.filter { it.startDate in startOfDay..endOfDay }
    val dayFormatted = remember(selectedCal) {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(selectedCal.time)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs)
    ) {
        // Day Info Summary Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayFormatted,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(AppRadius.full),
                color = if (dayEvents.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = if (dayEvents.isNotEmpty()) "${dayEvents.size} Events" else "0 Events",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (dayEvents.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs)
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        if (dayEvents.isEmpty()) {
            AppEmptyState(
                icon = Icons.Outlined.EventBusy,
                title = "No events scheduled",
                subtitle = "There are no events for this selected day.",
                modifier = Modifier.padding(vertical = AppSpacing.sm)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                dayEvents.forEach { event ->
                    val accentColor = remember(event.colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(event.colorHex))
                        } catch (_: Exception) {
                            null
                        }
                    } ?: MaterialTheme.colorScheme.primary

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppRadius.md))
                            .then(if (onSelectEvent != null) Modifier.clickable { onSelectEvent(event) } else Modifier),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        tonalElevation = AppElevation.low,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(AppRadius.xs))
                                    .background(accentColor)
                            )

                            Spacer(modifier = Modifier.width(AppSpacing.md))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(AppSpacing.xxs))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(AppDimensions.iconSmall)
                                    )
                                    val timeString = if (event.isAllDay) {
                                        "All Day"
                                    } else {
                                        "${CalendarUtils.formatTime(event.startDate)} - ${CalendarUtils.formatTime(event.endDate)}"
                                    }
                                    Text(
                                        text = timeString,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (event.location.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Place,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(AppDimensions.iconSmall)
                                        )
                                        Text(
                                            text = event.location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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
fun YearViewGrid(
    selectedCal: Calendar,
    events: List<Event> = emptyList(),
    onSelectMonth: (Int) -> Unit
) {
    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthShortNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val currentMonth = selectedCal.get(Calendar.MONTH)
    val selectedYear = selectedCal.get(Calendar.YEAR)
    val thisMonth = Calendar.getInstance().get(Calendar.MONTH)
    val thisYear = Calendar.getInstance().get(Calendar.YEAR)

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            items(12) { monthIdx ->
                val isSelected = monthIdx == currentMonth
                val isCurrentActualMonth = (monthIdx == thisMonth && selectedYear == thisYear)

                // Calculate event count for this month
                val monthEventCount = events.count { event ->
                    val eventCal = Calendar.getInstance().apply { timeInMillis = event.startDate }
                    eventCal.get(Calendar.MONTH) == monthIdx && eventCal.get(Calendar.YEAR) == selectedYear
                }

                val containerColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isCurrentActualMonth -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                }

                val contentColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isCurrentActualMonth -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }

                val border = when {
                    isSelected -> null
                    isCurrentActualMonth -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.md))
                        .clickable { onSelectMonth(monthIdx) },
                    shape = RoundedCornerShape(AppRadius.md),
                    border = border,
                    color = containerColor,
                    tonalElevation = if (isSelected) AppElevation.low else AppElevation.none
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.md, horizontal = AppSpacing.sm),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = monthShortNames[monthIdx],
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isSelected || isCurrentActualMonth) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = contentColor
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.xxs))

                        if (monthEventCount > 0) {
                            Text(
                                text = "$monthEventCount ${if (monthEventCount == 1) "event" else "events"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                else if (isCurrentActualMonth) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgendaViewList(
    selectedCal: Calendar,
    events: List<Event>,
    onSelectEvent: ((Event) -> Unit)? = null
) {
    val startOfToday = (selectedCal.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    val upcoming = events.filter { it.startDate >= startOfToday }.sortedBy { it.startDate }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs)
    ) {
        // Agenda Header with Count Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Agenda",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(AppRadius.full),
                color = if (upcoming.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = if (upcoming.isNotEmpty()) "${upcoming.size} Items" else "0 Items",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (upcoming.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs)
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.sm))

        if (upcoming.isEmpty()) {
            AppEmptyState(
                icon = Icons.Outlined.EventBusy,
                title = "No upcoming agenda items",
                subtitle = "No upcoming events scheduled in the near future.",
                modifier = Modifier.padding(vertical = AppSpacing.sm)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                upcoming.take(8).forEach { event ->
                    val eventCal = Calendar.getInstance().apply { timeInMillis = event.startDate }
                    val dayNum = eventCal.get(Calendar.DAY_OF_MONTH).toString()
                    val monthShort = SimpleDateFormat("MMM", Locale.getDefault()).format(eventCal.time)

                    val accentColor = remember(event.colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(event.colorHex))
                        } catch (_: Exception) {
                            null
                        }
                    } ?: MaterialTheme.colorScheme.primary

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppRadius.md))
                            .then(if (onSelectEvent != null) Modifier.clickable { onSelectEvent(event) } else Modifier),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        tonalElevation = AppElevation.low,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date Badge
                            Surface(
                                shape = RoundedCornerShape(AppRadius.sm),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.size(width = 44.dp, height = 48.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = monthShort.uppercase(Locale.getDefault()),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = dayNum,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(AppSpacing.md))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(AppSpacing.xxs))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(AppDimensions.iconSmall)
                                    )
                                    val timeStr = if (event.isAllDay) {
                                        "All Day"
                                    } else {
                                        CalendarUtils.formatTime(event.startDate)
                                    }
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (event.location.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Place,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(AppDimensions.iconSmall)
                                        )
                                        Text(
                                            text = event.location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Event Color Strip Indicator
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(AppRadius.xs))
                                    .background(accentColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
