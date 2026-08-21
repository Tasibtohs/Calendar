package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Event
import com.example.data.model.Holiday
import com.example.ui.components.*
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.ui.viewmodel.CalendarViewType
import com.example.util.CalendarConverters
import com.example.util.CalendarUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    onOpenSearch: (() -> Unit)? = null
) {
    val selectedCal by viewModel.selectedDate.collectAsState()
    val viewType by viewModel.calendarViewType.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allHolidays by viewModel.allHolidays.collectAsState()
    val todaysEvents by viewModel.todaysEvents.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val hijriAdjustment by viewModel.hijriDayAdjustment.collectAsState()

    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedEventForDetail by remember { mutableStateOf<Event?>(null) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showThreeDotMenu by remember { mutableStateOf(false) }
    var showHolidaysDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val lastDeletedEntity by viewModel.lastDeletedEntity.collectAsState()

    LaunchedEffect(lastDeletedEntity) {
        if (lastDeletedEntity is CalendarViewModel.DeletedEntity.DeletedEvent) {
            val result = snackbarHostState.showSnackbar(
                message = "ইভেন্ট মুছে ফেলা হয়েছে (Event Deleted)",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoLastDelete()
            } else {
                viewModel.clearLastDeleted()
            }
        }
    }

    // Bangla formatted Month & Year (e.g. "আগস্ট ২০২৬")
    val monthYearTitleBn = remember(selectedCal) {
        CalendarUtils.getBanglaMonthYear(selectedCal)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_top_bar"),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    // TOP ROW: Hamburger Menu | "আগস্ট ২০২৬" + Dropdown Arrow | Search + Three-dot Menu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Hamburger Menu Icon
                        IconButton(
                            onClick = { onOpenDrawer?.invoke() },
                            modifier = Modifier
                                .size(AppDimensions.minTouchTarget)
                                .testTag("btn_calendar_hamburger_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Drawer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Center: Month Year Title + Dropdown Arrow (e.g. "আগস্ট ২০২৬ ▾")
                        Surface(
                            shape = RoundedCornerShape(AppRadius.sm),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clickable { showDatePickerDialog = true }
                                .testTag("btn_month_dropdown_picker")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = monthYearTitleBn,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Month & Year",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Right: Search Icon + Three-Dot Menu
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = { onOpenSearch?.invoke() },
                                modifier = Modifier
                                    .size(AppDimensions.minTouchTarget)
                                    .testTag("btn_calendar_search")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Events",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { showThreeDotMenu = true },
                                    modifier = Modifier
                                        .size(AppDimensions.minTouchTarget)
                                        .testTag("btn_calendar_three_dot_menu")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                DropdownMenu(
                                    expanded = showThreeDotMenu,
                                    onDismissRequest = { showThreeDotMenu = false },
                                    shape = RoundedCornerShape(AppRadius.md)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("আজকের দিনে যান (Today)", fontWeight = FontWeight.Medium) },
                                        leadingIcon = { Icon(Icons.Outlined.Today, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            viewModel.setToday()
                                            showThreeDotMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("ছুটির তালিকা (Holidays)", fontWeight = FontWeight.Medium) },
                                        leadingIcon = { Icon(Icons.Outlined.Celebration, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                                        onClick = {
                                            showHolidaysDialog = true
                                            showThreeDotMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("তারিখ নির্বাচন (Pick Date)", fontWeight = FontWeight.Medium) },
                                        leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            showDatePickerDialog = true
                                            showThreeDotMenu = false
                                        }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    DropdownMenuItem(
                                        text = { Text("পূর্ববর্তী মাস (Prev Month)") },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            viewModel.navigateMonth(-1)
                                            showThreeDotMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("পরবর্তী মাস (Next Month)") },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            viewModel.navigateMonth(1)
                                            showThreeDotMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 4 TABS: Month, Week, Day, Agenda (Month selected with Primary color background)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.sm, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            CalendarViewType.MONTH to "Month",
                            CalendarViewType.WEEK to "Week",
                            CalendarViewType.DAY to "Day",
                            CalendarViewType.AGENDA to "Agenda"
                        )

                        tabs.forEach { (type, label) ->
                            val isSelected = viewType == type
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(AppRadius.full))
                                    .clickable { viewModel.setCalendarViewType(type) }
                                    .testTag("tab_calendar_${label.lowercase()}"),
                                shape = RoundedCornerShape(AppRadius.full),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                ),
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(AppRadius.full),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Event") },
                text = { Text("+ Quick Add", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                modifier = Modifier.testTag("calendar_fab_quick_add")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            when (viewType) {
                CalendarViewType.MONTH -> {
                    item {
                        FullMonthCalendarCard(
                            selectedCal = selectedCal,
                            events = allEvents,
                            holidays = allHolidays,
                            hijriAdjustment = hijriAdjustment,
                            onSelectDate = { viewModel.setSelectedDate(it) },
                            onNavigateMonth = { viewModel.navigateMonth(it) }
                        )
                    }

                    item {
                        SelectedDateSummaryCard(
                            selectedCal = selectedCal,
                            events = allEvents,
                            holidays = allHolidays,
                            hijriAdjustment = hijriAdjustment,
                            onAddEvent = { showAddEventDialog = true },
                            onSelectEvent = { selectedEventForDetail = it }
                        )
                    }
                }
                CalendarViewType.WEEK -> {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppRadius.lg),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(AppSpacing.sm)) {
                                WeekViewGrid(
                                    selectedCal = selectedCal,
                                    events = allEvents,
                                    holidays = allHolidays,
                                    onSelectDate = { viewModel.setSelectedDate(it) }
                                )
                            }
                        }
                    }
                }
                CalendarViewType.DAY -> {
                    item {
                        DayViewGrid(
                            selectedCal = selectedCal,
                            events = allEvents,
                            onSelectEvent = { selectedEventForDetail = it }
                        )
                    }
                }
                CalendarViewType.AGENDA -> {
                    item {
                        AgendaViewList(
                            selectedCal = selectedCal,
                            events = allEvents,
                            onSelectEvent = { selectedEventForDetail = it }
                        )
                    }
                }
                else -> {
                    item {
                        TodaysEventsCard(
                            events = todaysEvents,
                            onDeleteEvent = { viewModel.deleteEvent(it) },
                            onSelectEvent = { selectedEventForDetail = it }
                        )
                    }
                }
            }

            // Bottom space for FAB and navigation bar
            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedCal.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            shape = RoundedCornerShape(AppRadius.sheet),
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance().apply { timeInMillis = millis }
                            viewModel.setSelectedDate(cal)
                        }
                        showDatePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(AppRadius.sm)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDatePickerDialog = false },
                    shape = RoundedCornerShape(AppRadius.sm)
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        EventEditorDialog(
            categories = categories,
            viewModel = viewModel,
            onDismiss = { showAddEventDialog = false },
            onSaveSuccess = { showAddEventDialog = false }
        )
    }

    // Event Detail Sheet
    selectedEventForDetail?.let { ev ->
        EventDetailSheet(
            event = ev,
            viewModel = viewModel,
            onDismiss = { selectedEventForDetail = null },
            onEditEvent = {
                selectedEventForDetail = null
                eventToEdit = it
            }
        )
    }

    // Edit Event Dialog
    eventToEdit?.let { ev ->
        EventEditorDialog(
            initialEvent = ev,
            categories = categories,
            viewModel = viewModel,
            onDismiss = { eventToEdit = null },
            onSaveSuccess = { eventToEdit = null }
        )
    }

    // Holidays Dialog
    if (showHolidaysDialog) {
        HolidaysListDialog(
            holidays = allHolidays,
            onDismiss = { showHolidaysDialog = false }
        )
    }
}

