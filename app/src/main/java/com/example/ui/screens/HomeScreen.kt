package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.*
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CalendarViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val todaysEvents by viewModel.todaysEvents.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val birthdays by viewModel.allBirthdays.collectAsState()
    val anniversaries by viewModel.allAnniversaries.collectAsState()
    val holidays by viewModel.allHolidays.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var showQuickAddSheet by remember { mutableStateOf(false) }
    var activeCreateType by remember { mutableStateOf<String?>(null) }
    var selectedEventForDetail by remember { mutableStateOf<com.example.data.model.Event?>(null) }
    var eventToEdit by remember { mutableStateOf<com.example.data.model.Event?>(null) }
    var showEventEditor by remember { mutableStateOf(false) }

    var showGlobalSearchDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }

    var showHolidaysDialog by remember { mutableStateOf(false) }
    var showDateCalcDialog by remember { mutableStateOf(false) }
    var showAddCountdownDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showFreeTimeDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val lastDeletedEntity by viewModel.lastDeletedEntity.collectAsState()

    LaunchedEffect(lastDeletedEntity) {
        if (lastDeletedEntity != null) {
            val result = snackbarHostState.showSnackbar(
                message = "আইটেম মুছে ফেলা হয়েছে (Item Deleted)",
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

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.testTag("home_screen_scaffold"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        floatingActionButton = {

            ExtendedFloatingActionButton(
                onClick = { showQuickAddSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Item") },
                text = { Text("Quick Add") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("home_fab_add")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // 1. Custom Cover Photo Section
            HeaderCoverPhoto(viewModel = viewModel)

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // 2. Greeting Header with Tri-Calendar Dates (English, Bangla, Arabic)
            DailyBriefingCard(
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = AppSpacing.lg)
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // A.3 Quick Action Row (Reusable, List-Driven)
            val quickActions = remember {
                listOf(
                    Triple("ছুটির তালিকা", Icons.Outlined.Celebration) { showHolidaysDialog = true },
                    Triple("Calculator", Icons.Outlined.Calculate) { showDateCalcDialog = true },
                    Triple("Statistics", Icons.Outlined.BarChart) { showStatsDialog = true },
                    Triple("Free Time", Icons.Outlined.Schedule) { showFreeTimeDialog = true },
                    Triple("Archive & Export", Icons.Outlined.Archive) { showArchiveDialog = true }
                )
            }

            androidx.compose.foundation.lazy.LazyRow(
                contentPadding = PaddingValues(horizontal = AppSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                items(quickActions.size) { idx ->
                    val (label, icon, onClick) = quickActions[idx]
                    AppChip(
                        label = label,
                        selected = false,
                        onClick = onClick,
                        leadingIcon = icon
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // A.4 & A.5 Calendar View (View Selector Tabs + Tri-Calendar Month Grid)
            CalendarViewComposable(
                viewModel = viewModel,
                isEmbeddedInHome = true,
                onSelectEvent = { selectedEventForDetail = it }
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // A.6 Countdowns Section
            CountdownSection(
                viewModel = viewModel,
                onAddCountdownClick = { showAddCountdownDialog = true }
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // A.7 Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
                shape = RoundedCornerShape(AppRadius.md),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = AppElevation.low,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar"),
                    placeholder = { 
                        Text(
                            "Search events, tasks, notes, birthdays...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    leadingIcon = {
                        IconButton(onClick = { showGlobalSearchDialog = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Global Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                                }
                            }
                            IconButton(
                                onClick = { showGlobalSearchDialog = true },
                                modifier = Modifier.testTag("open_global_search_button")
                            ) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = "Advanced Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(AppRadius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // 8. Today's Events Card
            TodaysEventsCard(
                events = if (searchQuery.isBlank()) todaysEvents else todaysEvents.filter { it.title.contains(searchQuery, ignoreCase = true) },
                onDeleteEvent = { viewModel.deleteEvent(it) },
                onSelectEvent = { selectedEventForDetail = it }
            )

            // 9. Upcoming Events Card
            UpcomingEventsCard(
                events = if (searchQuery.isBlank()) upcomingEvents else upcomingEvents.filter { it.title.contains(searchQuery, ignoreCase = true) },
                onSelectEvent = { selectedEventForDetail = it }
            )

            // 10. Tasks Section Card
            TasksSectionCard(
                tasks = if (searchQuery.isBlank()) tasks else tasks.filter { it.title.contains(searchQuery, ignoreCase = true) },
                onToggleTask = { viewModel.toggleTaskCompletion(it) },
                onDeleteTask = { viewModel.deleteTask(it) },
                onAddNewTask = { activeCreateType = "Task" }
            )

            // 11. Notes Section Card
            NotesSectionCard(
                notes = if (searchQuery.isBlank()) notes else notes.filter { it.title.contains(searchQuery, ignoreCase = true) },
                onTogglePin = { viewModel.toggleNotePin(it) },
                onDeleteNote = { viewModel.deleteNote(it) },
                onAddNewNote = { activeCreateType = "Note" }
            )

            // 12. Birthdays Card
            BirthdaysCard(
                birthdays = if (searchQuery.isBlank()) birthdays else birthdays.filter { it.personName.contains(searchQuery, ignoreCase = true) },
                onDeleteBirthday = { viewModel.deleteBirthday(it) },
                onAddBirthday = { activeCreateType = "Birthday" }
            )

            // 13. Anniversaries Card
            AnniversariesCard(
                anniversaries = if (searchQuery.isBlank()) anniversaries else anniversaries.filter { it.title.contains(searchQuery, ignoreCase = true) },
                onDeleteAnniversary = { viewModel.deleteAnniversary(it) },
                onAddAnniversary = { activeCreateType = "Anniversary" }
            )

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    if (showQuickAddSheet) {
        QuickAddBottomSheet(
            viewModel = viewModel,
            onDismiss = { showQuickAddSheet = false },
            onSelectAddType = { selectedType ->
                activeCreateType = selectedType
            }
        )
    }

    activeCreateType?.let { type ->
        when (type) {
            "Event" -> {
                EventEditorDialog(
                    categories = categories,
                    viewModel = viewModel,
                    onDismiss = { activeCreateType = null },
                    onSaveSuccess = { activeCreateType = null }
                )
            }
            "Note" -> {
                NoteEditorDialog(
                    categories = categories,
                    onDismiss = { activeCreateType = null },
                    onSave = { note ->
                        viewModel.saveNote(note)
                        activeCreateType = null
                    }
                )
            }
            "Birthday" -> {
                BirthdayEditorDialog(
                    onDismiss = { activeCreateType = null },
                    onSave = { bday ->
                        viewModel.saveBirthday(bday)
                        activeCreateType = null
                    }
                )
            }
            "Anniversary" -> {
                AnniversaryEditorDialog(
                    onDismiss = { activeCreateType = null },
                    onSave = { anni ->
                        viewModel.saveAnniversary(anni)
                        activeCreateType = null
                    }
                )
            }
            "Holiday" -> {
                CustomHolidayEditorDialog(
                    onDismiss = { activeCreateType = null },
                    onSave = { hol ->
                        viewModel.saveHoliday(hol)
                        activeCreateType = null
                    }
                )
            }
            "Category" -> {
                CategoryEditorDialog(
                    onDismiss = { activeCreateType = null },
                    onSave = { cat ->
                        viewModel.saveCategory(cat)
                        activeCreateType = null
                    }
                )
            }
            else -> {
                CreateEntityDialog(
                    addType = type,
                    viewModel = viewModel,
                    onDismiss = { activeCreateType = null }
                )
            }
        }
    }

    if (showGlobalSearchDialog) {
        GlobalSearchDialog(
            viewModel = viewModel,
            onDismiss = { showGlobalSearchDialog = false },
            onSelectEvent = { selectedEventForDetail = it }
        )
    }

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

    eventToEdit?.let { ev ->
        EventEditorDialog(
            initialEvent = ev,
            categories = categories,
            viewModel = viewModel,
            onDismiss = { eventToEdit = null },
            onSaveSuccess = { eventToEdit = null }
        )
    }

    if (showEventEditor) {
        EventEditorDialog(
            categories = categories,
            viewModel = viewModel,
            onDismiss = { showEventEditor = false },
            onSaveSuccess = { showEventEditor = false }
        )
    }

    if (showDateCalcDialog) {
        Dialog(
            onDismissRequest = { showDateCalcDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            DateCalculatorScreen(
                onBack = { showDateCalcDialog = false }
            )
        }
    }

    if (showAddCountdownDialog) {
        AddCountdownDialog(
            onDismiss = { showAddCountdownDialog = false },
            onSave = { countdown -> viewModel.saveCountdown(countdown) }
        )
    }

    if (showHolidaysDialog) {
        Dialog(
            onDismissRequest = { showHolidaysDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            HolidaysScreen(
                viewModel = viewModel,
                onBack = { showHolidaysDialog = false },
                onQuickAdd = {
                    showHolidaysDialog = false
                    showQuickAddSheet = true
                }
            )
        }
    }

    if (showStatsDialog) {
        Dialog(
            onDismissRequest = { showStatsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            StatisticsScreen(
                viewModel = viewModel,
                onBack = { showStatsDialog = false }
            )
        }
    }

    if (showFreeTimeDialog) {
        Dialog(
            onDismissRequest = { showFreeTimeDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            FreeTimeScreen(
                viewModel = viewModel,
                onBack = { showFreeTimeDialog = false }
            )
        }
    }

    if (showArchiveDialog) {
        Dialog(
            onDismissRequest = { showArchiveDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ArchiveExportScreen(
                viewModel = viewModel,
                onBack = { showArchiveDialog = false }
            )
        }
    }

    if (showNotificationDialog) {
        NotificationHistoryDialog(
            viewModel = viewModel,
            onDismiss = { showNotificationDialog = false },
            onSelectEvent = { selectedEventForDetail = it }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            viewModel = viewModel,
            onDismiss = { showBackupRestoreDialog = false }
        )
    }
}

