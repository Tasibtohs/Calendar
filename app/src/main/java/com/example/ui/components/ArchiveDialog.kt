package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Event
import com.example.data.model.Task
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.BackupUtils
import com.example.util.CalendarUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveExportScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val archivedEvents by viewModel.archivedEvents.collectAsState()
    val archivedTasks by viewModel.archivedTasks.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val allBirthdays by viewModel.allBirthdays.collectAsState()
    val allAnniversaries by viewModel.allAnniversaries.collectAsState()
    val allHolidays by viewModel.allHolidays.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val allCountdowns by viewModel.allCountdowns.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Events, 1: Tasks, 2: Export
    var searchQuery by remember { mutableStateOf("") }
    var eventToDelete by remember { mutableStateOf<Event?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    val filteredArchivedEvents = remember(archivedEvents, searchQuery) {
        if (searchQuery.isBlank()) archivedEvents
        else archivedEvents.filter { it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
    }

    val filteredArchivedTasks = remember(archivedTasks, searchQuery) {
        if (searchQuery.isBlank()) archivedTasks
        else archivedTasks.filter { it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("archive_export_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(AppSpacing.xs))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "আর্কাইভ ও ডাটা এক্সপোর্ট",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "পুরোনো আইটেম পুনরুদ্ধার ও ব্যাকআপ ফাইল এক্সপোর্ট",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = AppSpacing.xs)
                        ) {
                            Text(
                                text = "${archivedEvents.size + archivedTasks.size} টি আর্কাইভ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 4.dp)
                            )
                        }
                    }

                    // Tab Row
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("ইভেন্ট (${archivedEvents.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            icon = { Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("টাস্ক (${archivedTasks.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("ডাটা এক্সপোর্ট", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            icon = { Icon(Icons.Outlined.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }

                    // Search field for tab 0 & 1
                    if (selectedTab < 2) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("আর্কাইভ থেকে খুঁজুন...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                            shape = RoundedCornerShape(AppRadius.full),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // Archived Events List
                    if (filteredArchivedEvents.isEmpty()) {
                        AppEmptyState(
                            icon = Icons.Outlined.Archive,
                            title = if (searchQuery.isNotBlank()) "কোনো ফলাফল মেলেনি" else "কোনো আর্কাইভড ইভেন্ট নেই",
                            subtitle = "আর্কাইভ করা ইভেন্টসমূহ এখানে প্রদর্শিত হবে এবং আপনি সহজেই পুনরুদ্ধার করতে পারবেন।",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(AppSpacing.xl)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            items(filteredArchivedEvents, key = { it.id }) { event ->
                                Surface(
                                    shape = RoundedCornerShape(AppRadius.lg),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(AppSpacing.md)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = event.title,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                            Text(
                                                text = CalendarUtils.formatDate(event.startDate, "EEEE, d MMMM yyyy, h:mm a"),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (event.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                                Text(
                                                    text = event.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                            FilledTonalIconButton(
                                                onClick = {
                                                    viewModel.unarchiveEvent(event)
                                                    Toast.makeText(context, "ইভেন্ট পুনরুদ্ধার করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Unarchive,
                                                    contentDescription = "Restore",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { eventToDelete = event },
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Archived Tasks List
                    if (filteredArchivedTasks.isEmpty()) {
                        AppEmptyState(
                            icon = Icons.Outlined.Archive,
                            title = if (searchQuery.isNotBlank()) "কোনো ফলাফল মেলেনি" else "কোনো আর্কাইভড টাস্ক নেই",
                            subtitle = "আর্কাইভ করা টাস্কসমূহ এখানে সংরক্ষিত থাকবে এবং প্রয়োজনে রিস্টোর করতে পারবেন।",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(AppSpacing.xl)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            items(filteredArchivedTasks, key = { it.id }) { task ->
                                Surface(
                                    shape = RoundedCornerShape(AppRadius.lg),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(AppSpacing.md)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                            Text(
                                                text = task.dueDate?.let { "তারিখ: ${CalendarUtils.formatDate(it, "d MMM yyyy")}" } ?: "নির্দিষ্ট তারিখ নেই",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (task.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                                Text(
                                                    text = task.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                            FilledTonalIconButton(
                                                onClick = {
                                                    viewModel.unarchiveTask(task)
                                                    Toast.makeText(context, "টাস্ক পুনরুদ্ধার করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Unarchive,
                                                    contentDescription = "Restore",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { taskToDelete = task },
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Export Section
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        Text(
                            text = "ডাটা এক্সপোর্ট অপশন সমূহ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        // 1. JSON Full Backup Export Card
                        Surface(
                            shape = RoundedCornerShape(AppRadius.lg),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(AppSpacing.md),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Outlined.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                                    Column {
                                        Text("সম্পূর্ণ ডাটাবেজ ব্যাকআপ (JSON Backup)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("ইভেন্ট, টাস্ক, নোট, ক্যাটাগরি, জন্মদিন ও ছুটির তালিকা", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val json = BackupUtils.createFullBackupJson(
                                                    events = allEvents,
                                                    tasks = allTasks,
                                                    notes = allNotes,
                                                    birthdays = allBirthdays,
                                                    anniversaries = allAnniversaries,
                                                    holidays = allHolidays,
                                                    categories = allCategories,
                                                    countdowns = allCountdowns,
                                                    settings = emptyMap()
                                                )
                                                val clip = ClipData.newPlainText("Calendar JSON Backup", json)
                                                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                                Toast.makeText(context, "ব্যাকআপ ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("কপি করুন")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                val json = BackupUtils.createFullBackupJson(
                                                    events = allEvents,
                                                    tasks = allTasks,
                                                    notes = allNotes,
                                                    birthdays = allBirthdays,
                                                    anniversaries = allAnniversaries,
                                                    holidays = allHolidays,
                                                    categories = allCategories,
                                                    countdowns = allCountdowns,
                                                    settings = emptyMap()
                                                )
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, json)
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "ব্যাকআপ ফাইল শেয়ার করুন"))
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("শেয়ার")
                                    }
                                }
                            }
                        }

                        // 2. Plain Text Agenda Summary Export Card
                        Surface(
                            shape = RoundedCornerShape(AppRadius.lg),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(AppSpacing.md),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Outlined.Summarize, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                                    Column {
                                        Text("টেক্সট সামারি ও এজেন্ডা (Plain Text Agenda)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("সহজে মেসেঞ্জার, হোয়াটসঅ্যাপ বা ইমেইলে পাঠানোর উপযোগী", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                                ) {
                                    Button(
                                        onClick = {
                                            val summary = buildString {
                                                appendLine("📅 ক্যালেন্ডার ইভেন্ট ও টাস্ক সামারি")
                                                appendLine("================================")
                                                appendLine("\n📌 ইভেন্টসমূহ:")
                                                allEvents.filter { !it.isArchived }.forEach { ev ->
                                                    appendLine("• ${ev.title} (${CalendarUtils.formatDate(ev.startDate, "d MMM yyyy, h:mm a")})")
                                                }
                                                appendLine("\n✅ টাস্কসমূহ:")
                                                allTasks.filter { !it.isArchived }.forEach { t ->
                                                    val status = if (t.isCompleted) "[সম্পন্ন]" else "[বাকি]"
                                                    appendLine("• $status ${t.title}")
                                                }
                                            }
                                            val clip = ClipData.newPlainText("Calendar Summary", summary)
                                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                            Toast.makeText(context, "সামারি ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("কপি করুন")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val summary = buildString {
                                                appendLine("📅 ক্যালেন্ডার ইভেন্ট ও টাস্ক সামারি")
                                                appendLine("================================")
                                                appendLine("\n📌 ইভেন্টসমূহ:")
                                                allEvents.filter { !it.isArchived }.forEach { ev ->
                                                    appendLine("• ${ev.title} (${CalendarUtils.formatDate(ev.startDate, "d MMM yyyy, h:mm a")})")
                                                }
                                                appendLine("\n✅ টাস্কসমূহ:")
                                                allTasks.filter { !it.isArchived }.forEach { t ->
                                                    val status = if (t.isCompleted) "[সম্পন্ন]" else "[বাকি]"
                                                    appendLine("• $status ${t.title}")
                                                }
                                            }
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, summary)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "সামারি শেয়ার করুন"))
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("শেয়ার")
                                    }
                                }
                            }
                        }

                        // 3. ICS iCalendar File Export Card
                        Surface(
                            shape = RoundedCornerShape(AppRadius.lg),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(AppSpacing.md),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(22.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                                    Column {
                                        Text("iCalendar ফাইল (ICS Export)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("Google Calendar, Outlook বা Apple Calendar এ ইম্পোর্টযোগ্য", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                                ) {
                                    Button(
                                        onClick = {
                                            val ics = BackupUtils.exportEventsToIcs(allEvents)
                                            val clip = ClipData.newPlainText("Calendar ICS", ics)
                                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                            Toast.makeText(context, "ICS টেক্সট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("কপি ICS")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val ics = BackupUtils.exportEventsToIcs(allEvents)
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, ics)
                                                type = "text/calendar"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "ICS শেয়ার করুন"))
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("শেয়ার")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Permanent Delete Confirmation for Event
    eventToDelete?.let { ev ->
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("স্থায়ীভাবে মুছে ফেলবেন?") },
            text = { Text("ইভেন্ট '${ev.title}' ডাটাবেজ থেকে স্থায়ীভাবে মুছে যাবে। এটি আর পুনরুদ্ধার করা যাবে না।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEvent(ev)
                        eventToDelete = null
                        Toast.makeText(context, "ইভেন্ট মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) { Text("বাতিল") }
            }
        )
    }

    // Permanent Delete Confirmation for Task
    taskToDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("টাস্কটি স্থায়ীভাবে মুছে ফেলবেন?") },
            text = { Text("টাস্ক '${t.title}' ডাটাবেজ থেকে স্থায়ীভাবে মুছে যাবে। এটি আর পুনরুদ্ধার করা যাবে না।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(t)
                        taskToDelete = null
                        Toast.makeText(context, "টাস্ক মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text("বাতিল") }
            }
        )
    }
}

// Backwards compatibility alias for Dialog
@Composable
fun ArchiveDialog(
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ArchiveExportScreen(
            viewModel = viewModel,
            onBack = onDismiss
        )
    }
}
