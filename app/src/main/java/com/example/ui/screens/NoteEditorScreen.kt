package com.example.ui.screens

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.ui.components.*
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.util.CalendarUtils
import com.example.util.NoteChecklistItem
import com.example.util.NoteUtils
import java.text.SimpleDateFormat
import java.util.*

private val NAVY_BLUE_ACCENT = Color(0xFF0D47A1)

private val NOTE_COLORS = listOf(
    "#0D47A1", // Navy Blue
    "#1565C0", // Royal Blue
    "#00695C", // Deep Teal
    "#2E7D32", // Forest Green
    "#C62828", // Crimson
    "#6A1B9A", // Deep Purple
    "#AD1457", // Deep Rose
    "#D84315", // Rust Orange
    "#37474F", // Slate Grey
    "#212121"  // Dark Obsidian
)

enum class EditorViewMode {
    WRITE,
    PREVIEW
}

enum class NoteFontSize(val label: String, val sizeSp: Float, val lineHeightSp: Float) {
    SMALL("ছোট", 14f, 22f),
    NORMAL("স্বাভাবিক", 16f, 25f),
    LARGE("বড়", 18f, 28f),
    EXTRA_LARGE("বিশাল", 22f, 32f)
}

data class NoteTemplate(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val content: String,
    val isChecklist: Boolean = false
)

