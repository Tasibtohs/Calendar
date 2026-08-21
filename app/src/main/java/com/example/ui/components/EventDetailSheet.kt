package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StickyNote2
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
import com.example.data.model.Event
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import com.example.util.ConflictDetector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(
    event: Event,
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit,
    onEditEvent: (Event) -> Unit
) {
    val context = LocalContext.current
    val allEvents by viewModel.allEvents.collectAsState()

    var showMoveDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Conflict detection for this event
    val conflicts = remember(event, allEvents) {
        ConflictDetector.findConflicts(allEvents, event.startDate, event.endDate, excludeEventId = event.id)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = AppRadius.sheet, topEnd = AppRadius.sheet),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outlineVariant
            )
        },
        modifier = Modifier.testTag("event_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg)
                .padding(bottom = AppSpacing.xl)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Category Color, Title & Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(event.colorHex))
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
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

            Spacer(modifier = Modifier.height(AppSpacing.md))

            // Schedule Conflict Warning Banner
            if (conflicts.isNotEmpty()) {
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
                            contentDescription = "Conflict",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Column {
                            Text(
                                text = "Schedule Conflict Detected",
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

            // Details Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppRadius.lg),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                tonalElevation = AppElevation.low
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    // Time & Date Row
                    DetailRow(
                        icon = Icons.Outlined.Schedule,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = CalendarUtils.formatDate(event.startDate, "EEEE, d MMMM yyyy"),
                        subtitle = if (event.isAllDay) "All Day Event" else "${CalendarUtils.formatTime(event.startDate)} - ${CalendarUtils.formatTime(event.endDate)}"
                    )

                    // Location Row
                    if (event.location.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Outlined.LocationOn,
                            iconTint = Color(0xFFE91E63),
                            title = event.location,
                            subtitle = "Tap to view on Google Maps",
                            onClick = {
                                openMaps(context, event.location)
                            }
                        )
                    }

                    // Reminders Row
                    val reminderStr = formatRemindersText(event.reminderMinutesList)
                    DetailRow(
                        icon = Icons.Outlined.NotificationsActive,
                        iconTint = Color(0xFFFF9800),
                        title = "Reminders",
                        subtitle = reminderStr
                    )

                    // Repeat Row
                    if (event.repeatType != "NONE") {
                        DetailRow(
                            icon = Icons.Outlined.Repeat,
                            iconTint = Color(0xFF4CAF50),
                            title = "Recurrence Rule",
                            subtitle = "Repeats ${event.repeatType.lowercase().capitalize()} • ${event.repeatEndType}"
                        )
                    }

                    // Description Row
                    if (event.description.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Outlined.Notes,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            title = "Description",
                            subtitle = event.description
                        )
                    }

                    // Participants Row
                    if (event.participants.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Outlined.People,
                            iconTint = Color(0xFF9C27B0),
                            title = "Participants",
                            subtitle = event.participants
                        )
                    }

                    // Link URL Row
                    if (event.linkUrl.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Outlined.Link,
                            iconTint = MaterialTheme.colorScheme.primary,
                            title = "Link",
                            subtitle = event.linkUrl,
                            onClick = {
                                openWebLink(context, event.linkUrl)
                            }
                        )
                    }

                    // Attachment Row
                    if (!event.attachmentUri.isNullOrBlank()) {
                        DetailRow(
                            icon = Icons.Outlined.AttachFile,
                            iconTint = Color(0xFF009688),
                            title = "Attachment",
                            subtitle = "Tap to open attachment",
                            onClick = {
                                openAttachment(context, event.attachmentUri)
                            }
                        )
                    }

                    // Private Notes
                    if (event.notes.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Outlined.StickyNote2,
                            iconTint = Color(0xFF795548),
                            title = "Private Notes",
                            subtitle = event.notes
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            // Action Buttons: Edit, Duplicate, Move, Share, Delete
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppRadius.lg),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.sm, horizontal = AppSpacing.xs),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ActionButton(
                        icon = Icons.Outlined.Edit,
                        label = "Edit",
                        onClick = {
                            onDismiss()
                            onEditEvent(event)
                        }
                    )

                    ActionButton(
                        icon = Icons.Outlined.ContentCopy,
                        label = "Duplicate",
                        onClick = {
                            viewModel.duplicateEvent(event, context)
                            Toast.makeText(context, "Event duplicated!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    ActionButton(
                        icon = Icons.Outlined.DriveFileMove,
                        label = "Move",
                        onClick = { showMoveDatePicker = true }
                    )

                    ActionButton(
                        icon = Icons.Outlined.Share,
                        label = "Share",
                        onClick = {
                            shareEventDetails(context, event)
                        }
                    )

                    ActionButton(
                        icon = Icons.Outlined.Delete,
                        label = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        onClick = { showDeleteConfirmDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))
        }
    }

    // Move Event Date Picker Dialog
    if (showMoveDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = event.startDate)
        DatePickerDialog(
            onDismissRequest = { showMoveDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { newDate ->
                        viewModel.moveEvent(event, newDate, context)
                        Toast.makeText(context, "Event moved successfully!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                    showMoveDatePicker = false
                }) { Text("Move") }
            },
            dismissButton = {
                TextButton(onClick = { showMoveDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete Options Dialog (This event, Following, Entire Series)
    if (showDeleteConfirmDialog) {
        val isRecurring = event.repeatType != "NONE" || event.parentEventId != null

        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(AppDimensions.iconMedium)
                            )
                        }
                    }
                    Text(
                        text = "Delete Event",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                if (isRecurring) {
                    Text(
                        text = "This is a recurring event. Which occurrences would you like to delete?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Are you sure you want to delete '${event.title}'? This will remove the event from your calendar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                if (isRecurring) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        Button(
                            onClick = {
                                viewModel.deleteEventWithMode(event, "THIS", context)
                                showDeleteConfirmDialog = false
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppRadius.md)
                        ) { Text("This Event Only") }

                        OutlinedButton(
                            onClick = {
                                viewModel.deleteEventWithMode(event, "FOLLOWING", context)
                                showDeleteConfirmDialog = false
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppRadius.md)
                        ) { Text("This & Following Events") }

                        Button(
                            onClick = {
                                viewModel.deleteEventWithMode(event, "SERIES", context)
                                showDeleteConfirmDialog = false
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppRadius.md)
                        ) { Text("Entire Recurring Series") }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.deleteEvent(event, context)
                            showDeleteConfirmDialog = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(AppRadius.md)
                    ) { Text("Delete Event") }
                }
            },
            dismissButton = {
                if (!isRecurring) {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = false },
                        shape = RoundedCornerShape(AppRadius.md)
                    ) { Text("Cancel") }
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.sm))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = AppSpacing.xs, horizontal = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(AppRadius.sm),
            color = iconTint.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(AppDimensions.iconMedium))
            }
        }
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(AppDimensions.iconMedium)
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .clickable { onClick() }
            .padding(AppSpacing.xs)
    ) {
        Surface(
            shape = RoundedCornerShape(AppRadius.md),
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(AppDimensions.iconMedium)
                )
            }
        }
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = color
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

