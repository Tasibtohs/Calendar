package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Category
import com.example.data.model.Event
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import com.example.util.ConflictDetector
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventEditorDialog(
    initialEvent: Event? = null,
    initialDate: Calendar = Calendar.getInstance(),
    categories: List<Category> = emptyList(),
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val allEvents by viewModel.allEvents.collectAsState()

    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var isAllDay by remember { mutableStateOf(initialEvent?.isAllDay ?: false) }
    var location by remember { mutableStateOf(initialEvent?.location ?: "") }
    var selectedCategoryId by remember { mutableStateOf(initialEvent?.categoryId ?: 1L) }
    var colorHex by remember { mutableStateOf(initialEvent?.colorHex ?: "#3F51B5") }

    // Start & End Calendar
    val startCal = remember {
        Calendar.getInstance().apply {
            timeInMillis = initialEvent?.startDate ?: initialDate.timeInMillis
        }
    }
    val endCal = remember {
        Calendar.getInstance().apply {
            timeInMillis = initialEvent?.endDate ?: (initialDate.timeInMillis + 3600000L) // +1 hour
        }
    }

    var startTimestamp by remember { mutableLongStateOf(startCal.timeInMillis) }
    var endTimestamp by remember { mutableLongStateOf(endCal.timeInMillis) }

    // Pickers visibility
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // Multiple Reminders
    val defaultReminders = listOf(15)
    val initialRemindersList = remember(initialEvent) {
        initialEvent?.reminderMinutesList?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: defaultReminders
    }
    val selectedReminders = remember { mutableStateListOf<Int>().apply { addAll(initialRemindersList) } }

    // Repeat Rule
    var repeatType by remember { mutableStateOf(initialEvent?.repeatType ?: "NONE") } // "NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    var repeatEndType by remember { mutableStateOf(initialEvent?.repeatEndType ?: "NEVER") } // "NEVER", "DATE", "COUNT"
    var repeatUntilDate by remember { mutableStateOf(initialEvent?.repeatUntilDate) }
    var repeatCount by remember { mutableIntStateOf(initialEvent?.repeatCount ?: 5) }

    var participants by remember { mutableStateOf(initialEvent?.participants ?: "") }
    var attachmentUriStr by remember { mutableStateOf(initialEvent?.attachmentUri) }
    var linkUrl by remember { mutableStateOf(initialEvent?.linkUrl ?: "") }
    var notes by remember { mutableStateOf(initialEvent?.notes ?: "") }

    // Attachment file launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            attachmentUriStr = it.toString()
        }
    }

    // Schedule Conflict Detection
    val conflicts = remember(startTimestamp, endTimestamp, allEvents, initialEvent) {
        ConflictDetector.findConflicts(allEvents, startTimestamp, endTimestamp, excludeEventId = initialEvent?.id)
    }

    val availableColors = listOf(
        "#3F51B5", "#E91E63", "#4CAF50", "#FF9800", "#9C27B0", "#009688", "#F44336", "#00BCD4"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_editor_dialog"),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(AppRadius.dialog),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = AppElevation.dialog
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.lg)
            ) {
                // Dialog Title Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialEvent == null) "Create New Event" else "Edit Event",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(AppDimensions.iconMedium)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Conflict Warning Banner
                    AnimatedVisibility(visible = conflicts.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = AppSpacing.md),
                            shape = RoundedCornerShape(AppRadius.md),
                            color = MaterialTheme.colorScheme.errorContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(AppSpacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.sm))
                                Column {
                                    Text(
                                        text = "Time Slot Conflict Detected",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Overlaps with: ${conflicts.joinToString { it.title }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(AppRadius.md),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    // All Day Switch Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Today,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "All Day Event",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = isAllDay,
                                onCheckedChange = { isAllDay = it },
                                modifier = Modifier.testTag("all_day_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    // Date & Time Selectors Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(AppSpacing.md)) {
                            // Start Date & Time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Starts",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                    OutlinedButton(
                                        onClick = { showStartDatePicker = true },
                                        contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                                        shape = RoundedCornerShape(AppRadius.sm)
                                    ) {
                                        Text(CalendarUtils.formatDate(startTimestamp, "MMM d, yyyy"))
                                    }
                                    if (!isAllDay) {
                                        OutlinedButton(
                                            onClick = { showStartTimePicker = true },
                                            contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                                            shape = RoundedCornerShape(AppRadius.sm)
                                        ) {
                                            Text(CalendarUtils.formatTime(startTimestamp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(AppSpacing.sm))

                            // End Date & Time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ends",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                    if (!isAllDay) {
                                        OutlinedButton(
                                            onClick = { showEndTimePicker = true },
                                            contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                                            shape = RoundedCornerShape(AppRadius.sm)
                                        ) {
                                            Text(CalendarUtils.formatTime(endTimestamp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    // Color Picker
                    Text(
                        text = "Event Color",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        availableColors.forEach { hex ->
                            val parsed = parseHexColor(hex)
                            val isSelected = colorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(parsed)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { colorHex = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    // Category Selector
                    if (categories.isNotEmpty()) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                        ) {
                            categories.forEach { cat ->
                                val selected = selectedCategoryId == cat.id
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategoryId = cat.id },
                                    label = { Text(cat.name) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(parseHexColor(cat.colorHex))
                                        )
                                    },
                                    shape = RoundedCornerShape(AppRadius.sm)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                    }

                    // Multiple Reminders
                    Text(
                        text = "Reminders",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    val reminderOptions = listOf(
                        0 to "At time",
                        5 to "5m before",
                        15 to "15m before",
                        30 to "30m before",
                        60 to "1h before",
                        1440 to "1d before"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        reminderOptions.forEach { (mins, label) ->
                            val isSelected = selectedReminders.contains(mins)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedReminders.remove(mins) else selectedReminders.add(mins)
                                },
                                label = { Text(label) },
                                shape = RoundedCornerShape(AppRadius.sm)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    // Repeat / Recurrence Options
                    Text(
                        text = "Repeat / Recurrence",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    val repeatOptions = listOf("NONE" to "Does not repeat", "DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly", "YEARLY" to "Yearly")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        repeatOptions.forEach { (key, label) ->
                            FilterChip(
                                selected = repeatType == key,
                                onClick = { repeatType = key },
                                label = { Text(label) },
                                shape = RoundedCornerShape(AppRadius.sm)
                            )
                        }
                    }

                    if (repeatType != "NONE") {
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Text("Ends:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FilterChip(
                                selected = repeatEndType == "NEVER",
                                onClick = { repeatEndType = "NEVER" },
                                label = { Text("Never") },
                                shape = RoundedCornerShape(AppRadius.sm)
                            )
                            FilterChip(
                                selected = repeatEndType == "COUNT",
                                onClick = { repeatEndType = "COUNT" },
                                label = { Text("Count ($repeatCount)") },
                                shape = RoundedCornerShape(AppRadius.sm)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    // Location Input
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(AppRadius.md),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    // Description Input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(AppRadius.md),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    // Participants Input
                    OutlinedTextField(
                        value = participants,
                        onValueChange = { participants = it },
                        label = { Text("Participants (Comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(AppRadius.md),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    // Web Link URL
                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        label = { Text("Link URL (e.g. https://...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(AppRadius.md),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    // Attachment Picker Button & Chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { filePickerLauncher.launch("*/*") },
                            shape = RoundedCornerShape(AppRadius.md)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(AppDimensions.iconSmall)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xs))
                            Text(if (attachmentUriStr == null) "Attach File" else "Change File")
                        }

                        if (attachmentUriStr != null) {
                            InputChip(
                                selected = true,
                                onClick = { attachmentUriStr = null },
                                label = { Text("Attached") },
                                trailingIcon = { Icon(Icons.Outlined.Close, contentDescription = "Remove") },
                                shape = RoundedCornerShape(AppRadius.sm)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    // Private Notes Input
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Private Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(AppRadius.md),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.StickyNote2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Bottom Buttons (Cancel / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(AppRadius.md)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "Please enter an event title", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val reminderStr = selectedReminders.ifEmpty { listOf(15) }.joinToString(",")

                            val eventToSave = (initialEvent ?: Event(
                                title = title,
                                startDate = startTimestamp,
                                endDate = endTimestamp
                            )).copy(
                                title = title,
                                description = description,
                                startDate = startTimestamp,
                                endDate = endTimestamp,
                                isAllDay = isAllDay,
                                location = location,
                                categoryId = selectedCategoryId,
                                colorHex = colorHex,
                                reminderMinutesList = reminderStr,
                                repeatType = repeatType,
                                repeatEndType = repeatEndType,
                                repeatUntilDate = repeatUntilDate,
                                repeatCount = repeatCount,
                                participants = participants,
                                attachmentUri = attachmentUriStr,
                                linkUrl = linkUrl,
                                notes = notes
                            )

                            viewModel.saveEvent(eventToSave, context)
                            Toast.makeText(context, if (initialEvent == null) "Event created!" else "Event updated!", Toast.LENGTH_SHORT).show()
                            onSaveSuccess()
                        },
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier.testTag("save_event_button")
                    ) {
                        Text("Save Event")
                    }
                }
            }
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startTimestamp)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { newMillis ->
                        val duration = endTimestamp - startTimestamp
                        startTimestamp = newMillis
                        endTimestamp = newMillis + duration
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = startTimestamp }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val duration = endTimestamp - startTimestamp
                    cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    cal.set(Calendar.MINUTE, timePickerState.minute)
                    startTimestamp = cal.timeInMillis
                    endTimestamp = startTimestamp + duration
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) },
            shape = RoundedCornerShape(AppRadius.dialog)
        )
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = endTimestamp }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    cal.set(Calendar.MINUTE, timePickerState.minute)
                    if (cal.timeInMillis > startTimestamp) {
                        endTimestamp = cal.timeInMillis
                    } else {
                        Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                    }
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) },
            shape = RoundedCornerShape(AppRadius.dialog)
        )
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF3F51B5)
    }
}

