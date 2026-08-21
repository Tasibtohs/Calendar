package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarConverters

@Composable
fun HijriBanglaWidget(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCal by viewModel.selectedDate.collectAsState()
    val hijriAdj by viewModel.hijriDayAdjustment.collectAsState()

    var showHijriTuneDialog by remember { mutableStateOf(false) }

    val banglaDate = CalendarConverters.getBanglaDate(selectedCal)
    val hijriDate = CalendarConverters.getHijriDate(selectedCal, hijriAdj)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("hijri_bangla_widget_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Traditional Calendars",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                IconButton(
                    onClick = { showHijriTuneDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("tune_hijri_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Adjust Hijri Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bangla Date Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bangla_date_surface"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "🇧🇩 বাংলা পঞ্জিকা",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = banglaDate.formattedBn,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${banglaDate.day} ${banglaDate.monthNameEn}, ${banglaDate.year} BS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Hijri Date Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hijri_date_surface"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "🌙 التقويم الهجري",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hijriDate.formattedBn,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = hijriDate.formattedEn,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showHijriTuneDialog) {
        AlertDialog(
            onDismissRequest = { showHijriTuneDialog = false },
            title = { Text("Hijri Date Moon Sighting Adjustment") },
            text = {
                Column {
                    Text("Adjust Hijri calendar by ±1 or ±2 days according to local moon sighting:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = hijriAdj == -2,
                            onClick = { viewModel.setHijriDayAdjustment(-2) },
                            label = { Text("-2 Days") }
                        )
                        FilterChip(
                            selected = hijriAdj == -1,
                            onClick = { viewModel.setHijriDayAdjustment(-1) },
                            label = { Text("-1 Day") }
                        )
                        FilterChip(
                            selected = hijriAdj == 0,
                            onClick = { viewModel.setHijriDayAdjustment(0) },
                            label = { Text("0 (Default)") }
                        )
                        FilterChip(
                            selected = hijriAdj == 1,
                            onClick = { viewModel.setHijriDayAdjustment(1) },
                            label = { Text("+1 Day") }
                        )
                        FilterChip(
                            selected = hijriAdj == 2,
                            onClick = { viewModel.setHijriDayAdjustment(2) },
                            label = { Text("+2 Days") }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHijriTuneDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
