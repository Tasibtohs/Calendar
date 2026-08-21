package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEntityDialog(
    addType: String, // "Event", "Reminder", "Task", "Note", "Birthday", "Anniversary"
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit
) {
    val selectedCal by viewModel.selectedDate.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var personName by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var noteColorHex by remember { mutableStateOf("#2D3748") }
    var isPinned by remember { mutableStateOf(false) }

    var eventDateMillis by remember { mutableLongStateOf(selectedCal.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = eventDateMillis)

    val iconVector = when (addType) {
        "Event" -> Icons.Outlined.Event
        "Reminder", "Task" -> Icons.Outlined.CheckCircle
        "Note" -> Icons.Outlined.Description
        "Birthday" -> Icons.Outlined.Cake
        "Anniversary" -> Icons.Outlined.Favorite
        else -> Icons.Outlined.AddCircle
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppRadius.dialog),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = addType,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.iconMedium)
                        )
                    }
                }
                Text(
                    text = "Add $addType",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                if (addType == "Birthday") {
                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text("Person Name *") },
                        singleLine = true,
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_person_name")
                    )
                } else {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (addType == "Note") "Note Title *" else "Title *") },
                        singleLine = true,
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_title")
                    )
                }

                if (addType == "Note") {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Content") },
                        minLines = 3,
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_note_content")
                    )

                    Surface(
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Pin Note to Top",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = isPinned,
                                onCheckedChange = { isPinned = it }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Notes") },
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_description")
                    )
                }

                if (addType != "Note") {
                    // Date Picker Trigger
                    Surface(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Date",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                Text(
                                    text = CalendarUtils.formatDate(eventDateMillis),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "Pick Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(AppDimensions.iconMedium)
                            )
                        }
                    }
                }

                if (addType == "Task" || addType == "Reminder") {
                    Text(
                        "Priority Level:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        listOf("Low", "Medium", "High").forEach { level ->
                            FilterChip(
                                selected = priority == level,
                                onClick = { priority = level },
                                label = { Text(level) },
                                shape = RoundedCornerShape(AppRadius.full),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = if (addType == "Birthday") personName else title
                    if (finalTitle.isNotBlank()) {
                        when (addType) {
                            "Event" -> viewModel.addEvent(
                                title = finalTitle,
                                description = description,
                                startDate = eventDateMillis,
                                endDate = eventDateMillis + 3600000,
                                isAllDay = false,
                                location = "",
                                categoryId = 1,
                                colorHex = "#3F51B5"
                            )
                            "Reminder", "Task" -> viewModel.addTask(
                                title = finalTitle,
                                description = description,
                                dueDate = eventDateMillis,
                                priority = priority
                            )
                            "Note" -> viewModel.addNote(
                                title = finalTitle,
                                content = description,
                                colorHex = noteColorHex,
                                isPinned = isPinned
                            )
                            "Birthday" -> viewModel.addBirthday(
                                personName = finalTitle,
                                birthDate = eventDateMillis,
                                notes = description,
                                avatarUri = null
                            )
                            "Anniversary" -> viewModel.addAnniversary(
                                title = finalTitle,
                                date = eventDateMillis,
                                notes = description
                            )
                        }
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(AppRadius.md),
                modifier = Modifier.testTag("dialog_save_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { eventDateMillis = it }
                        showDatePicker = false
                    },
                    shape = RoundedCornerShape(AppRadius.md)
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) { Text("Cancel") }
            },
            shape = RoundedCornerShape(AppRadius.dialog)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
