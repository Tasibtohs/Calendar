package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.Countdown
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun CountdownSection(
    viewModel: CalendarViewModel,
    onAddCountdownClick: () -> Unit
) {
    val countdowns by viewModel.allCountdowns.collectAsState()
    var itemToDelete by remember { mutableStateOf<Countdown?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm)
            .testTag("countdown_section")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.HourglassTop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.iconSmall)
                        )
                    }
                }
                Text(
                    text = "Countdowns (কাউন্টডাউন)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = onAddCountdownClick,
                modifier = Modifier.size(AppDimensions.minTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add Countdown",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AppDimensions.iconMedium)
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.xs))

        if (countdowns.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg)
                    .clickable { onAddCountdownClick() },
                shape = RoundedCornerShape(AppRadius.md),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.HourglassEmpty,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(AppDimensions.iconSmall)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "কোনো কাউন্টডাউন নেই ⏳",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "পরীক্ষা, জন্মদিন বা ট্রাভেলের সময় ট্র্যাক করতে যোগ করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = AppSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                items(countdowns, key = { it.id }) { item ->
                    CountdownCard(
                        countdown = item,
                        onDelete = { itemToDelete = item }
                    )
                }
            }
        }
    }

    // Delete Countdown Confirmation Dialog
    itemToDelete?.let { countdown ->
        AppDeleteDialog(
            onDismissRequest = { itemToDelete = null },
            title = "Delete Countdown",
            message = "Are you sure you want to delete '${countdown.title}' countdown?",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm = {
                viewModel.deleteCountdown(countdown)
                itemToDelete = null
            }
        )
    }
}

@Composable
fun CountdownCard(
    countdown: Countdown,
    onDelete: () -> Unit
) {
    val now = Calendar.getInstance().timeInMillis
    val diffMs = countdown.targetDate - now
    val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMs)

    val cardBgColor = try {
        Color(android.graphics.Color.parseColor(countdown.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier
            .width(176.dp)
            .testTag("countdown_card_${countdown.id}"),
        color = cardBgColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(AppRadius.md),
        border = BorderStroke(1.dp, cardBgColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .padding(AppSpacing.md)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = cardBgColor,
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (countdown.category) {
                                "Birthday" -> Icons.Outlined.Cake
                                "Exam" -> Icons.Outlined.School
                                "Wedding" -> Icons.Outlined.Favorite
                                "Travel" -> Icons.Outlined.Flight
                                else -> Icons.Outlined.Event
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(AppDimensions.minTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppDimensions.iconSmall)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            Text(
                text = countdown.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(AppSpacing.xxs))

            Text(
                text = if (daysRemaining > 0) {
                    "$daysRemaining দিন বাকি"
                } else if (daysRemaining == 0L) {
                    "আজকে অনুষ্ঠিত 🎉"
                } else {
                    "${kotlin.math.abs(daysRemaining)} দিন পার হয়েছে"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = cardBgColor
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.xxs))

            Text(
                text = CalendarUtils.formatDate(countdown.targetDate, "d MMM yyyy"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCountdownDialog(
    onDismiss: () -> Unit,
    onSave: (Countdown) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Event") } // Birthday, Exam, Wedding, Travel, Important Event
    var targetDate by remember { mutableLongStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis) }
    var colorHex by remember { mutableStateOf("#3F51B5") }
    var notes by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }

    val categories = listOf("Event", "Birthday", "Exam", "Wedding", "Travel", "Important")
    val colors = listOf("#3F51B5", "#E91E63", "#4CAF50", "#FF9800", "#9C27B0", "#00BCD4")

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
                            imageVector = Icons.Outlined.HourglassEmpty,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.iconMedium)
                        )
                    }
                }
                Text(
                    text = "New Countdown",
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (শিরোনাম) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppRadius.md),
                    singleLine = true
                )

                Text(
                    text = "Select Category:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(AppRadius.full)
                        )
                    }
                }

                Text(
                    text = "Target Date (লক্ষ্য তারিখ):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(AppRadius.md),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CalendarUtils.formatDate(targetDate, "EEEE, d MMMM yyyy"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Pick Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.iconMedium)
                        )
                    }
                }

                Text(
                    text = "Color Theme:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    colors.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorHex == hex) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            Countdown(
                                title = title,
                                targetDate = targetDate,
                                category = category,
                                colorHex = colorHex,
                                notes = notes
                            )
                        )
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(AppRadius.md),
                enabled = title.isNotBlank()
            ) {
                Text("Save Countdown")
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
        DatePickerModal(
            initialSelectedDateMillis = targetDate,
            onDateSelected = { targetDate = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }
}