/**
 * Full Month Calendar Grid Card with:
 * - 7 Days Weekday Header: রবি (Sun), সোম (Mon), মঙ্গল (Tue), বুধ (Wed), বৃহ (Thu), শুক্র (Fri), শনি (Sat)
 * - Date cell with Bengali Numerals, Gregorian Day, Prayer/Solar Times subtext, and Today Highlight.
 */
@Composable
fun FullMonthCalendarCard(
    selectedCal: Calendar,
    events: List<Event>,
    holidays: List<Holiday>,
    hijriAdjustment: Int,
    onSelectDate: (Calendar) -> Unit,
    onNavigateMonth: (Int) -> Unit
) {
    val weekDaysBnEn = listOf(
        Pair("রবি", "Sun"),
        Pair("সোম", "Mon"),
        Pair("মঙ্গল", "Tue"),
        Pair("বুধ", "Wed"),
        Pair("বৃহ", "Thu"),
        Pair("শুক্র", "Fri"),
        Pair("শনি", "Sat")
    )

    val currentMonthCal = (selectedCal.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val daysInMonth = currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = currentMonthCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
    val todayCal = remember { Calendar.getInstance() }

    // Month Navigation Header & Bangla/Hijri Month Pill
    val banglaMonthSpan = remember(selectedCal) { CalendarConverters.getBanglaMonthSpan(selectedCal) }
    val hijriMonthSpan = remember(selectedCal, hijriAdjustment) { CalendarConverters.getHijriMonthSpan(selectedCal, hijriAdjustment) }

    // Swipe horizontal drag state
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val dragModifier = Modifier.draggable(
        orientation = Orientation.Horizontal,
        state = rememberDraggableState { delta -> dragOffset += delta },
        onDragStopped = {
            if (dragOffset > 80) onNavigateMonth(-1)
            else if (dragOffset < -80) onNavigateMonth(1)
            dragOffset = 0f
        }
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("full_month_calendar_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm)
                .then(dragModifier)
        ) {
            // Traditional Month Range Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bangla Month Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(AppRadius.full),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🌾", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = banglaMonthSpan,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Hijri Month Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(AppRadius.full),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🌙", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = hijriMonthSpan,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 7-DAY WEEKDAY HEADER ROW (রবি-শনি)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weekDaysBnEn.forEachIndexed { index, (bn, en) ->
                        val isWeekend = index == 0 || index == 5 || index == 6 // Sun, Fri, Sat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = bn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = if (isWeekend) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = en,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (isWeekend) MaterialTheme.colorScheme.error.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Build list of all day cells in the month grid
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
                // Leading days from next month
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

            // Grid Rows (7 columns per row)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        week.forEachIndexed { dayIndex, dayCal ->
                            val isCurrentMonth = dayCal.get(Calendar.MONTH) == currentMonthCal.get(Calendar.MONTH)
                            val isWeekend = dayIndex == 0 || dayIndex == 5 || dayIndex == 6
                            val isToday = CalendarUtils.isSameDay(dayCal, todayCal)
                            val isSelected = CalendarUtils.isSameDay(dayCal, selectedCal)

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

                            val solarTimes = remember(dayCal) { CalendarUtils.getSolarPrayerTimesForDate(dayCal) }
                            val banglaDate = remember(dayCal) { CalendarUtils.getBanglaDate(dayCal) }

                            MonthDateCell(
                                modifier = Modifier.weight(1f),
                                dayCal = dayCal,
                                isCurrentMonth = isCurrentMonth,
                                isToday = isToday,
                                isSelected = isSelected,
                                isWeekend = isWeekend,
                                hasHoliday = hasHoliday,
                                hasEvent = hasEvent,
                                solarTimes = solarTimes,
                                banglaDate = banglaDate,
                                onClick = { onSelectDate(dayCal) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single Date Cell in the Calendar Grid
 */
@Composable
fun MonthDateCell(
    modifier: Modifier = Modifier,
    dayCal: Calendar,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    isWeekend: Boolean,
    hasHoliday: Boolean,
    hasEvent: Boolean,
    solarTimes: CalendarUtils.SolarPrayerTimes,
    banglaDate: CalendarUtils.BanglaDate,
    onClick: () -> Unit
) {
    val dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH)
    val dayBn = CalendarUtils.toBanglaDigit(dayOfMonth)

    val cellBg = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        isSelected && !isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        hasHoliday -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
    }

    val cellBorder = when {
        isSelected && !isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        hasHoliday -> BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
        !isCurrentMonth -> BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        else -> BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }

    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("date_cell_${dayOfMonth}"),
        shape = RoundedCornerShape(8.dp),
        color = cellBg,
        border = cellBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP: Date representation (Today is highlighted in a primary circle badge)
            if (isToday) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = dayBn,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayBn,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = when {
                            !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            isSelected -> MaterialTheme.colorScheme.primary
                            hasHoliday -> MaterialTheme.colorScheme.tertiary
                            isWeekend -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (hasHoliday && isCurrentMonth) {
                        Spacer(modifier = Modifier.width(1.dp))
                        Text("★", fontSize = 8.sp, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            // MIDDLE/BOTTOM: Sunrise/Sunset or Prayer Times Subtext in Bangla
            if (isCurrentMonth) {
                Text(
                    text = "সূ: ${solarTimes.sunrise}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 9.sp
                    ),
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "•",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // BOTTOM: Event Indicator Dot
            if (hasEvent && isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Summary Card for Selected Date: Traditional Bangla, Hijri, Solar Prayer Times, and Day Agenda
 */
@Composable
fun SelectedDateSummaryCard(
    selectedCal: Calendar,
    events: List<Event>,
    holidays: List<Holiday>,
    hijriAdjustment: Int,
    onAddEvent: () -> Unit,
    onSelectEvent: (Event) -> Unit
) {
    val banglaDate = remember(selectedCal) { CalendarUtils.getBanglaDate(selectedCal) }
    val hijriDate = remember(selectedCal, hijriAdjustment) { CalendarUtils.getHijriDate(selectedCal, hijriAdjustment) }
    val solarTimes = remember(selectedCal) { CalendarUtils.getSolarPrayerTimesForDate(selectedCal) }

    val startOfDay = (selectedCal.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val endOfDay = (selectedCal.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    val dayEvents = events.filter { it.startDate in startOfDay..endOfDay }
    val dayHolidays = holidays.filter {
        val hCal = Calendar.getInstance().apply { timeInMillis = it.date }
        CalendarUtils.isSameDay(selectedCal, hCal)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_date_summary_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md)
        ) {
            // Selected Day Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = CalendarUtils.formatDate(selectedCal.timeInMillis, "EEEE, d MMMM yyyy"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🌾 ${banglaDate.formattedBn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "🌙 ${CalendarUtils.toBanglaDigit(hijriDate.day)} ${hijriDate.monthNameEn} ${CalendarUtils.toBanglaDigit(hijriDate.year)} হিজরী",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // Add button for selected day
                IconButton(
                    onClick = onAddEvent,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Event to Date",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prayer & Solar Times Grid Strip (ফজর, সূর্যোদয়, যোহর, আসর, মাগরিব, ইশা)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val prayerList = listOf(
                        Triple("ফজর", solarTimes.fajr, "🌅"),
                        Triple("সূর্যোদয়", solarTimes.sunrise, "☀️"),
                        Triple("যোহর", solarTimes.dhuhr, "🌞"),
                        Triple("আসর", solarTimes.asr, "⛅"),
                        Triple("মাগরিব", solarTimes.maghrib, "🌇"),
                        Triple("ইশা", solarTimes.isha, "🌙")
                    )

                    prayerList.forEach { (name, time, emoji) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 11.sp)
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = time,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // If Holiday on selected day
            if (dayHolidays.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                dayHolidays.forEach { holiday ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎉", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = holiday.nameBn.ifBlank { holiday.name },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = holiday.type,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Events on this day
            if (dayEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "দিনের ইভেন্ট ও শিডিউল (${CalendarUtils.toBanglaDigit(dayEvents.size)} টি):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                dayEvents.forEach { ev ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { onSelectEvent(ev) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Column {
                                    Text(
                                        text = ev.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${CalendarUtils.formatTime(ev.startDate)} - ${CalendarUtils.formatTime(ev.endDate)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
