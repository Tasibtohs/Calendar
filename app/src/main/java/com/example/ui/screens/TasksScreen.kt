package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Section Palette Constants
private val NAVY_BLUE_ACCENT = Color(0xFF0D47A1)
private val UPCOMING_BLUE = Color(0xFF0284C7)
private val TASK_GREEN = Color(0xFF10B981)
private val NOTE_AMBER = Color(0xFFF59E0B)
private val BIRTHDAY_ROSE = Color(0xFFEC4899)
private val ANNIVERSARY_RUBY = Color(0xFFE11D48)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Observe streams from ViewModel
    val todaysEvents by viewModel.todaysEvents.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val allBirthdays by viewModel.allBirthdays.collectAsState()
    val allAnniversaries by viewModel.allAnniversaries.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    // Dialog & Sheet States
    var showQuickAddSheet by remember { mutableStateOf(false) }
    var activeAddType by remember { mutableStateOf<String?>(null) } // "Event", "Task", "Note", "Birthday", "Anniversary"

    var eventForDetail by remember { mutableStateOf<Event?>(null) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var birthdayToEdit by remember { mutableStateOf<Birthday?>(null) }
    var anniversaryToEdit by remember { mutableStateOf<Anniversary?>(null) }

    // Task Filter State
    var taskStatusFilter by remember { mutableStateOf("All") } // "All", "Pending", "Completed"

    val snackbarHostState = remember { SnackbarHostState() }
    val lastDeletedEntity by viewModel.lastDeletedEntity.collectAsState()

    LaunchedEffect(lastDeletedEntity) {
        if (lastDeletedEntity != null) {
            val message = when (lastDeletedEntity) {
                is CalendarViewModel.DeletedEntity.DeletedTask -> "টাস্ক মুছে ফেলা হয়েছে (Task Deleted)"
                is CalendarViewModel.DeletedEntity.DeletedEvent -> "ইভেন্ট মুছে ফেলা হয়েছে (Event Deleted)"
                is CalendarViewModel.DeletedEntity.DeletedNote -> "নোট মুছে ফেলা হয়েছে (Note Deleted)"
                is CalendarViewModel.DeletedEntity.DeletedBirthday -> "জন্মদিন মুছে ফেলা হয়েছে"
                is CalendarViewModel.DeletedEntity.DeletedAnniversary -> "বার্ষিকী মুছে ফেলা হয়েছে"
                else -> "আইটেম মুছে ফেলা হয়েছে"
            }
            val result = snackbarHostState.showSnackbar(
                message = message,
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

    Scaffold(
        modifier = modifier.testTag("events_and_tasks_scaffold"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showQuickAddSheet = true },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Add",
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = {
                    Text(
                        text = "+ Quick Add",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                containerColor = NAVY_BLUE_ACCENT,
                contentColor = Color.White,
                shape = RoundedCornerShape(AppRadius.lg),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = AppElevation.medium,
                    pressedElevation = AppElevation.high
                ),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("fab_quick_add_events_tasks")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            // (1) TODAY'S EVENTS SECTION
            SectionCard(
                title = "Today's Events",
                subtitle = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()),
                badgeColor = NAVY_BLUE_ACCENT,
                badgeIcon = Icons.Outlined.Event,
                count = todaysEvents.size,
                onAddClick = { activeAddType = "Event" },
                testTag = "section_todays_events"
            ) {
                if (todaysEvents.isEmpty()) {
                    EmptySectionPlaceholder(
                        message = "আজ কোনো Event নেই",
                        onAddClick = { activeAddType = "Event" },
                        buttonText = "+ Add Event"
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        todaysEvents.forEach { event ->
                            val category = categories.find { it.id == event.categoryId }
                            EventItemRow(
                                event = event,
                                category = category,
                                onClick = { eventForDetail = event },
                                onEdit = { eventToEdit = event },
                                onDelete = { viewModel.deleteEvent(event) }
                            )
                        }
                    }
                }
            }

            // (2) UPCOMING EVENTS SECTION
            SectionCard(
                title = "Upcoming Events",
                subtitle = "পরবর্তী নির্ধারিত ইভেন্টসমূহ",
                badgeColor = UPCOMING_BLUE,
                badgeIcon = Icons.Outlined.CalendarMonth,
                count = upcomingEvents.size,
                onAddClick = { activeAddType = "Event" },
                testTag = "section_upcoming_events"
            ) {
                if (upcomingEvents.isEmpty()) {
                    EmptySectionPlaceholder(
                        message = "No upcoming events scheduled",
                        onAddClick = { activeAddType = "Event" },
                        buttonText = "+ Add Event"
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        upcomingEvents.forEach { event ->
                            val category = categories.find { it.id == event.categoryId }
                            UpcomingEventItemRow(
                                event = event,
                                category = category,
                                onClick = { eventForDetail = event },
                                onEdit = { eventToEdit = event },
                                onDelete = { viewModel.deleteEvent(event) }
                            )
                        }
                    }
                }
            }

            // (3) TASKS SECTION
            val filteredTasks = remember(allTasks, taskStatusFilter) {
                when (taskStatusFilter) {
                    "Pending" -> allTasks.filter { !it.isCompleted }
                    "Completed" -> allTasks.filter { it.isCompleted }
                    else -> allTasks
                }
            }

            SectionCard(
                title = "Tasks",
                subtitle = "${allTasks.count { !it.isCompleted }} Pending • ${allTasks.count { it.isCompleted }} Done",
                badgeColor = TASK_GREEN,
                badgeIcon = Icons.Outlined.CheckCircle,
                count = allTasks.size,
                onAddClick = { activeAddType = "Task" },
                testTag = "section_tasks"
            ) {
                if (allTasks.isEmpty()) {
                    EmptySectionPlaceholder(
                        message = "এখনো কোনো Task নেই",
                        onAddClick = { activeAddType = "Task" },
                        buttonText = "+ Add"
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        // Sub-filter tabs for tasks
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("All", "Pending", "Completed").forEach { filter ->
                                val isSelected = taskStatusFilter == filter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { taskStatusFilter = filter },
                                    label = {
                                        Text(
                                            text = filter,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(AppRadius.full),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TASK_GREEN.copy(alpha = 0.16f),
                                        selectedLabelColor = TASK_GREEN
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        selectedBorderColor = TASK_GREEN.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        if (filteredTasks.isEmpty()) {
                            Text(
                                text = "কোনো $taskStatusFilter টাস্ক নেই",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = AppSpacing.sm)
                            )
                        } else {
                            filteredTasks.forEach { task ->
                                val category = categories.find { it.id == task.categoryId }
                                TaskItemRow(
                                    task = task,
                                    category = category,
                                    onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                                    onEdit = { taskToEdit = task },
                                    onDelete = { taskToDelete = task }
                                )
                            }
                        }
                    }
                }
            }

            // (4) NOTES SECTION
            SectionCard(
                title = "Notes",
                subtitle = "আইডিয়া, ড্রাফট ও মেমো",
                badgeColor = NOTE_AMBER,
                badgeIcon = Icons.Outlined.StickyNote2,
                count = allNotes.size,
                onAddClick = { activeAddType = "Note" },
                testTag = "section_notes"
            ) {
                if (allNotes.isEmpty()) {
                    EmptySectionPlaceholder(
                        message = "কোনো Note নেই",
                        onAddClick = { activeAddType = "Note" },
                        buttonText = "+ Add"
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        allNotes.take(4).forEach { note ->
                            val category = categories.find { it.id == note.categoryId }
                            NoteItemRow(
                                note = note,
                                category = category,
                                onClick = { noteToEdit = note },
                                onCopy = {
                                    copyTextToClipboard(context, "${note.title}\n\n${note.content}")
                                    Toast.makeText(context, "নোট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = { viewModel.deleteNote(note) }
                            )
                        }
                        if (allNotes.size > 4) {
                            TextButton(
                                onClick = { viewModel.selectTab(3) }, // Go to Notes Tab
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = "সব নোট দেখুন (${allNotes.size}) →",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NOTE_AMBER
                                )
                            }
                        }
                    }
                }
            }

            // (5) BIRTHDAYS SECTION
            SectionCard(
                title = "Birthdays",
                subtitle = "শুভ জন্মদিন ও স্মরণীয় দিন",
                badgeColor = BIRTHDAY_ROSE,
                badgeIcon = Icons.Outlined.Cake,
                count = allBirthdays.size,
                onAddClick = { activeAddType = "Birthday" },
                testTag = "section_birthdays"
            ) {
                if (allBirthdays.isEmpty()) {
                    EmptySectionPlaceholder(
                        message = "এখনো কোনো Birthday পাওয়া যায়নি",
                        onAddClick = { activeAddType = "Birthday" },
                        buttonText = "+ Add"
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        allBirthdays.forEach { bday ->
                            BirthdayItemRow(
                                birthday = bday,
                                onEdit = { birthdayToEdit = bday },
                                onDelete = { viewModel.deleteBirthday(bday) }
                            )
                        }
                    }
                }
            }

            // (6) ANNIVERSARIES SECTION
            SectionCard(
                title = "Anniversaries",
                subtitle = "বিবাহবার্ষিকী ও বিশেষ মাইলস্টোন",
                badgeColor = ANNIVERSARY_RUBY,
                badgeIcon = Icons.Outlined.Favorite,
                count = allAnniversaries.size,
                onAddClick = { activeAddType = "Anniversary" },
                testTag = "section_anniversaries"
            ) {
                if (allAnniversaries.isEmpty()) {
                    EmptySectionPlaceholder(
                        message = "কোনো Anniversary নেই",
                        onAddClick = { activeAddType = "Anniversary" },
                        buttonText = "+ Add"
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        allAnniversaries.forEach { anni ->
                            AnniversaryItemRow(
                                anniversary = anni,
                                onEdit = { anniversaryToEdit = anni },
                                onDelete = { viewModel.deleteAnniversary(anni) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Quick Add Bottom Sheet
    if (showQuickAddSheet) {
        QuickAddBottomSheet(
            viewModel = viewModel,
            onDismiss = { showQuickAddSheet = false },
            onSelectAddType = { type ->
                showQuickAddSheet = false
                activeAddType = type
            }
        )
    }

    // Generic Entity Creation Dialog
    activeAddType?.let { type ->
        when (type) {
            "Task" -> {
                TaskEditorDialog(
                    categories = categories,
                    onDismiss = { activeAddType = null },
                    onSave = { newTask ->
                        viewModel.saveTask(newTask)
                        activeAddType = null
                    }
                )
            }
            "Event" -> {
                EventEditorDialog(
                    categories = categories,
                    viewModel = viewModel,
                    onDismiss = { activeAddType = null },
                    onSaveSuccess = { activeAddType = null }
                )
            }
            "Note" -> {
                NoteEditorDialog(
                    categories = categories,
                    onDismiss = { activeAddType = null },
                    onSave = { newNote ->
                        viewModel.saveNote(newNote)
                        activeAddType = null
                    }
                )
            }
            "Birthday" -> {
                BirthdayEditorDialog(
                    onDismiss = { activeAddType = null },
                    onSave = { bday ->
                        viewModel.saveBirthday(bday)
                        activeAddType = null
                    }
                )
            }
            "Anniversary" -> {
                AnniversaryEditorDialog(
                    onDismiss = { activeAddType = null },
                    onSave = { anni ->
                        viewModel.saveAnniversary(anni)
                        activeAddType = null
                    }
                )
            }
            else -> {
                CreateEntityDialog(
                    addType = type,
                    viewModel = viewModel,
                    onDismiss = { activeAddType = null }
                )
            }
        }
    }

    // Detail and Edit Dialogs
    eventForDetail?.let { ev ->
        EventDetailSheet(
            event = ev,
            viewModel = viewModel,
            onDismiss = { eventForDetail = null },
            onEditEvent = {
                eventForDetail = null
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

    taskToEdit?.let { task ->
        TaskEditorDialog(
            initialTask = task,
            categories = categories,
            onDismiss = { taskToEdit = null },
            onSave = { updated ->
                viewModel.saveTask(updated)
                taskToEdit = null
            }
        )
    }

    taskToDelete?.let { task ->
        AppDeleteDialog(
            onDismissRequest = { taskToDelete = null },
            title = "Delete Task",
            message = "Are you sure you want to permanently delete '${task.title}'?",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm = {
                viewModel.deleteTask(task)
                taskToDelete = null
            }
        )
    }

    noteToEdit?.let { note ->
        NoteEditorDialog(
            initialNote = note,
            categories = categories,
            onDismiss = { noteToEdit = null },
            onSave = { updated ->
                viewModel.saveNote(updated)
                noteToEdit = null
            }
        )
    }

    birthdayToEdit?.let { bday ->
        BirthdayEditorDialog(
            initialBirthday = bday,
            onDismiss = { birthdayToEdit = null },
            onSave = { updated ->
                viewModel.saveBirthday(updated)
                birthdayToEdit = null
            }
        )
    }

    anniversaryToEdit?.let { anni ->
        AnniversaryEditorDialog(
            initialAnniversary = anni,
            onDismiss = { anniversaryToEdit = null },
            onSave = { updated ->
                viewModel.saveAnniversary(updated)
                anniversaryToEdit = null
            }
        )
    }
}

// -------------------------------------------------------------
// REUSABLE SECTION CONTAINER CARD
// -------------------------------------------------------------
@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    badgeColor: Color,
    badgeIcon: ImageVector,
    count: Int = 0,
    onAddClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(AppRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    modifier = Modifier.weight(1f)
                ) {
                    // Colorful circular icon badge on the left
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.1).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (count > 0) {
                                Surface(
                                    color = badgeColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(AppRadius.full),
                                    modifier = Modifier.height(20.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // "+ Add" link / button
                TextButton(
                    onClick = onAddClick,
                    contentPadding = PaddingValues(horizontal = AppSpacing.xs, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Body Content
            content()
        }
    }
}

// -------------------------------------------------------------
// EMPTY PLACEHOLDER COMPONENT
// -------------------------------------------------------------
@Composable
fun EmptySectionPlaceholder(
    message: String,
    onAddClick: () -> Unit,
    buttonText: String = "+ Add"
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddClick() },
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.6f),
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Text(
                text = buttonText,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// -------------------------------------------------------------
// TODAY'S EVENT ITEM ROW
// -------------------------------------------------------------
@Composable
fun EventItemRow(
    event: Event,
    category: Category?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(category?.let { parseHexColor(it.colorHex) } ?: NAVY_BLUE_ACCENT)
                )

                Column {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (event.isAllDay) "All Day" else "${CalendarUtils.formatTime(event.startDate)} - ${CalendarUtils.formatTime(event.endDate)}${if (event.location.isNotBlank()) " • 📍 ${event.location}" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// UPCOMING EVENT ITEM ROW
// -------------------------------------------------------------
@Composable
fun UpcomingEventItemRow(
    event: Event,
    category: Category?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val daysAway = remember(event.startDate) {
        val diff = event.startDate - System.currentTimeMillis()
        TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(category?.let { parseHexColor(it.colorHex) } ?: UPCOMING_BLUE)
                )

                Column {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${CalendarUtils.formatDate(event.startDate)} • ${if (event.isAllDay) "All Day" else CalendarUtils.formatTime(event.startDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    color = UPCOMING_BLUE.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(AppRadius.xs)
                ) {
                    Text(
                        text = if (daysAway == 0L) "Today" else if (daysAway == 1L) "Tomorrow" else "$daysAway days left",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = UPCOMING_BLUE,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TASK ITEM ROW
// -------------------------------------------------------------
@Composable
fun TaskItemRow(
    task: Task,
    category: Category?,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priority.lowercase()) {
        "high" -> Color(0xFFEF4444)
        "medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                modifier = Modifier.weight(1f)
            ) {
                // Interactive Checkbox
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = if (task.isCompleted) "Mark Incomplete" else "Mark Complete",
                        tint = if (task.isCompleted) TASK_GREEN else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.clickable { onEdit() }) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Priority Dot & Label
                        Surface(
                            color = priorityColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(AppRadius.xs)
                        ) {
                            Text(
                                text = task.priority,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                                color = priorityColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        if (task.dueDate != null) {
                            Text(
                                text = "Due: ${CalendarUtils.formatDate(task.dueDate)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// NOTE ITEM ROW
// -------------------------------------------------------------
@Composable
fun NoteItemRow(
    note: Note,
    category: Category?,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val noteColor = parseHexColor(note.colorHex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(noteColor)
                )

                Column {
                    Text(
                        text = note.title.ifBlank { "Untitled Note" },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// BIRTHDAY ITEM ROW
// -------------------------------------------------------------
@Composable
fun BirthdayItemRow(
    birthday: Birthday,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BIRTHDAY_ROSE)
                )

                Column {
                    Text(
                        text = birthday.personName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Birthday: ${CalendarUtils.formatDate(birthday.birthDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ANNIVERSARY ITEM ROW
// -------------------------------------------------------------
@Composable
fun AnniversaryItemRow(
    anniversary: Anniversary,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ANNIVERSARY_RUBY)
                )

                Column {
                    Text(
                        text = anniversary.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Date: ${CalendarUtils.formatDate(anniversary.date)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TASK CREATION / EDIT DIALOG
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorDialog(
    initialTask: Task? = null,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var dueDateMillis by remember { mutableLongStateOf(initialTask?.dueDate ?: System.currentTimeMillis()) }
    var dueTime by remember { mutableStateOf(initialTask?.dueTime ?: "10:00 AM") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: "Medium") }
    var categoryId by remember { mutableLongStateOf(initialTask?.categoryId ?: categories.firstOrNull()?.id ?: 1L) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppRadius.dialog),
        title = {
            Text(
                text = if (initialTask == null) "Create Task" else "Edit Task",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    shape = RoundedCornerShape(AppRadius.md),
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    shape = RoundedCornerShape(AppRadius.md),
                    modifier = Modifier.fillMaxWidth().testTag("input_task_desc")
                )

                // Date Picker Button
                Surface(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(AppRadius.md),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Due Date: ${CalendarUtils.formatDate(dueDateMillis)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.iconSmall)
                        )
                    }
                }

                // Priority Selection
                Text(
                    text = "Priority:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    listOf("High", "Medium", "Low").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) },
                            shape = RoundedCornerShape(AppRadius.sm),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val task = (initialTask ?: Task(title = title)).copy(
                            title = title,
                            description = description,
                            dueDate = dueDateMillis,
                            dueTime = dueTime,
                            priority = priority,
                            categoryId = categoryId
                        )
                        onSave(task)
                    }
                },
                shape = RoundedCornerShape(AppRadius.md),
                colors = ButtonDefaults.buttonColors(containerColor = NAVY_BLUE_ACCENT),
                modifier = Modifier.testTag("button_save_task")
            ) {
                Text("Save Task")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppRadius.md)
            ) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(AppRadius.dialog)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// -------------------------------------------------------------
// HELPER FUNCTIONS
// -------------------------------------------------------------
private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        NAVY_BLUE_ACCENT
    }
}

private fun copyTextToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("Copied Note", text)
    clipboard?.setPrimaryClip(clip)
}