private val BUILT_IN_TEMPLATES = listOf(
    NoteTemplate(
        title = "দৈনিক পরিকল্পনা ও লক্ষ্য",
        description = "আজকের প্রধান কাজ ও লক্ষ্যের তালিকা",
        icon = Icons.Outlined.Today,
        content = """# 📅 দৈনিক পরিকল্পনা

## 🎯 আজকের প্রধান ৩টি লক্ষ্য:
1. 
2. 
3. 

## 📋 কাজের তালিকা:
[ ] সকালের জরুরি কাজ
[ ] মিটিং ও যোগাযোগ
[ ] মূল প্রজেক্টের কাজ
[ ] রিভিউ ও ফলোআপ

## 💡 নোট ও চিন্তা:
> আজকের গুরুত্বপূর্ণ কোনো ভাবনা থাকলে এখানে লিখুন...

## 🌙 দিনশেষে প্রাপ্তি ও মূল্যায়ন:
- সম্পন্ন হয়েছে: 
- কালকের জন্য বাকি: """,
        isChecklist = false
    ),
    NoteTemplate(
        title = "মিটিং নোটস ও সিদ্ধান্ত",
        description = "আলোচ্য বিষয়, সিদ্ধান্ত ও অ্যাকশন পয়েন্ট",
        icon = Icons.Outlined.Groups,
        content = """# 💼 মিটিং নোটস
**স্থান/প্ল্যাটফর্ম:** 
**উপস্থিত সদস্যবৃন্দ:** 

---

## 📌 আলোচ্য এজেন্ডা:
1. 
2. 

## 📝 মূল আলোচনা ও সিদ্ধান্ত:
- 
- 

## 🚀 অ্যাকশন আইটেমস (দায়িত্ব ও ডেডলাইন):
[ ] কাজ ১ - দায়িত্বপ্রাপ্ত:  ডেডলাইন: 
[ ] কাজ ২ - দায়িত্বপ্রাপ্ত:  ডেডলাইন: 

## ⏰ পরবর্তী মিটিংয়ের সময়সূচি:
- """,
        isChecklist = false
    ),
    NoteTemplate(
        title = "কেনাকাটা / বাজারের ফর্দ",
        description = "প্রয়োজনীয় জিনিসপত্রের চেকলিস্ট",
        icon = Icons.Outlined.ShoppingCart,
        content = """# 🛒 বাজারের ফর্দ

## 🥬 শাকসবজি ও ফলমূল:
[ ] পেঁয়াজ ও রসুন
[ ] আলু
[ ] টমেটো ও কাঁচামরিচ
[ ] মৌসুমী ফল

## 🐟 মাছ ও মাংস:
[ ] 
[ ] 

## 🥛 মুদি ও শুকনো খাবার:
[ ] চাল ও ডাল
[ ] সয়াবিন/সরিষার তেল
[ ] লবণ ও মসলা
[ ] দুধ ও ডিম

## 🧼 প্রসাধন ও গৃহস্থালি সামগ্রী:
[ ] সাবান ও ডিটারজেন্ট
[ ] টুথপেস্ট""",
        isChecklist = false
    ),
    NoteTemplate(
        title = "নতুন প্রজেক্ট ও ব্রেনস্টর্মিং",
        description = "আইডিয়া, সমস্যা ও সমাধানের রূপরেখা",
        icon = Icons.Outlined.Lightbulb,
        content = """# 💡 নতুন প্রজেক্ট আইডিয়া

## ❓ মূল সমস্যা / প্রয়োজন:
> ব্যবহারকারী কোন সমস্যার মুখোমুখি হচ্ছেন?

## 🚀 প্রস্তাবিত সমাধান:
- 

## ⭐ মূল বৈশিষ্ট্যসমূহ (Key Features):
1. 
2. 
3. 

## 📊 সম্ভাব্য ঝুঁকি ও চ্যালেঞ্জ:
- 

## 🪜 পরবর্তী পদক্ষেপ:
[ ] মার্কেট যাচাই
[ ] ডিজাইন খসড়া তৈরি
[ ] প্রোটোটাইপ প্রস্তুত""",
        isChecklist = false
    ),
    NoteTemplate(
        title = "আয়-ব্যয় ও মাসিক বাজেট",
        description = "ব্যক্তিগত আয়, ব্যয় ও সঞ্চয়ের হিসাব",
        icon = Icons.Outlined.AccountBalanceWallet,
        content = """# 💰 মাসিক আয়-ব্যয়ের হিসাব

## 💵 আয়ের উৎস:
- বেতন / মূল আয়: 
- অতিরিক্ত আয়: 
**মোট আয়:** 

---

## 💸 প্রধান ব্যয়সমূহ:
[ ] বাসা ভাড়া ও ইউটিলিটি বিল
[ ] খাবার ও বাজার খরচ
[ ] পরিবহন খরচ
[ ] মোবাইল ও ইন্টারনেট বিল
[ ] চিকিৎসা ও জরুরি খরচ
[ ] বিনোদন ও অন্যান্য

---

## 📈 সঞ্চয় ও বিনিয়োগ লক্ষ্য:
- সঞ্চয়: 
- মোট উদ্বৃত্ত: """,
        isChecklist = false
    ),
    NoteTemplate(
        title = "ব্যক্তিগত ডায়েরি ও জার্নাল",
        description = "আজকের অনুভূতি ও কৃতজ্ঞতাবোধ",
        icon = Icons.Outlined.AutoStories,
        content = """# 📖 আমার ডায়েরি

## 🌸 আজকের সেরা মুহূর্ত:
> 

## 💖 যে ৩টি বিষয়ের জন্য আমি আজ কৃতজ্ঞ:
1. 
2. 
3. 

## 💭 আজকের সারাদিনের অনুভূতি ও প্রতিফলন:
""",
        isChecklist = false
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    initialNote: Note? = null,
    categories: List<Category> = emptyList(),
    onSave: (Note) -> Unit,
    onDelete: ((Note) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    onNavigateBack: () -> Unit = { onDismiss?.invoke() }
) {
    val handleBack = {
        onDismiss?.invoke() ?: onNavigateBack()
    }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // State Variables
    var titleValue by remember {
        mutableStateOf(TextFieldValue(initialNote?.title ?: ""))
    }
    var contentValue by remember {
        mutableStateOf(TextFieldValue(initialNote?.content ?: ""))
    }
    var selectedColorHex by remember {
        mutableStateOf(initialNote?.colorHex ?: "#0D47A1")
    }
    var isPinned by remember {
        mutableStateOf(initialNote?.isPinned ?: false)
    }
    var categoryId by remember {
        mutableStateOf(initialNote?.categoryId ?: (categories.firstOrNull()?.id ?: 1L))
    }

    // Advanced Note Features
    var viewMode by remember { mutableStateOf(EditorViewMode.WRITE) }
    var fontSizeSetting by remember { mutableStateOf(NoteFontSize.NORMAL) }
    var isChecklistMode by remember {
        mutableStateOf(initialNote?.isChecklist ?: false)
    }
    val checklistItems = remember {
        mutableStateListOf<NoteChecklistItem>().apply {
            addAll(NoteUtils.parseChecklist(initialNote?.checklistJson ?: "", initialNote?.content ?: ""))
        }
    }
    var isLocked by remember {
        mutableStateOf(initialNote?.isLocked ?: false)
    }
    var pinCode by remember {
        mutableStateOf(initialNote?.pinCode ?: "")
    }
    val tagsList = remember {
        mutableStateListOf<String>().apply {
            addAll(NoteUtils.parseTags(initialNote?.tags ?: ""))
        }
    }
    var reminderTime by remember {
        mutableStateOf(initialNote?.reminderTime)
    }
    var drawingData by remember {
        mutableStateOf(initialNote?.drawingData)
    }

    // Undo / Redo History Management
    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }

    // Search and Replace In-Note
    var showSearchReplaceBar by remember { mutableStateOf(false) }
    var searchKeyword by remember { mutableStateOf("") }
    var replaceKeyword by remember { mutableStateOf("") }

    // UI Dialog & Sheet States
    var showColorPaletteSheet by remember { mutableStateOf(false) }
    var showAttachLinkDialog by remember { mutableStateOf(false) }
    var showSketchDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showExportCardDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }
    var linkTextInput by remember { mutableStateOf("") }
    var linkUrlInput by remember { mutableStateOf("") }

    val contentFocusRequester = remember { FocusRequester() }

    // Voice to Text Speech Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val currentText = contentValue.text
                val selection = contentValue.selection
                val newText = if (selection.collapsed) {
                    val prefix = if (currentText.isNotEmpty() && !currentText.endsWith(" ") && !currentText.endsWith("\n")) " " else ""
                    currentText.substring(0, selection.start) + prefix + spokenText + currentText.substring(selection.end)
                } else {
                    currentText.substring(0, selection.start) + spokenText + currentText.substring(selection.end)
                }
                val newCursor = selection.start + spokenText.length + 1
                if (undoStack.size > 40) undoStack.removeAt(0)
                undoStack.add(contentValue)
                redoStack.clear()
                contentValue = TextFieldValue(text = newText, selection = TextRange(newCursor.coerceAtMost(newText.length)))
                Toast.makeText(context, "কণ্ঠস্বর যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Word, Character & Reading Time Stats
    val wordCount = remember(contentValue.text, checklistItems, isChecklistMode) {
        if (isChecklistMode) {
            checklistItems.sumOf { if (it.text.isBlank()) 0 else it.text.trim().split("\\s+".toRegex()).size }
        } else {
            if (contentValue.text.isBlank()) 0 else contentValue.text.trim().split("\\s+".toRegex()).size
        }
    }
    val charCount = remember(contentValue.text, checklistItems, isChecklistMode) {
        if (isChecklistMode) {
            checklistItems.sumOf { it.text.length }
        } else {
            contentValue.text.length
        }
    }
    val readingTimeMinutes = remember(wordCount) {
        (wordCount / 180).coerceAtLeast(if (wordCount > 0) 1 else 0)
    }

    fun updateContentWithHistory(newValue: TextFieldValue) {
        if (newValue.text != contentValue.text) {
            if (undoStack.size > 40) undoStack.removeAt(0)
            undoStack.add(contentValue)
            redoStack.clear()
        }
        contentValue = newValue
    }

    fun performUndo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(contentValue)
            contentValue = previous
            Toast.makeText(context, "পূর্বাবস্থায় ফিরে গেছে", Toast.LENGTH_SHORT).show()
        }
    }

    fun performRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(contentValue)
            contentValue = next
            Toast.makeText(context, "পুনরায় প্রয়োগ করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveCurrentNote() {
        val finalTitle = titleValue.text.trim().ifEmpty {
            if (isChecklistMode && checklistItems.isNotEmpty()) {
                checklistItems.first().text.take(30).ifBlank { "নোট" }
            } else if (contentValue.text.isNotBlank()) {
                contentValue.text.lines().firstOrNull { it.isNotBlank() }?.replace("#", "")?.trim()?.take(30) ?: "নোট"
            } else {
                "নোট"
            }
        }

        val finalChecklistJson = if (isChecklistMode) NoteUtils.serializeChecklist(checklistItems) else ""
        val finalContent = if (isChecklistMode) {
            checklistItems.joinToString("\n") { (if (it.isDone) "[x] " else "[ ] ") + it.text }
        } else {
            contentValue.text
        }

        val note = (initialNote ?: Note(title = finalTitle)).copy(
            title = finalTitle,
            content = finalContent,
            colorHex = selectedColorHex,
            isPinned = isPinned,
            categoryId = categoryId,
            isChecklist = isChecklistMode,
            checklistJson = finalChecklistJson,
            isLocked = isLocked,
            pinCode = pinCode,
            tags = NoteUtils.formatTags(tagsList),
            reminderTime = reminderTime,
            drawingData = drawingData,
            updatedAt = System.currentTimeMillis()
        )
        onSave(note)
    }

    // Helper functions for rich text insertion
    fun insertFormatting(prefix: String, suffix: String = "", placeholder: String = "") {
        val text = contentValue.text
        val selection = contentValue.selection

        val newText: String
        val newSelection: TextRange

        if (selection.collapsed) {
            val insert = if (placeholder.isNotEmpty()) "$prefix$placeholder$suffix" else "$prefix$suffix"
            newText = text.substring(0, selection.start) + insert + text.substring(selection.end)
            val cursor = selection.start + prefix.length + (if (placeholder.isNotEmpty()) placeholder.length else 0)
            newSelection = TextRange(cursor)
        } else {
            val selectedText = text.substring(selection.start, selection.end)
            val formatted = "$prefix$selectedText$suffix"
            newText = text.substring(0, selection.start) + formatted + text.substring(selection.end)
            newSelection = TextRange(selection.start + prefix.length, selection.end + prefix.length)
        }

        updateContentWithHistory(TextFieldValue(text = newText, selection = newSelection))
    }

    fun insertLinePrefix(prefix: String) {
        val text = contentValue.text
        val selection = contentValue.selection
        val cursor = selection.start

        val lastNewline = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0))
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1

        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        val newCursor = cursor + prefix.length
        updateContentWithHistory(TextFieldValue(text = newText, selection = TextRange(newCursor)))
    }

    fun insertCurrentTimestamp() {
        val formattedDate = SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale("bn")).format(Date())
        insertFormatting("📅 $formattedDate\n")
    }

    fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        reminderTime?.let { calendar.timeInMillis = it }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        reminderTime = calendar.timeInMillis
                        Toast.makeText(context, "রিমাইন্ডার সেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun startVoiceTyping() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "নোটের জন্য বলুন...")
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ভয়েস টাইপিং সমর্থিত নয়", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (initialNote == null) "নতুন নোট" else "নোট সম্পাদনা",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (viewMode == EditorViewMode.WRITE) "লেখা ও ফরম্যাটিং" else "রিচ মার্কডাউন প্রিভিউ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            saveCurrentNote()
                            handleBack()
                        },
                        modifier = Modifier.testTag("btn_back_note_editor")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Undo & Redo Actions
                    IconButton(
                        onClick = { performUndo() },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (undoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { performRedo() },
                        enabled = redoStack.isNotEmpty(),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (redoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // View Mode Toggle (Write vs Preview)
                    IconButton(
                        onClick = {
                            viewMode = if (viewMode == EditorViewMode.WRITE) EditorViewMode.PREVIEW else EditorViewMode.WRITE
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (viewMode == EditorViewMode.WRITE) Icons.Outlined.Visibility else Icons.Outlined.Edit,
                            contentDescription = "Toggle Preview",
                            tint = if (viewMode == EditorViewMode.PREVIEW) NAVY_BLUE_ACCENT else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Search & Replace Toggle
                    IconButton(
                        onClick = { showSearchReplaceBar = !showSearchReplaceBar },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FindReplace,
                            contentDescription = "Find & Replace",
                            tint = if (showSearchReplaceBar) NAVY_BLUE_ACCENT else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Templates Picker
                    IconButton(
                        onClick = { showTemplatesDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "Templates",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Pin Action
                    IconButton(
                        onClick = { isPinned = !isPinned },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_pin_note")
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "Unpin" else "Pin",
                            tint = if (isPinned) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Save Button
                    IconButton(
                        onClick = {
                            saveCurrentNote()
                            Toast.makeText(context, "নোট সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                            handleBack()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("btn_save_note")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Note",
                            tint = NAVY_BLUE_ACCENT,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Enhanced Bottom Formatting / Tools Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Color Palette Selector Row
                    AnimatedVisibility(
                        visible = showColorPaletteSheet,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs)
                        ) {
                            Text(
                                text = "নোট কালার থিম নির্বাচন করুন",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NOTE_COLORS.forEach { hex ->
                                    val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                                    val color = parseHex(hex)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColorHex = hex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }

                    // Main Tools Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = AppSpacing.xs, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Voice Typing Button
                        FormattingIconButton(
                            icon = Icons.Filled.Mic,
                            contentDescription = "Voice Typing",
                            onClick = { startVoiceTyping() },
                            tint = Color(0xFFD32F2F)
                        )

                        // Checklist Mode Switch
                        FormattingIconButton(
                            icon = if (isChecklistMode) Icons.Filled.Checklist else Icons.Outlined.Checklist,
                            contentDescription = "Interactive Checklist Mode",
                            onClick = {
                                if (!isChecklistMode && checklistItems.isEmpty() && contentValue.text.isNotBlank()) {
                                    checklistItems.addAll(NoteUtils.parseChecklist("", contentValue.text))
                                }
                                isChecklistMode = !isChecklistMode
                            },
                            tint = if (isChecklistMode) NAVY_BLUE_ACCENT else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Bold
                        FormattingIconButton(
                            icon = Icons.Default.FormatBold,
                            contentDescription = "Bold",
                            onClick = { insertFormatting("**", "**", "বোল্ড টেক্সট") }
                        )

                        // Italic
                        FormattingIconButton(
                            icon = Icons.Default.FormatItalic,
                            contentDescription = "Italic",
                            onClick = { insertFormatting("*", "*", "ইটালিক টেক্সট") }
                        )

                        // Heading 1 & Heading 2
                        FormattingIconButton(
                            icon = Icons.Default.Title,
                            contentDescription = "H1 Heading",
                            onClick = { insertLinePrefix("# ") }
                        )

                        FormattingIconButton(
                            icon = Icons.Outlined.FormatSize,
                            contentDescription = "H2 Subheading",
                            onClick = { insertLinePrefix("## ") }
                        )

                        // Strikethrough
                        FormattingIconButton(
                            icon = Icons.Default.FormatStrikethrough,
                            contentDescription = "Strikethrough",
                            onClick = { insertFormatting("~~", "~~", "স্ট্রাইক টেক্সট") }
                        )

                        // Quote Block
                        FormattingIconButton(
                            icon = Icons.Default.FormatQuote,
                            contentDescription = "Quote Block",
                            onClick = { insertLinePrefix("> ") }
                        )

                        // Bullet List
                        FormattingIconButton(
                            icon = Icons.Default.FormatListBulleted,
                            contentDescription = "Bullet List",
                            onClick = { insertLinePrefix("• ") }
                        )

                        // Numbered List
                        FormattingIconButton(
                            icon = Icons.Default.FormatListNumbered,
                            contentDescription = "Numbered List",
                            onClick = { insertLinePrefix("1. ") }
                        )

                        // Inline Checkbox
                        FormattingIconButton(
                            icon = Icons.Outlined.CheckBox,
                            contentDescription = "Checklist Prefix",
                            onClick = { insertLinePrefix("[ ] ") }
                        )

                        // Code Block
                        FormattingIconButton(
                            icon = Icons.Outlined.Code,
                            contentDescription = "Code Block",
                            onClick = { insertFormatting("```\n", "\n```", "কোড বা বিস্তারিত...") }
                        )

                        // Current Timestamp
                        FormattingIconButton(
                            icon = Icons.Outlined.Schedule,
                            contentDescription = "Insert Date & Time",
                            onClick = { insertCurrentTimestamp() },
                            tint = Color(0xFF00796B)
                        )

                        // Divider Line
                        FormattingIconButton(
                            icon = Icons.Outlined.HorizontalRule,
                            contentDescription = "Horizontal Divider",
                            onClick = { insertFormatting("\n---\n") }
                        )

                        // Quick Callout Box
                        FormattingIconButton(
                            icon = Icons.Outlined.Info,
                            contentDescription = "Callout Note",
                            onClick = { insertFormatting("\n💡 **নোট:** ", "\n") },
                            tint = Color(0xFFF57C00)
                        )

                        // Hand Drawing / Sketch Pad
                        FormattingIconButton(
                            icon = Icons.Outlined.Draw,
                            contentDescription = "Sketch Pad",
                            onClick = { showSketchDialog = true },
                            tint = if (!drawingData.isNullOrBlank()) Color(0xFFE91E63) else NAVY_BLUE_ACCENT
                        )

                        // Link Dialog
                        FormattingIconButton(
                            icon = Icons.Outlined.AttachFile,
                            contentDescription = "Attachment Link",
                            onClick = { showAttachLinkDialog = true }
                        )

                        // Tag Manager
                        FormattingIconButton(
                            icon = Icons.Outlined.Label,
                            contentDescription = "Tags",
                            onClick = { showAddTagDialog = true },
                            tint = if (tagsList.isNotEmpty()) Color(0xFF00897B) else NAVY_BLUE_ACCENT
                        )

                        // Reminder Alarm
                        FormattingIconButton(
                            icon = if (reminderTime != null) Icons.Filled.AlarmOn else Icons.Outlined.AddAlarm,
                            contentDescription = "Reminder",
                            onClick = { showDateTimePicker() },
                            tint = if (reminderTime != null) Color(0xFF2E7D32) else NAVY_BLUE_ACCENT
                        )

                        // PIN Lock / Privacy
                        FormattingIconButton(
                            icon = if (isLocked) Icons.Filled.Lock else Icons.Outlined.LockOpen,
                            contentDescription = "PIN Lock",
                            onClick = { showPinSetupDialog = true },
                            tint = if (isLocked) Color(0xFFD32F2F) else NAVY_BLUE_ACCENT
                        )

                        // Share Card
                        FormattingIconButton(
                            icon = Icons.Outlined.Share,
                            contentDescription = "Share Card",
                            onClick = { showExportCardDialog = true }
                        )

                        // Color Palette Toggle
                        FormattingIconButton(
                            icon = Icons.Outlined.Palette,
                            contentDescription = "Color Theme",
                            onClick = { showColorPaletteSheet = !showColorPaletteSheet },
                            tint = parseHex(selectedColorHex)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
        ) {
            // In-Note Search and Replace Panel
            AnimatedVisibility(
                visible = showSearchReplaceBar,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AppSpacing.sm),
                    shape = RoundedCornerShape(AppRadius.md),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, NAVY_BLUE_ACCENT.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = searchKeyword,
                                onValueChange = { searchKeyword = it },
                                placeholder = { Text("নোটের মধ্যে খুঁজুন...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            IconButton(
                                onClick = { showSearchReplaceBar = false },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Find")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = replaceKeyword,
                                onValueChange = { replaceKeyword = it },
                                placeholder = { Text("নতুন শব্দ দিয়ে পরিবর্তন...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodySmall
                            )

                            Button(
                                onClick = {
                                    if (searchKeyword.isNotBlank()) {
                                        val newText = contentValue.text.replace(searchKeyword, replaceKeyword, ignoreCase = true)
                                        updateContentWithHistory(TextFieldValue(newText))
                                        Toast.makeText(context, "শব্দ পরিবর্তন সম্পন্ন", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NAVY_BLUE_ACCENT)
                            ) {
                                Text("পরিবর্তন", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Meta Row 1: Category Selector + Font Size + Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Chip Dropdown
                if (categories.isNotEmpty()) {
                    var showCatDropdown by remember { mutableStateOf(false) }
                    val currentCategory = categories.find { it.id == categoryId }

                    Box {
                        Surface(
                            onClick = { showCatDropdown = true },
                            color = currentCategory?.let { parseHex(it.colorHex).copy(alpha = 0.14f) } ?: MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(AppRadius.full),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(currentCategory?.let { parseHex(it.colorHex) } ?: NAVY_BLUE_ACCENT)
                                )
                                Text(
                                    text = currentCategory?.name ?: "Category",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = currentCategory?.let { parseHex(it.colorHex) } ?: MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCatDropdown,
                            onDismissRequest = { showCatDropdown = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(parseHex(cat.colorHex))
                                        )
                                    },
                                    onClick = {
                                        categoryId = cat.id
                                        showCatDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Font Size Selector & Word count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var showFontMenu by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            onClick = { showFontMenu = true },
                            shape = RoundedCornerShape(AppRadius.sm),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.height(26.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(
                                    text = fontSizeSetting.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showFontMenu,
                            onDismissRequest = { showFontMenu = false }
                        ) {
                            NoteFontSize.values().forEach { sizeOption ->
                                DropdownMenuItem(
                                    text = { Text(sizeOption.label) },
                                    leadingIcon = {
                                        if (fontSizeSetting == sizeOption) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = NAVY_BLUE_ACCENT)
                                        }
                                    },
                                    onClick = {
                                        fontSizeSetting = sizeOption
                                        showFontMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Word count & stats
                    Text(
                        text = "$wordCount শব্দ • $charCount অক্ষর • $readingTimeMinutes মিনিট পড়া",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Meta Row 2: Active Feature Badges (Reminder, Lock, Tags)
            if (reminderTime != null || isLocked || tagsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reminder Pill
                    reminderTime?.let { time ->
                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = Color(0xFFE8F5E9),
                            border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                Text(
                                    text = CalendarUtils.formatDate(time, "dd MMM, hh:mm a"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF2E7D32)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Reminder",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { reminderTime = null }
                                )
                            }
                        }
                    }

                    // PIN Locked Pill
                    if (isLocked) {
                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(14.dp))
                                Text(
                                    text = "পিন সুরক্ষিত",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                    }

                    // Tags Pills
                    tagsList.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Tag",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { tagsList.remove(tag) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // TITLE INPUT
            BasicTextField(
                value = titleValue,
                onValueChange = { titleValue = it },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.2).sp
                ),
                cursorBrush = SolidColor(NAVY_BLUE_ACCENT),
                singleLine = false,
                maxLines = 3,
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (titleValue.text.isEmpty()) {
                            Text(
                                text = "শিরোনাম লিখুন",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                    letterSpacing = (-0.2).sp
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_note_title")
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Sketch / Drawing Preview if attached
            if (!drawingData.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.xs),
                    shape = RoundedCornerShape(AppRadius.md),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.sm)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎨 স্কেচ / ড্রয়িং",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { showSketchDialog = true }) {
                                    Text("এডিট")
                                }
                                TextButton(onClick = { drawingData = null }) {
                                    Text("মুছুন", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        NoteDrawingThumbnail(
                            drawingJson = drawingData!!,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacing.sm))
            }

            // VIEW MODE: WRITE vs PREVIEW
            if (viewMode == EditorViewMode.PREVIEW) {
                // Rich Markdown Visual Preview Mode
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 350.dp),
                    shape = RoundedCornerShape(AppRadius.md),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "রিচ মার্কডাউন প্রিভিউ",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = NAVY_BLUE_ACCENT
                            )
                            TextButton(onClick = { viewMode = EditorViewMode.WRITE }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("এডিটরে ফিরুন")
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        if (contentValue.text.isBlank()) {
                            Text(
                                text = "কোনো টেক্সট লেখা হয়নি। এডিটরে গিয়ে লিখুন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            RenderMarkdownPreview(
                                markdownText = contentValue.text,
                                fontSize = fontSizeSetting.sizeSp.sp,
                                lineHeight = fontSizeSetting.lineHeightSp.sp
                            )
                        }
                    }
                }
            } else {
                // Interactive Checklist Mode vs Plain/Rich Text Mode
                if (isChecklistMode) {
                    // Checklist Header & Progress
                    val totalItems = checklistItems.size
                    val doneItems = checklistItems.count { it.isDone }
                    val progress = if (totalItems > 0) doneItems.toFloat() / totalItems.toFloat() else 0f

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xs)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "চেকলিস্ট ($doneItems/$totalItems সম্পন্ন)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = NAVY_BLUE_ACCENT
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        val allDone = checklistItems.all { it.isDone }
                                        checklistItems.forEachIndexed { i, item ->
                                            checklistItems[i] = item.copy(isDone = !allDone)
                                        }
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(if (checklistItems.all { it.isDone }) "সব আনচেক" else "সব সম্পন্ন", style = MaterialTheme.typography.labelSmall)
                                }

                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(AppRadius.full)),
                            color = NAVY_BLUE_ACCENT,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.md))

                        // Checklist Items
                        checklistItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isDone,
                                    onCheckedChange = { checked ->
                                        checklistItems[index] = item.copy(isDone = checked)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = NAVY_BLUE_ACCENT)
                                )

                                var itemText by remember(item.text) { mutableStateOf(item.text) }

                                BasicTextField(
                                    value = itemText,
                                    onValueChange = {
                                        itemText = it
                                        checklistItems[index] = item.copy(text = it)
                                    },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = fontSizeSetting.sizeSp.sp,
                                        color = if (item.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            checklistItems.add(index + 1, NoteChecklistItem())
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (itemText.isEmpty()) {
                                                Text(
                                                    text = "আইটেম লিখুন...",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = fontSizeSetting.sizeSp.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                IconButton(
                                    onClick = { checklistItems.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Delete item",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.xs))

                        // Add Item Button
                        TextButton(
                            onClick = { checklistItems.add(NoteChecklistItem()) },
                            colors = ButtonDefaults.textButtonColors(contentColor = NAVY_BLUE_ACCENT)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("নতুন আইটেম যুক্ত করুন")
                        }
                    }
                } else {
                    // LARGE MULTILINE CONTENT INPUT AREA
                    BasicTextField(
                        value = contentValue,
                        onValueChange = { updateContentWithHistory(it) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = fontSizeSetting.sizeSp.sp,
                            lineHeight = fontSizeSetting.lineHeightSp.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(NAVY_BLUE_ACCENT),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 380.dp)
                            ) {
                                if (contentValue.text.isEmpty()) {
                                    Text(
                                        text = "নোট লিখুন...\n\n💡 টিপস:\n• হেডিং দিতে লিখুন # বা ##\n• বোল্ড করতে **বোল্ড**\n• তালিকা করতে • বা 1. বা [ ]\n• ভয়েস টাইপিং করতে নিচের মাইক বাটন ব্যবহার করুন।",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = fontSizeSetting.sizeSp.sp,
                                            lineHeight = fontSizeSetting.lineHeightSp.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(contentFocusRequester)
                            .testTag("input_note_content")
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xxl))
        }
    }

    // Template Picker Dialog
    if (showTemplatesDialog) {
        AlertDialog(
            onDismissRequest = { showTemplatesDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color(0xFFFF9800))
                    Text("রেডিমেড নোট টেমপ্লেট", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "একটি টেমপ্লেট নির্বাচন করে দ্রুত আপনার প্রয়োজনীয় নোট বা তালিকা তৈরি করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    BUILT_IN_TEMPLATES.forEach { template ->
                        Surface(
                            onClick = {
                                if (titleValue.text.isBlank()) {
                                    titleValue = TextFieldValue(template.title)
                                }
                                if (contentValue.text.isNotBlank()) {
                                    val merged = contentValue.text + "\n\n" + template.content
                                    updateContentWithHistory(TextFieldValue(merged))
                                } else {
                                    updateContentWithHistory(TextFieldValue(template.content))
                                }
                                showTemplatesDialog = false
                                Toast.makeText(context, "'${template.title}' টেমপ্লেট যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(AppRadius.md),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NAVY_BLUE_ACCENT.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = template.icon,
                                        contentDescription = null,
                                        tint = NAVY_BLUE_ACCENT,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = template.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = template.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemplatesDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Dialogs: Drawing, PIN Setup, Attach Link, Add Tags, Export Card
    if (showSketchDialog) {
        NoteDrawingDialog(
            initialDrawingJson = drawingData,
            onDismiss = { showSketchDialog = false },
            onSave = { savedJson ->
                drawingData = savedJson
                showSketchDialog = false
            }
        )
    }

    if (showPinSetupDialog) {
        NotePinSetupDialog(
            initialPin = if (isLocked) pinCode else "",
            onDismiss = { showPinSetupDialog = false },
            onPinSaved = { newPin ->
                pinCode = newPin
                isLocked = true
                Toast.makeText(context, "পিন সুরক্ষা সক্রিয় করা হয়েছে", Toast.LENGTH_SHORT).show()
            },
            onRemovePin = {
                pinCode = ""
                isLocked = false
                Toast.makeText(context, "পিন সুরক্ষা নিষ্ক্রিয় করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text("ট্যাগ পরিচালনা", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        label = { Text("নতুন ট্যাগ (#Tag)") },
                        placeholder = { Text("যেমন: প্রজেক্ট") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "প্রস্তাবিত ট্যাগ সমূহ:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NoteUtils.SUGGESTED_TAGS.forEach { tag ->
                            val isAdded = tagsList.contains(tag)
                            FilterChip(
                                selected = isAdded,
                                onClick = {
                                    if (isAdded) tagsList.remove(tag) else tagsList.add(tag)
                                },
                                label = { Text("#$tag") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newTagInput.trim().removePrefix("#").trim()
                        if (clean.isNotBlank() && !tagsList.contains(clean)) {
                            tagsList.add(clean)
                        }
                        newTagInput = ""
                        showAddTagDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NAVY_BLUE_ACCENT)
                ) {
                    Text("যোগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text("বন্ধ")
                }
            }
        )
    }

    if (showAttachLinkDialog) {
        AlertDialog(
            onDismissRequest = { showAttachLinkDialog = false },
            title = { Text("লিংক যুক্ত করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    OutlinedTextField(
                        value = linkTextInput,
                        onValueChange = { linkTextInput = it },
                        label = { Text("প্রদর্শন নাম (Display Text)") },
                        placeholder = { Text("যেমন: ওয়েবসাইট") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = linkUrlInput,
                        onValueChange = { linkUrlInput = it },
                        label = { Text("URL / লিংক") },
                        placeholder = { Text("https://example.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val text = linkTextInput.ifBlank { "Link" }
                        val url = linkUrlInput.ifBlank { "https://" }
                        insertFormatting("[$text]($url)")
                        linkTextInput = ""
                        linkUrlInput = ""
                        showAttachLinkDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NAVY_BLUE_ACCENT)
                ) {
                    Text("সংযুক্ত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAttachLinkDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (showExportCardDialog) {
        val currentCategory = categories.find { it.id == categoryId }
        val noteForExport = (initialNote ?: Note(title = titleValue.text)).copy(
            title = titleValue.text.ifBlank { "নোট" },
            content = if (isChecklistMode) checklistItems.joinToString("\n") { (if (it.isDone) "[x] " else "[ ] ") + it.text } else contentValue.text,
            colorHex = selectedColorHex,
            isPinned = isPinned,
            categoryId = categoryId,
            isChecklist = isChecklistMode,
            checklistJson = if (isChecklistMode) NoteUtils.serializeChecklist(checklistItems) else "",
            tags = NoteUtils.formatTags(tagsList),
            drawingData = drawingData,
            updatedAt = System.currentTimeMillis()
        )
        NoteExportCardDialog(
            note = noteForExport,
            category = currentCategory,
            onDismiss = { showExportCardDialog = false }
        )
    }
}

@Composable
fun RenderMarkdownPreview(
    markdownText: String,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    modifier: Modifier = Modifier
) {
    val lines = remember(markdownText) { markdownText.lines() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# ").trim(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = NAVY_BLUE_ACCENT
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = NAVY_BLUE_ACCENT
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### ").trim(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                trimmed.startsWith("> ") -> {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NAVY_BLUE_ACCENT.copy(alpha = 0.08f),
                        border = BorderStroke(2.dp, NAVY_BLUE_ACCENT),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = trimmed.removePrefix("> ").trim(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                trimmed.startsWith("```") -> {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF263238),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = trimmed.removePrefix("```").removeSuffix("```").trim().ifEmpty { "{ code }" },
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF80CBC4),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                trimmed.startsWith("[x] ") || trimmed.startsWith("[X] ") -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.CheckBox, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                        Text(
                            text = trimmed.substring(4),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = fontSize,
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                trimmed.startsWith("[ ] ") -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.CheckBoxOutlineBlank, contentDescription = null, tint = NAVY_BLUE_ACCENT, modifier = Modifier.size(18.dp))
                        Text(
                            text = trimmed.substring(4),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                trimmed.startsWith("• ") || trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("•", fontWeight = FontWeight.Bold, color = NAVY_BLUE_ACCENT, fontSize = fontSize)
                        Text(
                            text = trimmed.drop(2).trim(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize, lineHeight = lineHeight),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                trimmed == "---" -> {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
                trimmed.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = trimmed,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = fontSize,
                            lineHeight = lineHeight
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun FormattingIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = NAVY_BLUE_ACCENT
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(38.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun parseHex(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        NAVY_BLUE_ACCENT
    }
}
