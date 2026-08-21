package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils

sealed class SearchResultItem {
    data class EventItem(val event: Event) : SearchResultItem()
    data class TaskItem(val task: Task) : SearchResultItem()
    data class NoteItem(val note: Note) : SearchResultItem()
    data class BirthdayItem(val birthday: Birthday) : SearchResultItem()
    data class AnniversaryItem(val anniversary: Anniversary) : SearchResultItem()
    data class HolidayItem(val holiday: Holiday) : SearchResultItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchDialog(
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit,
    onSelectEvent: (Event) -> Unit
) {
    val allEvents by viewModel.allEvents.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val allBirthdays by viewModel.allBirthdays.collectAsState()
    val allAnniversaries by viewModel.allAnniversaries.collectAsState()
    val allHolidays by viewModel.allHolidays.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("All") } // "All", "Event", "Task", "Note", "Birthday", "Anniversary", "Holiday"
    var sortBy by remember { mutableStateOf("Date") } // "Date", "Alphabetical"

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(150)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }


    val results = remember(
        allEvents, allTasks, allNotes, allBirthdays, allAnniversaries, allHolidays,
        searchQuery, selectedTypeFilter, sortBy
    ) {
        if (searchQuery.isBlank()) return@remember emptyList<SearchResultItem>()

        val list = mutableListOf<SearchResultItem>()

        if (selectedTypeFilter == "All" || selectedTypeFilter == "Event") {
            allEvents.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.location.contains(searchQuery, ignoreCase = true)
            }.forEach { list.add(SearchResultItem.EventItem(it)) }
        }

        if (selectedTypeFilter == "All" || selectedTypeFilter == "Task") {
            allTasks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }.forEach { list.add(SearchResultItem.TaskItem(it)) }
        }

        if (selectedTypeFilter == "All" || selectedTypeFilter == "Note") {
            allNotes.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
            }.forEach { list.add(SearchResultItem.NoteItem(it)) }
        }

        if (selectedTypeFilter == "All" || selectedTypeFilter == "Birthday") {
            allBirthdays.filter {
                it.personName.contains(searchQuery, ignoreCase = true) ||
                        it.notes.contains(searchQuery, ignoreCase = true)
            }.forEach { list.add(SearchResultItem.BirthdayItem(it)) }
        }

        if (selectedTypeFilter == "All" || selectedTypeFilter == "Anniversary") {
            allAnniversaries.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.notes.contains(searchQuery, ignoreCase = true)
            }.forEach { list.add(SearchResultItem.AnniversaryItem(it)) }
        }

        if (selectedTypeFilter == "All" || selectedTypeFilter == "Holiday") {
            allHolidays.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }.forEach { list.add(SearchResultItem.HolidayItem(it)) }
        }

        when (sortBy) {
            "Alphabetical" -> list.sortBy { item ->
                when (item) {
                    is SearchResultItem.EventItem -> item.event.title
                    is SearchResultItem.TaskItem -> item.task.title
                    is SearchResultItem.NoteItem -> item.note.title
                    is SearchResultItem.BirthdayItem -> item.birthday.personName
                    is SearchResultItem.AnniversaryItem -> item.anniversary.title
                    is SearchResultItem.HolidayItem -> item.holiday.name
                }
            }
            else -> list.sortByDescending { item ->
                when (item) {
                    is SearchResultItem.EventItem -> item.event.startDate
                    is SearchResultItem.TaskItem -> item.task.dueDate ?: 0L
                    is SearchResultItem.NoteItem -> item.note.updatedAt
                    is SearchResultItem.BirthdayItem -> item.birthday.birthDate
                    is SearchResultItem.AnniversaryItem -> item.anniversary.date
                    is SearchResultItem.HolidayItem -> item.holiday.date
                }
            }
        }

        list
    }

    val typeFilters = listOf("All", "Event", "Task", "Note", "Birthday", "Anniversary", "Holiday")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.lg)
                .testTag("global_search_dialog"),
            shape = RoundedCornerShape(AppRadius.dialog),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            tonalElevation = AppElevation.dialog
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.lg)
            ) {
                // Top Search Bar Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search events, tasks, notes...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(AppDimensions.iconMedium)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(AppDimensions.minTouchTarget)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(AppDimensions.iconMedium)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .testTag("input_global_search"),
                        shape = RoundedCornerShape(AppRadius.full)
                    )

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(AppDimensions.iconMedium)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Scrollable Type Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    typeFilters.forEach { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = { selectedTypeFilter = type },
                            label = {
                                Text(
                                    text = type,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selectedTypeFilter == type) FontWeight.Bold else FontWeight.Normal)
                                )
                            },
                            shape = RoundedCornerShape(AppRadius.full),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Results list or Empty State
                if (searchQuery.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AppEmptyState(
                            icon = Icons.Outlined.Search,
                            title = "Search Calendar & Reminders",
                            subtitle = "খুঁজতে উপরে কীওয়ার্ড লিখুন"
                        )
                    }
                } else if (results.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AppEmptyState(
                            icon = Icons.Outlined.SearchOff,
                            title = "কোনো ফলাফল পাওয়া যায়নি",
                            subtitle = "\"$searchQuery\" এর সাথে মিলছে এমন কিছু নেই"
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppSpacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${results.size} results found",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(results) { item ->
                            SearchResultCard(
                                item = item,
                                onSelectEvent = {
                                    if (item is SearchResultItem.EventItem) {
                                        onSelectEvent(item.event)
                                        onDismiss()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    item: SearchResultItem,
    onSelectEvent: () -> Unit
) {
    val (badgeText, badgeIcon, badgeColor) = when (item) {
        is SearchResultItem.EventItem -> Triple("Event", "📅", MaterialTheme.colorScheme.primary)
        is SearchResultItem.TaskItem -> Triple("Task", "📋", Color(0xFF43A047))
        is SearchResultItem.NoteItem -> Triple("Note", "📝", Color(0xFF8E24AA))
        is SearchResultItem.BirthdayItem -> Triple("Birthday", "🎂", Color(0xFFD81B60))
        is SearchResultItem.AnniversaryItem -> Triple("Anniversary", "💍", Color(0xFFE53935))
        is SearchResultItem.HolidayItem -> Triple("Holiday", "🎉", Color(0xFFFB8C00))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectEvent() },
        shape = RoundedCornerShape(AppRadius.md),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = badgeColor.copy(alpha = 0.14f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(badgeIcon, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                val (title, subtitle) = when (item) {
                    is SearchResultItem.EventItem -> item.event.title to CalendarUtils.formatDate(item.event.startDate, "d MMMM yyyy, hh:mm a")
                    is SearchResultItem.TaskItem -> item.task.title to "Priority: ${item.task.priority} ${if (item.task.isCompleted) "• Completed" else ""}"
                    is SearchResultItem.NoteItem -> item.note.title to item.note.content.take(50)
                    is SearchResultItem.BirthdayItem -> item.birthday.personName to CalendarUtils.formatDate(item.birthday.birthDate, "d MMMM")
                    is SearchResultItem.AnniversaryItem -> item.anniversary.title to CalendarUtils.formatDate(item.anniversary.date, "d MMMM")
                    is SearchResultItem.HolidayItem -> item.holiday.name to "${item.holiday.type} • ${CalendarUtils.formatDate(item.holiday.date, "d MMMM")}"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        color = badgeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(AppRadius.xs)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
                        )
                    }
                }
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (item is SearchResultItem.EventItem) {
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(AppDimensions.iconSmall)
                )
            }
        }
    }
}

