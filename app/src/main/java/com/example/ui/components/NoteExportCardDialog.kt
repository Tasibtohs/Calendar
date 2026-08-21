package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.util.CalendarUtils
import com.example.util.NoteUtils

@Composable
fun NoteExportCardDialog(
    note: Note,
    category: Category?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val baseColor = try {
        Color(android.graphics.Color.parseColor(note.colorHex))
    } catch (e: Exception) {
        Color(0xFF0D47A1)
    }

    val checklistItems = remember(note.checklistJson, note.content) {
        if (note.isChecklist) NoteUtils.parseChecklist(note.checklistJson, note.content) else emptyList()
    }
    val tagsList = remember(note.tags) { NoteUtils.parseTags(note.tags) }

    val wordCount = remember(note.content, checklistItems) {
        if (note.isChecklist) {
            checklistItems.sumOf { it.text.trim().split("\\s+".toRegex()).size }
        } else {
            if (note.content.isBlank()) 0 else note.content.trim().split("\\s+".toRegex()).size
        }
    }

    fun getShareableSummaryText(): String {
        return buildString {
            appendLine("📝 ${note.title.ifBlank { "Untitled Note" }}")
            category?.let { appendLine("📁 ক্যাটাগরি: ${it.name}") }
            if (tagsList.isNotEmpty()) {
                appendLine("🏷️ ${tagsList.joinToString(" ") { "#$it" }}")
            }
            appendLine("🕒 আপডেট: ${CalendarUtils.formatDate(note.updatedAt)}")
            appendLine("------------------------------")
            if (note.isChecklist) {
                checklistItems.forEach { item ->
                    appendLine(if (item.isDone) "☑️ ${item.text}" else "⬜ ${item.text}")
                }
            } else {
                appendLine(note.content)
            }
            appendLine("------------------------------")
            appendLine("সংগৃহীত: বাংলা ক্যালেন্ডার ও নোটবুক")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("dialog_export_note_card"),
            shape = RoundedCornerShape(AppRadius.dialog),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নোট কার্ড ও শেয়ার",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.xs))

                // Themed Visual Card Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.xs),
                    shape = RoundedCornerShape(AppRadius.lg),
                    colors = CardDefaults.cardColors(containerColor = baseColor, contentColor = Color.White),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(baseColor, baseColor.copy(alpha = 0.85f))
                                )
                            )
                            .padding(AppSpacing.lg)
                    ) {
                        // Title & Pin
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = note.title.ifBlank { "Untitled Note" },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            if (note.isPinned) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Tags & Badges
                        if (tagsList.isNotEmpty() || category != null) {
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                category?.let { cat ->
                                    Surface(
                                        shape = RoundedCornerShape(AppRadius.xs),
                                        color = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.height(22.dp)
                                    ) {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                tagsList.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(AppRadius.xs),
                                        color = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.height(22.dp)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.md))

                        // Body Content
                        if (note.isChecklist) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                checklistItems.forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.isDone) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                            contentDescription = null,
                                            tint = if (item.isDone) Color(0xFF81C784) else Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = item.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (item.isDone) Color.White.copy(alpha = 0.6f) else Color.White
                                        )
                                    }
                                }
                            }
                        } else if (note.content.isNotBlank()) {
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        }

                        // Drawing Thumbnail if present
                        if (!note.drawingData.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                            NoteDrawingThumbnail(
                                drawingJson = note.drawingData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.md))

                        // Card Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = CalendarUtils.formatDate(note.updatedAt, "dd MMM yyyy, hh:mm a"),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$wordCount শব্দ",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Actions: Copy Text, Share Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Note Summary", getShareableSummaryText())
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "নোট টেক্সট ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppRadius.md)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("কপি করুন")
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, getShareableSummaryText())
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "নোট শেয়ার করুন"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppRadius.md)
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("শেয়ার করুন")
                    }
                }
            }
        }
    }
}