private fun formatRemindersText(reminderStr: String): String {
    val offsets = reminderStr.split(",").mapNotNull { it.trim().toIntOrNull() }
    if (offsets.isEmpty()) return "None"

    return offsets.joinToString(", ") { mins ->
        when (mins) {
            0 -> "At event time"
            5 -> "5 mins before"
            10 -> "10 mins before"
            15 -> "15 mins before"
            30 -> "30 mins before"
            60 -> "1 hour before"
            120 -> "2 hours before"
            1440 -> "1 day before"
            2880 -> "2 days before"
            10080 -> "1 week before"
            else -> "$mins mins before"
        }
    }
}

private fun openMaps(context: Context, location: String) {
    try {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Location: $location", Toast.LENGTH_SHORT).show()
    }
}

private fun openWebLink(context: Context, url: String) {
    try {
        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open link: $url", Toast.LENGTH_SHORT).show()
    }
}

private fun openAttachment(context: Context, uriStr: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(uriStr), "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Attachment: $uriStr", Toast.LENGTH_SHORT).show()
    }
}

private fun shareEventDetails(context: Context, event: Event) {
    val shareText = """
        📅 ${event.title}
        🗓️ Date: ${CalendarUtils.formatDate(event.startDate, "EEEE, d MMMM yyyy")}
        ⏰ Time: ${if (event.isAllDay) "All Day" else "${CalendarUtils.formatTime(event.startDate)} - ${CalendarUtils.formatTime(event.endDate)}"}
        ${if (event.location.isNotBlank()) "📍 Location: ${event.location}" else ""}
        ${if (event.description.isNotBlank()) "📝 Description: ${event.description}" else ""}
        ${if (event.linkUrl.isNotBlank()) "🔗 Link: ${event.linkUrl}" else ""}
        
        Shared via Professional Calendar App
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Event")
    context.startActivity(shareIntent)
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

