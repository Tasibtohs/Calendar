package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.ui.components.*
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import com.example.util.NoteChecklistItem
import com.example.util.NoteUtils
import java.text.SimpleDateFormat
import java.util.*

private val NAVY_BLUE_ACCENT = Color(0xFF0D47A1)

val NOTE_PALETTE = listOf(
    "#0D47A1", // Navy Blue
    "#1565C0", // Royal Blue
    "#00695C", // Deep Teal
    "#2E7D32", // Forest Green
    "#C62828", // Crimson
    "#6A1B9A", // Deep Purple
    "#AD1457", // Deep Rose
    "#D84315", // Rust Orange
    "#37474F", // Slate Grey
    "#212121"  // Obsidian
)

enum class NoteLayoutMode {
    GRID,
    LIST
}

enum class NoteSortOrder(val label: String) {
    RECENT("সর্বশেষ আপডেট"),
    PINNED_FIRST("শীর্ষে পিন করা"),
    TITLE_AZ("শিরোনাম (A-Z)"),
    LONGEST("দীর্ঘতম নোট")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allNotes by viewModel.allNotes.collectAsState()
    val deletedNotes by viewModel.deletedNotes.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var isTrashViewActive by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var filterPinnedOnly by remember { mutableStateOf(false) }
    var filterChecklistOnly by remember { mutableStateOf(false) }
    var filterLockedOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf(NoteSortOrder.RECENT) }
    var layoutMode by remember { mutableStateOf(NoteLayoutMode.GRID) }

    var showCreateNoteScreen by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var noteToUnlock by remember { mutableStateOf<Note?>(null) }
    var noteToExport by remember { mutableStateOf<Note?>(null) }
    var noteToDeleteForever by remember { mutableStateOf<Note?>(null) }
    var showEmptyTrashConfirmDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val lastDeletedEntity by viewModel.lastDeletedEntity.collectAsState()

    LaunchedEffect(lastDeletedEntity) {
        if (lastDeletedEntity is CalendarViewModel.DeletedEntity.DeletedNote) {
            val result = snackbarHostState.showSnackbar(
                message = "নোট রিসাইকেল বিনে পাঠানো হয়েছে",
                actionLabel = "পূর্বাবস্থা (Undo)",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoLastDelete()
            } else {
                viewModel.clearLastDeleted()
            }
        }
    }

    // Dynamic Tags extracted from all active notes
    val allTags = remember(allNotes) {
        allNotes.flatMap { NoteUtils.parseTags(it.tags) }.distinct()
    }

    // Filtered Active Notes
    val filteredNotes = remember(
        allNotes, searchQuery, selectedCategoryId, selectedTag,
        filterPinnedOnly, filterChecklistOnly, filterLockedOnly, sortOrder
    ) {
        allNotes.filter { note ->
            val matchesSearch = searchQuery.isBlank() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    note.tags.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryId == null || note.categoryId == selectedCategoryId
            val matchesTag = selectedTag == null || NoteUtils.parseTags(note.tags).contains(selectedTag)
            val matchesPinned = !filterPinnedOnly || note.isPinned
            val matchesChecklist = !filterChecklistOnly || note.isChecklist
            val matchesLocked = !filterLockedOnly || note.isLocked

            matchesSearch && matchesCategory && matchesTag && matchesPinned && matchesChecklist && matchesLocked
        }.let { list ->
            when (sortOrder) {
                NoteSortOrder.RECENT -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
                NoteSortOrder.PINNED_FIRST -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
                NoteSortOrder.TITLE_AZ -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.title.lowercase(Locale.getDefault()) })
                NoteSortOrder.LONGEST -> list.sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.content.length })
            }
        }
    }

    val pinnedCount = remember(allNotes) { allNotes.count { it.isPinned } }
    val checklistCount = remember(allNotes) { allNotes.count { it.isChecklist } }

    Scaffold(
        modifier = modifier.testTag("notes_screen_scaffold"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!isTrashViewActive) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateNoteScreen = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.EditNote,
                            contentDescription = "New Note",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "নতুন নোট",
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
                        .testTag("fab_add_note")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Search Bar & Header Controls Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (isTrashViewActive) "রিসাইকেল বিন খুঁজুন..." else "নোট, চেকলিস্ট বা #ট্যাগ খুঁজুন...",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = NAVY_BLUE_ACCENT
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(AppRadius.md),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NAVY_BLUE_ACCENT,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_search_notes")
                    )

                    // Recycle Bin / Trash View Toggle
                    IconButton(
                        onClick = { isTrashViewActive = !isTrashViewActive },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(AppRadius.sm))
                            .background(if (isTrashViewActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                            .testTag("btn_toggle_trash_view")
                    ) {
                        BadgedBox(
                            badge = {
                                if (deletedNotes.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text("${deletedNotes.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isTrashViewActive) Icons.Filled.DeleteSweep else Icons.Outlined.DeleteOutline,
                                contentDescription = "Recycle Bin",
                                tint = if (isTrashViewActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Layout Mode Toggle Button (Grid vs List)
                    IconButton(
                        onClick = {
                            layoutMode = if (layoutMode == NoteLayoutMode.GRID) NoteLayoutMode.LIST else NoteLayoutMode.GRID
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(AppRadius.sm))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .testTag("btn_toggle_note_layout")
                    ) {
                        Icon(
                            imageVector = if (layoutMode == NoteLayoutMode.GRID) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                            contentDescription = if (layoutMode == NoteLayoutMode.GRID) "Switch to List View" else "Switch to Grid View",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Trash View Bar OR Filter Chips Bar
            if (isTrashViewActive) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(AppRadius.md),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "রিসাইকেল বিন (${deletedNotes.size}টি আইটেম)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        if (deletedNotes.isNotEmpty()) {
                            TextButton(
                                onClick = { showEmptyTrashConfirmDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ট্র্যাশ খালি করুন")
                            }
                        }
                    }
                }
            } else {
                // Horizontal Filter & Category & Tag Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xxs),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "All Notes" Filter Chip
                    FilterChip(
                        selected = selectedCategoryId == null && selectedTag == null && !filterPinnedOnly && !filterChecklistOnly && !filterLockedOnly,
                        onClick = {
                            selectedCategoryId = null
                            selectedTag = null
                            filterPinnedOnly = false
                            filterChecklistOnly = false
                            filterLockedOnly = false
                        },
                        label = {
                            Text(
                                text = "সব (${allNotes.size})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.StickyNote2,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(AppRadius.full)
                    )

                    // "Pinned" Filter Chip
                    if (pinnedCount > 0) {
                        FilterChip(
                            selected = filterPinnedOnly,
                            onClick = {
                                filterPinnedOnly = !filterPinnedOnly
                                if (filterPinnedOnly) selectedCategoryId = null
                            },
                            label = {
                                Text(
                                    text = "পিন ($pinnedCount)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = null,
                                    tint = if (filterPinnedOnly) MaterialTheme.colorScheme.primary else Color(0xFFFFB300),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(AppRadius.full)
                        )
                    }

                    // "Checklist" Filter Chip
                    if (checklistCount > 0) {
                        FilterChip(
                            selected = filterChecklistOnly,
                            onClick = {
                                filterChecklistOnly = !filterChecklistOnly
                            },
                            label = {
                                Text(
                                    text = "চেকলিস্ট ($checklistCount)",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CheckBox,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(AppRadius.full)
                        )
                    }

                    // Dynamic Categories Chips
                    categories.forEach { cat ->
                        val catNotesCount = remember(allNotes, cat.id) { allNotes.count { it.categoryId == cat.id } }
                        val isCatSelected = selectedCategoryId == cat.id
                        val catColor = parseHexColor(cat.colorHex)

                        FilterChip(
                            selected = isCatSelected,
                            onClick = {
                                selectedCategoryId = if (isCatSelected) null else cat.id
                                selectedTag = null
                                filterPinnedOnly = false
                            },
                            label = {
                                Text(
                                    text = "${cat.name} ($catNotesCount)",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                            },
                            shape = RoundedCornerShape(AppRadius.full)
                        )
                    }

                    // Dynamic Tags Chips
                    allTags.forEach { tag ->
                        val isTagSelected = selectedTag == tag
                        FilterChip(
                            selected = isTagSelected,
                            onClick = {
                                selectedTag = if (isTagSelected) null else tag
                                selectedCategoryId = null
                            },
                            label = {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Label,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            shape = RoundedCornerShape(AppRadius.full)
                        )
                    }
                }

                // 3. Status Summary & Sort Order Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xxs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "${filteredNotes.size}টি ফলাফল পাওয়া গেছে"
                        } else {
                            "${filteredNotes.size}টি নোট সংরক্ষিত"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick Sort Menu Selector
                    var sortMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(
                            onClick = { sortMenuExpanded = true },
                            contentPadding = PaddingValues(horizontal = AppSpacing.xs, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sort,
                                contentDescription = "Sort Notes",
                                tint = NAVY_BLUE_ACCENT,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sortOrder.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = NAVY_BLUE_ACCENT
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            NoteSortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = order.label,
                                            fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        if (sortOrder == order) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = NAVY_BLUE_ACCENT
                                            )
                                        }
                                    },
                                    onClick = {
                                        sortOrder = order
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxs))

            // 4. Main Notes / Trash Content
            if (isTrashViewActive) {
                if (deletedNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(AppSpacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        AppEmptyState(
                            icon = Icons.Outlined.DeleteOutline,
                            title = "রিসাইকেল বিন ফাঁকা",
                            subtitle = "মুছে ফেলা কোনো নোট বর্তমানে ট্র্যাশে নেই।"
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        contentPadding = PaddingValues(
                            start = AppSpacing.md,
                            end = AppSpacing.md,
                            top = AppSpacing.xs,
                            bottom = 96.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(deletedNotes, key = { it.id }) { note ->
                            TrashNoteCard(
                                note = note,
                                onRestore = {
                                    viewModel.restoreNote(note)
                                    Toast.makeText(context, "'${note.title}' পুনরুদ্ধার করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                onDeleteForever = {
                                    noteToDeleteForever = note
                                }
                            )
                        }
                    }
                }
            } else {
                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(AppSpacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (searchQuery.isNotBlank()) {
                            AppEmptyState(
                                icon = Icons.Outlined.SearchOff,
                                title = "কোনো নোট পাওয়া যায়নি",
                                subtitle = "'$searchQuery' সম্পর্কিত কোনো নোটের মিল নেই।",
                                actionText = "অনুসন্ধান মুছুন",
                                onActionClick = { searchQuery = "" }
                            )
                        } else {
                            AppEmptyState(
                                icon = Icons.Outlined.NoteAdd,
                                title = "আপনার নোটবুক ফাঁকা",
                                subtitle = "গুরুত্বপূর্ণ ভাবনা, আইডিয়া, ড্রয়িং বা চেকলিস্ট লিখে রাখতে নিচে 'নতুন নোট' চাপুন।",
                                actionText = "প্রথম নোট তৈরি করুন",
                                onActionClick = { showCreateNoteScreen = true }
                            )
                        }
                    }
                } else {
                    AnimatedContent(
                        targetState = layoutMode,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { mode ->
                        when (mode) {
                            NoteLayoutMode.GRID -> {
                                LazyVerticalStaggeredGrid(
                                    columns = StaggeredGridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                    verticalItemSpacing = AppSpacing.sm,
                                    contentPadding = PaddingValues(
                                        start = AppSpacing.md,
                                        end = AppSpacing.md,
                                        top = AppSpacing.xs,
                                        bottom = 96.dp
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredNotes, key = { it.id }) { note ->
                                        val category = categories.find { it.id == note.categoryId }
                                        PremiumNoteCard(
                                            note = note,
                                            category = category,
                                            isListView = false,
                                            onTogglePin = { viewModel.toggleNotePin(note) },
                                            onClick = {
                                                if (note.isLocked && note.pinCode.isNotBlank()) {
                                                    noteToUnlock = note
                                                } else {
                                                    noteToEdit = note
                                                }
                                            },
                                            onChecklistToggle = { itemIndex ->
                                                val items = NoteUtils.parseChecklist(note.checklistJson, note.content).toMutableList()
                                                if (itemIndex in items.indices) {
                                                    val item = items[itemIndex]
                                                    items[itemIndex] = item.copy(isDone = !item.isDone)
                                                    val updatedJson = NoteUtils.serializeChecklist(items)
                                                    val updatedContent = items.joinToString("\n") { (if (it.isDone) "[x] " else "[ ] ") + it.text }
                                                    viewModel.saveNote(
                                                        note.copy(
                                                            checklistJson = updatedJson,
                                                            content = updatedContent,
                                                            updatedAt = System.currentTimeMillis()
                                                        )
                                                    )
                                                }
                                            },
                                            onShareCard = { noteToExport = note },
                                            onCopy = { copyNoteToClipboard(context, note) },
                                            onDelete = { viewModel.deleteNote(note) }
                                        )
                                    }
                                }
                            }

                            NoteLayoutMode.LIST -> {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                    contentPadding = PaddingValues(
                                        start = AppSpacing.md,
                                        end = AppSpacing.md,
                                        top = AppSpacing.xs,
                                        bottom = 96.dp
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredNotes, key = { it.id }) { note ->
                                        val category = categories.find { it.id == note.categoryId }
                                        PremiumNoteCard(
                                            note = note,
                                            category = category,
                                            isListView = true,
                                            onTogglePin = { viewModel.toggleNotePin(note) },
                                            onClick = {
                                                if (note.isLocked && note.pinCode.isNotBlank()) {
                                                    noteToUnlock = note
                                                } else {
                                                    noteToEdit = note
                                                }
                                            },
                                            onChecklistToggle = { itemIndex ->
                                                val items = NoteUtils.parseChecklist(note.checklistJson, note.content).toMutableList()
                                                if (itemIndex in items.indices) {
                                                    val item = items[itemIndex]
                                                    items[itemIndex] = item.copy(isDone = !item.isDone)
                                                    val updatedJson = NoteUtils.serializeChecklist(items)
                                                    val updatedContent = items.joinToString("\n") { (if (it.isDone) "[x] " else "[ ] ") + it.text }
                                                    viewModel.saveNote(
                                                        note.copy(
                                                            checklistJson = updatedJson,
                                                            content = updatedContent,
                                                            updatedAt = System.currentTimeMillis()
                                                        )
                                                    )
                                                }
                                            },
                                            onShareCard = { noteToExport = note },
                                            onCopy = { copyNoteToClipboard(context, note) },
                                            onDelete = { viewModel.deleteNote(note) }
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

    // Full Editor Screen for Create
    if (showCreateNoteScreen) {
        NoteEditorScreen(
            categories = categories,
            onDismiss = { showCreateNoteScreen = false },
            onSave = { newNote ->
                viewModel.saveNote(newNote)
                showCreateNoteScreen = false
            }
        )
    }

    // Full Editor Screen for Edit
    noteToEdit?.let { note ->
        NoteEditorScreen(
            initialNote = note,
            categories = categories,
            onDismiss = { noteToEdit = null },
            onSave = { updated ->
                viewModel.saveNote(updated)
                noteToEdit = null
            },
            onDelete = {
                viewModel.deleteNote(it)
                noteToEdit = null
            }
        )
    }

    // PIN Unlock Dialog
    noteToUnlock?.let { lockedNote ->
        NotePinUnlockDialog(
            correctPin = lockedNote.pinCode,
            title = "'${lockedNote.title}' সুরক্ষিত নোট",
            onDismiss = { noteToUnlock = null },
            onUnlocked = {
                val target = noteToUnlock
                noteToUnlock = null
                noteToEdit = target
            }
        )
    }

    // Note Export & Share Card Dialog
    noteToExport?.let { note ->
        val cat = categories.find { it.id == note.categoryId }
        NoteExportCardDialog(
            note = note,
            category = cat,
            onDismiss = { noteToExport = null }
        )
    }

    // Delete Forever Confirmation
    noteToDeleteForever?.let { note ->
        AppDeleteDialog(
            onDismissRequest = { noteToDeleteForever = null },
            title = "স্থায়ীভাবে মুছুন",
            message = "আপনি কি নিশ্চিত যে '${note.title}' চিরতরে মুছে ফেলতে চান? এটি আর পুনরুদ্ধার করা সম্ভব হবে না।",
            confirmText = "চিরতরে মুছুন",
            dismissText = "বাতিল",
            onConfirm = {
                viewModel.deleteNotePermanently(note)
                noteToDeleteForever = null
                Toast.makeText(context, "নোট চিরতরে মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Empty Trash Confirmation
    if (showEmptyTrashConfirmDialog) {
        AppDeleteDialog(
            onDismissRequest = { showEmptyTrashConfirmDialog = false },
            title = "সব ট্র্যাশ খালি করুন",
            message = "রিসাইকেল বিনে থাকা সকল (${deletedNotes.size}টি) নোট স্থায়ীভাবে মুছে ফেলা হবে। আপনি কি নিশ্চিত?",
            confirmText = "ট্র্যাশ খালি করুন",
            dismissText = "বাতিল",
            onConfirm = {
                viewModel.emptyNoteTrash()
                showEmptyTrashConfirmDialog = false
                Toast.makeText(context, "রিসাইকেল বিন সম্পূর্ণ খালি করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun PremiumNoteCard(
    note: Note,
    category: Category?,
    isListView: Boolean,
    onTogglePin: () -> Unit,
    onClick: () -> Unit,
    onChecklistToggle: (Int) -> Unit,
    onShareCard: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val baseColor = parseHexColor(note.colorHex)
    val tagsList = remember(note.tags) { NoteUtils.parseTags(note.tags) }
    val checklistItems = remember(note.checklistJson, note.content) {
        if (note.isChecklist) NoteUtils.parseChecklist(note.checklistJson, note.content) else emptyList()
    }

    val gradientBrush = remember(baseColor) {
        Brush.verticalGradient(
            colors = listOf(
                baseColor,
                baseColor.copy(alpha = 0.88f)
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(AppRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = baseColor,
            contentColor = Color.White
        ),
        border = BorderStroke(
            width = if (note.isPinned) 1.5.dp else 1.dp,
            color = if (note.isPinned) Color(0xFFFFD54F).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (note.isPinned) 4.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md)
            ) {
                // Header: Title & Pin Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (note.isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Note",
                                tint = Color(0xFFFFCDD2),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = note.title.ifBlank { "শিরোনামহীন নোট" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.1).sp
                            ),
                            color = Color.White,
                            maxLines = if (isListView) 2 else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_pin_note_${note.id}")
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (note.isPinned) "Unpin note" else "Pin note",
                            tint = if (note.isPinned) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Locked Note Blur Placeholder vs Normal Content
                if (note.isLocked) {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Surface(
                        shape = RoundedCornerShape(AppRadius.sm),
                        color = Color.Black.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.Shield, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                            Text(
                                text = "সুরক্ষিত নোট • পিন দিয়ে আনলক করুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                } else if (note.isChecklist && checklistItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    val previewItems = checklistItems.take(if (isListView) 4 else 3)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        previewItems.forEachIndexed { idx, item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { onChecklistToggle(idx) }
                            ) {
                                Icon(
                                    imageVector = if (item.isDone) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (item.isDone) Color(0xFF81C784) else Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (item.isDone) Color.White.copy(alpha = 0.5f) else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (checklistItems.size > previewItems.size) {
                            Text(
                                text = "+ আরও ${checklistItems.size - previewItems.size}টি আইটেম...",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 19.sp),
                        color = Color.White.copy(alpha = 0.92f),
                        maxLines = if (isListView) 4 else 6,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Drawing Thumbnail Preview
                if (!note.drawingData.isNullOrBlank() && !note.isLocked) {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    NoteDrawingThumbnail(
                        drawingJson = note.drawingData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                // Badges Row: Category, Reminder, Tags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    category?.let { cat ->
                        Surface(
                            shape = RoundedCornerShape(AppRadius.xs),
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(cat.colorHex))
                                )
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    note.reminderTime?.let { time ->
                        Surface(
                            shape = RoundedCornerShape(AppRadius.xs),
                            color = Color(0xFF2E7D32).copy(alpha = 0.4f),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = Color(0xFFA5D6A7), modifier = Modifier.size(10.dp))
                                Text(
                                    text = CalendarUtils.formatDate(time, "dd MMM"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(0xFFA5D6A7)
                                )
                            }
                        }
                    }

                    tagsList.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(AppRadius.xs),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.xs))

                // Bottom Footer: Relative Time & Action Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatRelativeTime(note.updatedAt),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.65f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Share Card
                        IconButton(
                            onClick = onShareCard,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share card",
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Copy
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy note",
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Move to Trash
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Move to Trash",
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrashNoteCard(
    note: Note,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "শিরোনামহীন নোট" },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "মুছে ফেলা হয়েছে: ${note.deletedAt?.let { formatRelativeTime(it) } ?: "সম্প্রতি"}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                // Restore Button
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore Note",
                        tint = Color(0xFF2E7D32)
                    )
                }

                // Delete Forever Button
                IconButton(onClick = onDeleteForever) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Forever",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun copyNoteToClipboard(context: Context, note: Note) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val textToCopy = buildString {
            appendLine(note.title)
            if (note.content.isNotBlank()) {
                appendLine()
                append(note.content)
            }
        }
        val clip = ClipData.newPlainText("Note Content", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "নোট টেক্সট ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "কপি করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        NAVY_BLUE_ACCENT
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "এখনই"
        minutes < 60 -> "${minutes} মি. আগে"
        hours < 24 -> "${hours} ঘণ্টা আগে"
        days == 1L -> "গতকাল"
        days < 7 -> "${days} দিন আগে"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun NoteEditorDialog(
    initialNote: Note? = null,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit
) {
    NoteEditorScreen(
        initialNote = initialNote,
        categories = categories,
        onDismiss = onDismiss,
        onSave = onSave
    )
}
