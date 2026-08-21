package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.BackupUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreDialog(
    viewModel: CalendarViewModel,
    initialTab: Int = 0, // 0: Backup, 1: Restore, 2: ICS Import/Export
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(initialTab) }
    var jsonBackupText by remember { mutableStateOf("") }
    var icsText by remember { mutableStateOf("") }
    var restoreInputJson by remember { mutableStateOf("") }
    var icsImportInput by remember { mutableStateOf("") }

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    val allEvents by viewModel.allEvents.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val allBirthdays by viewModel.allBirthdays.collectAsState()
    val allAnniversaries by viewModel.allAnniversaries.collectAsState()
    val allHolidays by viewModel.allHolidays.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val allCountdowns by viewModel.allCountdowns.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppRadius.dialog),
        modifier = Modifier.testTag("backup_restore_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                                imageVector = Icons.Outlined.CloudSync,
                                contentDescription = "Backup and Restore",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(AppDimensions.iconMedium)
                            )
                        }
                    }
                    Text(
                        text = "Backup & Restore",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(AppDimensions.minTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppDimensions.iconMedium)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Backup",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Restore",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "ICS File",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                when (selectedTab) {
                    0 -> { // BACKUP TAB
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(AppSpacing.md),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Text(
                                    text = "স্থানীয় ডিভাইস ব্যাকআপ (Local Backup)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "আপনার সমস্ত ইভেন্ট, টাস্ক, নোটস, জন্মদিন এবং সেটিংস ডিভাইস মেমরিতে ব্যাকআপ রাখুন। কোনো ইন্টারনেটের প্রয়োজন নেই।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(AppSpacing.xs))

                                Button(
                                    onClick = {
                                        scope.launch {
                                            jsonBackupText = BackupUtils.createFullBackupJson(
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
                                            Toast.makeText(context, "Backup JSON Generated ✓", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(AppRadius.md),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Outlined.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                                    Text("Create Local Backup Now")
                                }
                            }
                        }

                        if (jsonBackupText.isNotEmpty()) {
                            OutlinedTextField(
                                value = jsonBackupText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Backup JSON Data") },
                                shape = RoundedCornerShape(AppRadius.md),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(jsonBackupText))
                                    Toast.makeText(context, "Backup JSON Copied to Clipboard ✓", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(AppRadius.md),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(AppDimensions.iconSmall))
                                Spacer(modifier = Modifier.width(AppSpacing.xs))
                                Text("Copy JSON to Clipboard")
                            }
                        }
                    }

                    1 -> { // RESTORE TAB
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(AppSpacing.md),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Text(
                                    text = "রিস্টোর করুন (Restore Backup)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "পূর্বে তৈরি করা ব্যাকআপ JSON ডাটা পেস্ট করে ডেটাবেসে রিস্টোর করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = restoreInputJson,
                                    onValueChange = { restoreInputJson = it },
                                    label = { Text("Paste Backup JSON Content") },
                                    placeholder = { Text("{\"version\": 1, \"events\": [...]}") },
                                    shape = RoundedCornerShape(AppRadius.md),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                )

                                Button(
                                    onClick = { showRestoreConfirmDialog = true },
                                    enabled = restoreInputJson.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(AppRadius.md),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Outlined.Restore, contentDescription = null)
                                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                                    Text("Restore Database Data")
                                }
                            }
                        }
                    }

                    2 -> { // ICS IMPORT/EXPORT TAB
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
                                    text = "ICS Export (ইভেন্ট এক্সপোর্ট)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "আপনার সমস্ত ইভেন্ট স্ট্যান্ডার্ড .ics ক্যালেন্ডার ফরম্যাটে এক্সপোর্ট করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(AppSpacing.xs))
                                Button(
                                    onClick = {
                                        icsText = BackupUtils.exportEventsToIcs(allEvents)
                                        clipboardManager.setText(AnnotatedString(icsText))
                                        Toast.makeText(context, "ICS Content Copied to Clipboard ✓", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(AppRadius.md),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Outlined.Event, contentDescription = null)
                                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                                    Text("Export Events to ICS")
                                }
                            }
                        }

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
                                    text = "ICS Import (ইভেন্ট ইমপোর্ট)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "বাইরের .ics ফাইল কন্টেন্ট পেস্ট করে অ্যাপে ইভেন্ট যোগ করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(AppSpacing.xs))
                                OutlinedTextField(
                                    value = icsImportInput,
                                    onValueChange = { icsImportInput = it },
                                    label = { Text("Paste .ics File Content") },
                                    shape = RoundedCornerShape(AppRadius.md),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                )
                                Spacer(modifier = Modifier.height(AppSpacing.xs))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val count = BackupUtils.importEventsFromIcs(icsImportInput, viewModel.repository)
                                            Toast.makeText(context, "$count Events Imported Successfully ✓", Toast.LENGTH_SHORT).show()
                                            icsImportInput = ""
                                        }
                                    },
                                    enabled = icsImportInput.isNotBlank(),
                                    shape = RoundedCornerShape(AppRadius.md),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Outlined.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                                    Text("Import ICS Events")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Text("Close (বন্ধ করুন)")
            }
        }
    )

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text(
                    text = "রিস্টোর নিশ্চিতকরণ (Confirm Restore)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিত যে আপনি ব্যাকআপ থেকে সমস্ত ডাটা রিস্টোর করতে চান? এটি বর্তমান তালিকায় নতুন তথ্য যুক্ত/আপডেট করবে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = BackupUtils.restoreFullBackupJson(restoreInputJson, viewModel.repository)
                            if (success) {
                                Toast.makeText(context, "Data Restored Successfully! 🎉", Toast.LENGTH_LONG).show()
                                restoreInputJson = ""
                            } else {
                                Toast.makeText(context, "Restore failed! Check JSON format.", Toast.LENGTH_LONG).show()
                            }
                            showRestoreConfirmDialog = false
                        }
                    },
                    shape = RoundedCornerShape(AppRadius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Restore Data")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreConfirmDialog = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

