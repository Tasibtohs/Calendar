package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarConverters
import com.example.util.CalendarUtils
import java.util.Calendar

@Composable
fun HomeSectionHeader(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    count: Int? = null,
    onAddClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.14f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (count != null && count > 0) {
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Surface(
                    shape = RoundedCornerShape(AppRadius.xs),
                    color = iconColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = iconColor,
                        modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
                    )
                }
            }
        }

        if (onAddClick != null) {
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddCircleOutline,
                    contentDescription = "Add $title",
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TodaysEventsCard(
    events: List<Event>,
    onDeleteEvent: (Event) -> Unit,
    onSelectEvent: ((Event) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
            .testTag("todays_events_card"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        tonalElevation = AppElevation.low
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            HomeSectionHeader(
                title = "Today's Events",
                icon = Icons.Outlined.Today,
                iconColor = MaterialTheme.colorScheme.primary,
                count = events.size
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            if (events.isEmpty()) {
                AppCompactEmptyState(
                    icon = Icons.Outlined.CalendarToday,
                    message = "আজ কোনো Event নেই 🌿"
                )
            } else {
                events.forEach { event ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xxs)
                            .then(if (onSelectEvent != null) Modifier.clickable { onSelectEvent(event) } else Modifier),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(AppSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(AppRadius.full))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.sm))
                                Column {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = CalendarUtils.formatTime(event.startDate),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onDeleteEvent(event) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete Event",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(AppDimensions.iconSmall)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingEventsCard(
    events: List<Event>,
    onSelectEvent: ((Event) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
            .testTag("upcoming_events_card"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        tonalElevation = AppElevation.low
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            HomeSectionHeader(
                title = "Upcoming Events",
                icon = Icons.Outlined.Event,
                iconColor = Color(0xFF00897B),
                count = events.size
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            if (events.isEmpty()) {
                AppCompactEmptyState(
                    icon = Icons.Outlined.Event,
                    message = "No upcoming events scheduled."
                )
            } else {
                events.take(5).forEachIndexed { idx, event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (onSelectEvent != null) Modifier.clickable { onSelectEvent(event) } else Modifier)
                            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.xxs))
                            Text(
                                text = CalendarUtils.formatDate(event.startDate, "d MMMM yyyy, hh:mm a"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (idx < events.take(5).lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = AppSpacing.xs),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TasksSectionCard(
    tasks: List<Task>,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onAddNewTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
            .testTag("tasks_section_card"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        tonalElevation = AppElevation.low
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            HomeSectionHeader(
                title = "Tasks",
                icon = Icons.Outlined.CheckCircle,
                iconColor = Color(0xFF43A047),
                count = tasks.count { !it.isCompleted },
                onAddClick = onAddNewTask
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            if (tasks.isEmpty()) {
                AppCompactEmptyState(
                    icon = Icons.Outlined.CheckCircle,
                    message = "এখনও কোনো Task নেই",
                    actionText = "+ Add",
                    onActionClick = onAddNewTask
                )
            } else {
                tasks.take(5).forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.xxs, vertical = AppSpacing.xxs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task) }
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                            if (task.dueDate != null) {
                                Text(
                                    text = "Due: ${CalendarUtils.formatDate(task.dueDate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { onDeleteTask(task) }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete Task",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(AppDimensions.iconSmall)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesSectionCard(
    notes: List<Note>,
    onTogglePin: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onAddNewNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
            .testTag("notes_section_card"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        tonalElevation = AppElevation.low
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            HomeSectionHeader(
                title = "Notes",
                icon = Icons.Outlined.StickyNote2,
                iconColor = Color(0xFF8E24AA),
                count = notes.size,
                onAddClick = onAddNewNote
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            if (notes.isEmpty()) {
                AppCompactEmptyState(
                    icon = Icons.Outlined.StickyNote2,
                    message = "কোনো Note নেই",
                    actionText = "+ Add",
                    onActionClick = onAddNewNote
                )
            } else {
                notes.take(4).forEach { note ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xxs),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(AppSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (note.isPinned) {
                                        Icon(
                                            imageVector = Icons.Filled.PushPin,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(AppSpacing.xxs))
                                    }
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (note.content.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                    Text(
                                        text = note.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteNote(note) }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete Note",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(AppDimensions.iconSmall)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BirthdaysCard(
    birthdays: List<Birthday>,
    onDeleteBirthday: (Birthday) -> Unit,
    onAddBirthday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
            .testTag("birthdays_card"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        tonalElevation = AppElevation.low
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            HomeSectionHeader(
                title = "Birthdays",
                icon = Icons.Outlined.Cake,
                iconColor = Color(0xFFD81B60),
                count = birthdays.size,
                onAddClick = onAddBirthday
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            if (birthdays.isEmpty()) {
                AppCompactEmptyState(
                    icon = Icons.Outlined.Cake,
                    message = "কোনো Birthday পাওয়া যায়নি",
                    actionText = "+ Add",
                    onActionClick = onAddBirthday
                )
            } else {
                birthdays.forEach { bday ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xxs),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFD81B60).copy(alpha = 0.14f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🎂", fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(AppSpacing.sm))
                                Column {
                                    Text(
                                        text = bday.personName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                                    ) {
                                        Surface(
                                            color = Color(0xFFD81B60).copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(AppRadius.xs)
                                        ) {
                                            Text(
                                                text = CalendarUtils.formatDate(bday.birthDate, "d MMMM"),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                color = Color(0xFFD81B60),
                                                modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
                                            )
                                        }
                                        if (bday.birthYear != null) {
                                            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                                            val age = currentYear - bday.birthYear
                                            if (age > 0) {
                                                Text(
                                                    text = "($age yrs)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            IconButton(
                                onClick = { onDeleteBirthday(bday) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete Birthday",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(AppDimensions.iconSmall)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnniversariesCard(
    anniversaries: List<Anniversary>,
    onDeleteAnniversary: (Anniversary) -> Unit,
    onAddAnniversary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
            .testTag("anniversaries_card"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        tonalElevation = AppElevation.low
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            HomeSectionHeader(
                title = "Anniversaries",
                icon = Icons.Outlined.Favorite,
                iconColor = Color(0xFFE53935),
                count = anniversaries.size,
                onAddClick = onAddAnniversary
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            if (anniversaries.isEmpty()) {
                AppCompactEmptyState(
                    icon = Icons.Outlined.Favorite,
                    message = "কোনো Anniversary নেই",
                    actionText = "+ Add",
                    onActionClick = onAddAnniversary
                )
            } else {
                anniversaries.forEach { ann ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xxs),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE53935).copy(alpha = 0.14f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("💍", fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(AppSpacing.sm))
                                Column {
                                    Text(
                                        text = ann.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                                    ) {
                                        Surface(
                                            color = Color(0xFFE53935).copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(AppRadius.xs)
                                        ) {
                                            Text(
                                                text = CalendarUtils.formatDate(ann.date, "d MMMM"),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                color = Color(0xFFE53935),
                                                modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
                                            )
                                        }
                                        if (ann.year != null) {
                                            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                                            val years = currentYear - ann.year
                                            if (years > 0) {
                                                Text(
                                                    text = "($years yrs)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            IconButton(
                                onClick = { onDeleteAnniversary(ann) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete Anniversary",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(AppDimensions.iconSmall)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HolidaysCard(
    holidays: List<Holiday>,
    onSelectHoliday: ((Holiday) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedHolidayForDetail by remember { mutableStateOf<Holiday?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("সবগুলো") }
    var expandedHolidayId by remember { mutableStateOf<Long?>(null) }

    val categories = listOf("সবগুলো", "জাতীয় দিবস", "ইসলামিক ছুটি", "সরকারি ছুটি", "আন্তর্জাতিক")

    val filteredHolidays = remember(holidays, selectedCategoryFilter) {
        val sorted = holidays.sortedBy { it.date }
        when (selectedCategoryFilter) {
            "জাতীয় দিবস" -> sorted.filter { it.type.contains("National", ignoreCase = true) || it.type.contains("জাতীয়", ignoreCase = true) }
            "ইসলামিক ছুটি" -> sorted.filter { it.type.contains("Islamic", ignoreCase = true) || it.type.contains("Hijri", ignoreCase = true) || it.calendarType.equals("Hijri", ignoreCase = true) }
            "সরকারি ছুটি" -> sorted.filter { it.type.contains("Public", ignoreCase = true) || it.type.contains("Bangladesh", ignoreCase = true) }
            "আন্তর্জাতিক" -> sorted.filter { it.type.contains("International", ignoreCase = true) }
            else -> sorted
        }
    }

    val todayMidnight = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
            .testTag("holidays_card"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        tonalElevation = AppElevation.low
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            // Header
            HomeSectionHeader(
                title = "বাৎসরিক ছুটির তালিকা",
                icon = Icons.Outlined.Celebration,
                iconColor = Color(0xFFFB8C00),
                count = holidays.size
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            Text(
                text = "পুরো বছরের সকল সরকারি, জাতীয় ও ধর্মীয় ছুটির তালিকা:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Category Filter Chips (Horizontal scroll for responsiveness)
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                items(categories.size) { idx ->
                    val category = categories[idx]
                    val isSelected = selectedCategoryFilter == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = category },
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFB8C00).copy(alpha = 0.18f),
                            selectedLabelColor = Color(0xFFE65100)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(0xFFFB8C00) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(AppRadius.full)
                    )
                }
            }

            if (filteredHolidays.isEmpty()) {
                AppCompactEmptyState(
                    icon = Icons.Outlined.Celebration,
                    message = "এই বিভাগে কোনো ছুটি পাওয়া যায়নি"
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    filteredHolidays.forEach { holiday ->
                        val holidayCal = remember(holiday.date) {
                            Calendar.getInstance().apply { timeInMillis = holiday.date }
                        }
                        val banglaDate = remember(holidayCal) {
                            CalendarConverters.getBanglaDate(holidayCal)
                        }
                        val hijriDate = remember(holidayCal) {
                            CalendarConverters.getHijriDate(holidayCal)
                        }

                        val daysDiff = ((holiday.date - todayMidnight) / (1000 * 60 * 60 * 24)).toInt()
                        val isToday = daysDiff == 0
                        val isExpanded = expandedHolidayId == holiday.id

                        val badgeColor = when {
                            holiday.type.contains("National", ignoreCase = true) -> Color(0xFF2E7D32)
                            holiday.type.contains("Islamic", ignoreCase = true) -> Color(0xFF00897B)
                            holiday.type.contains("International", ignoreCase = true) -> Color(0xFF1976D2)
                            else -> Color(0xFFFB8C00)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (onSelectHoliday != null) {
                                        onSelectHoliday(holiday)
                                    } else {
                                        selectedHolidayForDetail = holiday
                                    }
                                },
                            shape = RoundedCornerShape(AppRadius.md),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (isToday) Color(0xFFFB8C00) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                            ) {
                                // Top Row: Date Box + Title & Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Left Date Badge
                                    Surface(
                                        shape = RoundedCornerShape(AppRadius.sm),
                                        color = badgeColor.copy(alpha = 0.12f),
                                        border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.3f)),
                                        modifier = Modifier.width(54.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = CalendarUtils.formatDate(holiday.date, "MMM").uppercase(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.5.sp
                                                ),
                                                color = badgeColor
                                            )
                                            Text(
                                                text = CalendarUtils.formatDate(holiday.date, "d"),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 16.sp
                                                ),
                                                color = badgeColor
                                            )
                                            Text(
                                                text = CalendarUtils.formatDate(holiday.date, "EEE"),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(AppSpacing.sm))

                                    // Title & Category Header
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Category Pill
                                            Surface(
                                                color = badgeColor.copy(alpha = 0.14f),
                                                shape = RoundedCornerShape(AppRadius.full)
                                            ) {
                                                Text(
                                                    text = when {
                                                        holiday.type.contains("National", ignoreCase = true) -> "🇧🇩 জাতীয় দিবস"
                                                        holiday.type.contains("Islamic", ignoreCase = true) -> "🌙 ইসলামিক ছুটি"
                                                        holiday.type.contains("International", ignoreCase = true) -> "🌍 আন্তর্জাতিক"
                                                        else -> "🏛️ সরকারি ছুটি"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 9.5.sp
                                                    ),
                                                    color = badgeColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            // Timing status
                                            Text(
                                                text = when {
                                                    isToday -> "🎉 আজ"
                                                    daysDiff in 1..30 -> "আর $daysDiff দিন বাকি"
                                                    daysDiff > 30 -> "$daysDiff দিন পর"
                                                    else -> "অতিক্রান্ত"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.5.sp,
                                                    fontWeight = if (isToday || (daysDiff in 1..30)) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = when {
                                                    isToday -> Color(0xFFFB8C00)
                                                    daysDiff in 1..30 -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.outline
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Main Bengali Holiday Name
                                        Text(
                                            text = holiday.nameBn.ifBlank { holiday.name },
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        // English Name (if available)
                                        if (holiday.nameBn.isNotBlank() && holiday.name.isNotBlank() && holiday.name != holiday.nameBn) {
                                            Text(
                                                text = holiday.name,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                // Tri-calendar Date Info Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                                ) {
                                    Text(
                                        text = "📅 ${CalendarUtils.formatDate(holiday.date, "EEEE, d MMM")}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    Text(
                                        text = "🌾 ${CalendarUtils.toBanglaDigit(banglaDate.day)} ${banglaDate.monthNameBn}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp
                                        ),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    Text(
                                        text = "🌙 ${CalendarUtils.toBanglaDigit(hijriDate.day)} ${hijriDate.monthNameBn}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.sp
                                        ),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }

                                // Collapsible Significance Box
                                val reasonText = holiday.description.ifBlank {
                                    when {
                                        holiday.type.contains("National", ignoreCase = true) -> "বাংলাদেশের জাতীয় ও ঐতিহাসিক তাৎপর্যমণ্ডিত ছুটির দিন।"
                                        holiday.type.contains("Islamic", ignoreCase = true) -> "মুসলিম উম্মাহর ধর্মীয় ও পবিত্র মহিমান্বিত উৎসব ও ইবাদতের দিন।"
                                        holiday.type.contains("International", ignoreCase = true) -> "আন্তর্জাতিকভাবে স্বীকৃত ও পালিত ছুটির দিন।"
                                        else -> "গণপ্রজাতন্ত্রী বাংলাদেশ সরকারের নির্বাহী আদেশে বাৎসরিক সরকারি ছুটি।"
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(AppRadius.xs))
                                        .clickable {
                                            expandedHolidayId = if (isExpanded) null else holiday.id
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("📖", fontSize = 11.sp)
                                        Text(
                                            text = "ছুটির কারণ ও তাৎপর্য",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                                    Surface(
                                        shape = RoundedCornerShape(AppRadius.xs),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = reasonText,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 5.dp)
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

    selectedHolidayForDetail?.let { holiday ->
        HolidayDetailDialog(
            holiday = holiday,
            onDismiss = { selectedHolidayForDetail = null }
        )
    }
}

@Composable
fun HolidayDetailDialog(
    holiday: Holiday,
    onDismiss: () -> Unit
) {
    val holidayCal = remember(holiday.date) {
        Calendar.getInstance().apply { timeInMillis = holiday.date }
    }
    val banglaDate = remember(holidayCal) {
        CalendarConverters.getBanglaDate(holidayCal)
    }
    val hijriDate = remember(holidayCal) {
        CalendarConverters.getHijriDate(holidayCal)
    }

    val todayMidnight = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val daysDiff = ((holiday.date - todayMidnight) / (1000 * 60 * 60 * 24)).toInt()

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
                    color = Color(0xFFFB8C00).copy(alpha = 0.14f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎉", fontSize = 22.sp)
                    }
                }
                Column {
                    Text(
                        text = holiday.nameBn.ifBlank { holiday.name },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (holiday.nameBn.isNotBlank() && holiday.name.isNotBlank()) {
                        Text(
                            text = holiday.name,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                // Countdown Badge
                Surface(
                    shape = RoundedCornerShape(AppRadius.sm),
                    color = when {
                        daysDiff == 0 -> Color(0xFFFB8C00).copy(alpha = 0.15f)
                        daysDiff > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when {
                                daysDiff == 0 -> "🎉 আজ এই ছুটির দিনটি উদযাপিত হচ্ছে!"
                                daysDiff in 1..30 -> "⏳ আর মাত্র $daysDiff দিন বাকি আছে"
                                daysDiff > 30 -> "⏳ আর $daysDiff দিন বাকি আছে"
                                else -> "✓ এই ছুটির দিনটি চলতি বছরে ইতিমধ্যে অতিক্রান্ত হয়েছে"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = when {
                                daysDiff == 0 -> Color(0xFFE65100)
                                daysDiff > 0 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Dates across 3 Calendars
                Surface(
                    shape = RoundedCornerShape(AppRadius.md),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        Text(
                            text = "তারিখ ও বার (Dates):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📅 ", fontSize = 13.sp)
                            Text(
                                text = "ইংরেজি: ${CalendarUtils.formatDate(holiday.date, "EEEE, d MMMM yyyy")}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌾 ", fontSize = 13.sp)
                            Text(
                                text = "বাংলা: ${CalendarUtils.toBanglaDigit(banglaDate.day)} ${banglaDate.monthNameBn} ${CalendarUtils.toBanglaDigit(banglaDate.year)} বঙ্গাব্দ",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌙 ", fontSize = 13.sp)
                            Text(
                                text = "হিজরী: ${CalendarUtils.toBanglaDigit(hijriDate.day)} ${hijriDate.monthNameBn} ${CalendarUtils.toBanglaDigit(hijriDate.year)} হিজরি",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                // Reason & Historical Significance
                Surface(
                    shape = RoundedCornerShape(AppRadius.md),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.md)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📖", fontSize = 15.sp)
                            Text(
                                text = "ছুটির কারণ ও ঐতিহাসিক পটভূমি:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = holiday.description.ifBlank {
                                "গণপ্রজাতন্ত্রী বাংলাদেশ সরকারের বার্ষিক ছুটির ক্যালেন্ডার অনুযায়ী রাষ্ট্রীয় ও ধর্মীয় মর্যাদায় দিনটি উদযাপিত হয়।"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Type & Category
                Surface(
                    shape = RoundedCornerShape(AppRadius.md),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ছুটির ধরন:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            color = Color(0xFFFB8C00).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(AppRadius.xs)
                        ) {
                            Text(
                                text = holiday.type,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFFB8C00),
                                modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Text("বন্ধ করুন (Close)")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidaysListDialog(
    holidays: List<Holiday>,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("সবগুলো") }
    var selectedDetailHoliday by remember { mutableStateOf<Holiday?>(null) }
    var expandedHolidayId by remember { mutableStateOf<Long?>(null) }

    val categories = listOf("সবগুলো", "জাতীয় দিবস", "ইসলামিক ছুটি", "সরকারি ছুটি", "আন্তর্জাতিক")

    val filteredList = remember(holidays, searchQuery, selectedCategory) {
        val sorted = holidays.sortedBy { it.date }
        val categoryFiltered = when (selectedCategory) {
            "জাতীয় দিবস" -> sorted.filter { it.type.contains("National", ignoreCase = true) || it.type.contains("জাতীয়", ignoreCase = true) }
            "ইসলামিক ছুটি" -> sorted.filter { it.type.contains("Islamic", ignoreCase = true) || it.type.contains("Hijri", ignoreCase = true) || it.calendarType.equals("Hijri", ignoreCase = true) }
            "সরকারি ছুটি" -> sorted.filter { it.type.contains("Public", ignoreCase = true) || it.type.contains("Bangladesh", ignoreCase = true) }
            "আন্তর্জাতিক" -> sorted.filter { it.type.contains("International", ignoreCase = true) }
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

    val todayMidnight = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm)
            .testTag("holidays_list_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFB8C00).copy(alpha = 0.18f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🎉", fontSize = 18.sp)
                        }
                    }
                    Text(
                        text = "বাৎসরিক ছুটির তালিকা",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(AppRadius.full),
                    color = Color(0xFFFB8C00).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${filteredList.size} টি ছুটি",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ছুটি খুঁজুন (Search holidays)...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(AppRadius.md),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AppSpacing.xs)
                )

                // Category Chips Row
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    modifier = Modifier.padding(bottom = AppSpacing.sm)
                ) {
                    items(categories.size) { idx ->
                        val cat = categories[idx]
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFB8C00).copy(alpha = 0.18f),
                                selectedLabelColor = Color(0xFFE65100)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFFFB8C00) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(AppRadius.full)
                        )
                    }
                }

                // Holidays List
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো ছুটি পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        items(filteredList.size) { idx ->
                            val holiday = filteredList[idx]
                            val holidayCal = Calendar.getInstance().apply { timeInMillis = holiday.date }
                            val daysDiff = ((holidayCal.timeInMillis - todayMidnight) / (1000 * 60 * 60 * 24)).toInt()
                            val isToday = daysDiff == 0
                            val isExpanded = expandedHolidayId == holiday.id
                            val banglaDate = CalendarUtils.getBanglaDate(holidayCal)
                            val hijriDate = CalendarUtils.getHijriDate(holidayCal)

                            val badgeColor = when {
                                holiday.type.contains("National", ignoreCase = true) -> Color(0xFF2E7D32)
                                holiday.type.contains("Islamic", ignoreCase = true) -> Color(0xFF00897B)
                                holiday.type.contains("International", ignoreCase = true) -> Color(0xFF1565C0)
                                else -> Color(0xFFFB8C00)
                            }

                            Surface(
                                shape = RoundedCornerShape(AppRadius.md),
                                color = if (isToday) Color(0xFFFB8C00).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(
                                    if (isToday) 1.2.dp else 0.8.dp,
                                    if (isToday) Color(0xFFFB8C00).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDetailHoliday = holiday }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = badgeColor.copy(alpha = 0.14f),
                                            shape = RoundedCornerShape(AppRadius.full)
                                        ) {
                                            Text(
                                                text = when {
                                                    holiday.type.contains("National", ignoreCase = true) -> "🇧🇩 জাতীয় দিবস"
                                                    holiday.type.contains("Islamic", ignoreCase = true) -> "🌙 ইসলামিক"
                                                    holiday.type.contains("International", ignoreCase = true) -> "🌍 আন্তর্জাতিক"
                                                    else -> "🏛️ সরকারি ছুটি"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.5.sp
                                                ),
                                                color = badgeColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = when {
                                                isToday -> "🎉 আজ ছুটি!"
                                                daysDiff in 1..30 -> "আর $daysDiff দিন বাকি"
                                                daysDiff > 30 -> "$daysDiff দিন পর"
                                                else -> "অতিক্রান্ত"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isToday || daysDiff in 1..30) Color(0xFFE65100) else MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = holiday.nameBn.ifBlank { holiday.name },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (holiday.nameBn.isNotBlank() && holiday.name.isNotBlank() && holiday.name != holiday.nameBn) {
                                        Text(
                                            text = holiday.name,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                                    ) {
                                        Text(
                                            text = "📅 ${CalendarUtils.formatDate(holiday.date, "d MMM, EEE")}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("•", fontSize = 8.sp, color = MaterialTheme.colorScheme.outlineVariant)
                                        Text(
                                            text = "🌾 ${CalendarUtils.toBanglaDigit(banglaDate.day)} ${banglaDate.monthNameBn}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text("•", fontSize = 8.sp, color = MaterialTheme.colorScheme.outlineVariant)
                                        Text(
                                            text = "🌙 ${CalendarUtils.toBanglaDigit(hijriDate.day)} ${hijriDate.monthNameEn}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }

                                    val reasonText = holiday.description.ifBlank {
                                        when {
                                            holiday.type.contains("National", ignoreCase = true) -> "বাংলাদেশের জাতীয় ও ঐতিহাসিক তাৎপর্যমণ্ডিত ছুটির দিন।"
                                            holiday.type.contains("Islamic", ignoreCase = true) -> "মুসলিম উম্মাহর ধর্মীয় ও পবিত্র মহিমান্বিত উৎসব ও ইবাদতের দিন।"
                                            holiday.type.contains("International", ignoreCase = true) -> "আন্তর্জাতিকভাবে স্বীকৃত ও পালিত ছুটির দিন।"
                                            else -> "গণপ্রজাতন্ত্রী বাংলাদেশ সরকারের নির্বাহী আদেশে বাৎসরিক সরকারি ছুটি।"
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(AppRadius.xs))
                                            .clickable {
                                                expandedHolidayId = if (isExpanded) null else holiday.id
                                            }
                                            .padding(vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text("📖", fontSize = 10.sp)
                                            Text(
                                                text = "ছুটির তাৎপর্য",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 10.sp
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                                        Surface(
                                            shape = RoundedCornerShape(AppRadius.xs),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = reasonText,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 10.5.sp,
                                                    lineHeight = 14.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Text("বন্ধ করুন (Close)")
            }
        }
    )

    selectedDetailHoliday?.let { h ->
        HolidayDetailDialog(holiday = h, onDismiss = { selectedDetailHoliday = null })
    }
}

