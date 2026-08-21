package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeTimeScreen(
    viewModel: CalendarViewModel,
    onBack: () -> Unit,
    onAddEventForSlot: ((startMillis: Long, endMillis: Long) -> Unit)? = null
) {
    val selectedCalState by viewModel.selectedDate.collectAsState()
    var currentCal by remember { mutableStateOf(selectedCalState.clone() as Calendar) }
    var showEventEditorForSlot by remember { mutableStateOf(false) }
    var editorInitialStart by remember { mutableLongStateOf(0L) }
    var editorInitialEnd by remember { mutableLongStateOf(0L) }

    val categories by viewModel.allCategories.collectAsState()

    val freeSlots = remember(currentCal) {
        viewModel.calculateFreeTimeSlots(currentCal)
    }

    val dateFormatted = remember(currentCal) {
        CalendarUtils.formatDate(currentCal.timeInMillis, "EEEE, d MMMM yyyy")
    }
    val banglaDate = remember(currentCal) {
        CalendarUtils.getBanglaDate(currentCal).formattedBn
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("free_time_screen"),
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
                                text = "ফ্রি সময় ও শিডিউল ফাইন্ডার",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "দিনের ফাঁকা স্লট ও মিটিংয়ের জন্য সেরা সময়",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = AppSpacing.xs)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${freeSlots.size} টি স্লট",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
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
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            // Date Selector Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val next = (currentCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                                currentCal = next
                            }
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "বাংলা: $banglaDate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                val next = (currentCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                                currentCal = next
                            }
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { currentCal = Calendar.getInstance() },
                            contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = 4.dp),
                            shape = RoundedCornerShape(AppRadius.full)
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("আজকের দিন (Today)", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Info Notice Banner
            Surface(
                shape = RoundedCornerShape(AppRadius.md),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Text(
                        text = "কার্যদিবসের সময়সূচী (সকাল ৮:০০ - রাত ৮:০০) অনুযায়ী সংরক্ষিত ইভেন্ট বিশ্লেষণ করে ফাঁকা সময় বের করা হয়েছে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = "পাওয়া যাওয়া ফ্রি স্লটসমূহ (Available Free Slots)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            if (freeSlots.isEmpty()) {
                AppEmptyState(
                    icon = Icons.Outlined.Warning,
                    title = "কোনো ফ্রি সময় খালি নেই",
                    subtitle = "নির্বাচিত দিনে সকাল ৮:০০ থেকে রাত ৮:০০ পর্যন্ত আপনার পূর্ণ শিডিউল বুকড!",
                    modifier = Modifier.padding(vertical = AppSpacing.lg)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    freeSlots.forEach { slot ->
                        FreeSlotItemCard(
                            startTime = slot.first,
                            endTime = slot.second,
                            onAddEvent = {
                                onAddEventForSlot?.invoke(currentCal.timeInMillis, currentCal.timeInMillis)
                                showEventEditorForSlot = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEventEditorForSlot) {
        EventEditorDialog(
            categories = categories,
            viewModel = viewModel,
            onDismiss = { showEventEditorForSlot = false },
            onSaveSuccess = {
                showEventEditorForSlot = false
                currentCal = (currentCal.clone() as Calendar)
            }
        )
    }
}

@Composable
private fun FreeSlotItemCard(
    startTime: String,
    endTime: String,
    onAddEvent: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacing.sm))

                Column {
                    Text(
                        text = "$startTime - $endTime",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "সম্পূর্ণ ফ্রি 🌿",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "• মিটিং বা কাজের জন্য আদর্শ",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = onAddEvent,
                shape = RoundedCornerShape(AppRadius.md),
                contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ইভেন্ট", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// Backwards compatibility alias for Dialog
@Composable
fun FreeTimeDialog(
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FreeTimeScreen(
            viewModel = viewModel,
            onBack = onDismiss
        )
    }
}
