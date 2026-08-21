package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarConverters
import com.example.util.CalendarUtils
import java.util.Calendar

@Composable
fun DailyBriefingCard(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val briefingEnabled by viewModel.dailyBriefingEnabled.collectAsState()
    val todaysEvents by viewModel.todaysEvents.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allBirthdays by viewModel.allBirthdays.collectAsState()
    val allHolidays by viewModel.allHolidays.collectAsState()
    val hijriAdj by viewModel.hijriDayAdjustment.collectAsState()

    if (!briefingEnabled) return

    val calNow = Calendar.getInstance()
    val hourOfDay = calNow.get(Calendar.HOUR_OF_DAY)

    val (greetingText, greetingIcon) = when (hourOfDay) {
        in 5..11 -> "শুভ সকাল" to Icons.Outlined.WbSunny
        in 12..16 -> "শুভ দুপুর" to Icons.Outlined.LightMode
        in 17..20 -> "শুভ সন্ধ্যা" to Icons.Outlined.WbTwilight
        else -> "শুভ রাত্রি" to Icons.Outlined.Bedtime
    }

    val todayCal = Calendar.getInstance()
    val dayNameBn = CalendarUtils.getBanglaDayName(todayCal.get(Calendar.DAY_OF_WEEK))
    val banglaDate = CalendarConverters.getBanglaDate(todayCal)
    val hijriDate = CalendarConverters.getHijriDate(todayCal, hijriAdj)

    val todaysTasks = allTasks.filter { task ->
        task.dueDate?.let { CalendarUtils.isSameDay(it, todayCal.timeInMillis) } ?: false
    }

    val todaysBirthdays = allBirthdays.filter { bday ->
        CalendarUtils.isSameDayMonth(bday.birthDate, todayCal.timeInMillis)
    }

    val todaysHolidays = allHolidays.filter { hol ->
        CalendarUtils.isSameDayMonth(hol.date, todayCal.timeInMillis)
    }

    // Dynamic grammatically correct summary text
    val eventCount = todaysEvents.size
    val taskCount = todaysTasks.size
    val summaryText = when {
        eventCount > 0 && taskCount > 0 -> "আজ আপনার ${CalendarUtils.toBanglaDigit(eventCount)}টি Event এবং ${CalendarUtils.toBanglaDigit(taskCount)}টি Task রয়েছে।"
        eventCount > 0 && taskCount == 0 -> "আজ আপনার ${CalendarUtils.toBanglaDigit(eventCount)}টি Event রয়েছে, কোনো জরুরি Task নেই।"
        eventCount == 0 && taskCount > 0 -> "আজ আপনার কোনো Event নেই, তবে ${CalendarUtils.toBanglaDigit(taskCount)}টি Task সম্পন্ন করার রয়েছে।"
        else -> "আজ আপনার কোনো Event বা Task নেই, দিনটি স্বস্তিতে কাটুক!"
    }

    val cardShape = RoundedCornerShape(AppRadius.lg)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .testTag("daily_briefing_card"),
        shape = cardShape,
        tonalElevation = AppElevation.low,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(AppSpacing.md)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                // 1. TOP ROW: Dynamic Greeting with Outline Icon + Day Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Icon(
                            imageVector = greetingIcon,
                            contentDescription = greetingText,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.iconLarge)
                        )
                        Text(
                            text = greetingText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(AppRadius.full),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = dayNameBn,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs)
                        )
                    }
                }

                // 2. TRI-CALENDAR DATE CARDS SECTION (English, Bangla, Arabic)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    // 2.1 Gregorian (English) Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("briefing_gregorian_date_badge"),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.sm),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(AppRadius.full),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "ENGLISH",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CalendarUtils.toBanglaDigit(todayCal.get(Calendar.DAY_OF_MONTH)),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = CalendarUtils.formatDate(todayCal.timeInMillis, "MMMM"),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = todayCal.get(Calendar.YEAR).toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // 2.2 Bangla Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("briefing_bangla_date_badge"),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.sm),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(AppRadius.full),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "বাংলা",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CalendarUtils.toBanglaDigit(banglaDate.day),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = banglaDate.monthNameBn,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = "${CalendarUtils.toBanglaDigit(banglaDate.year)} বঙ্গাব্দ",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1
                            )
                        }
                    }

                    // 2.3 Arabic / Hijri Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("briefing_hijri_date_badge"),
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.sm),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(AppRadius.full),
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "হিজরী",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CalendarUtils.toBanglaDigit(hijriDate.day),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = hijriDate.monthNameBn,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = "${CalendarUtils.toBanglaDigit(hijriDate.year)} হিঃ",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 3. Real-time Task & Event Summary
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // 4. Birthday & Holiday Badges
                if (todaysBirthdays.isNotEmpty() || todaysHolidays.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        todaysBirthdays.forEach { b ->
                            SuggestionChip(
                                onClick = {},
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Cake,
                                        contentDescription = null,
                                        modifier = Modifier.size(AppDimensions.iconSmall),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                label = { Text("${b.personName}-এর জন্মদিন", style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }
                        todaysHolidays.forEach { h ->
                            SuggestionChip(
                                onClick = {},
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Celebration,
                                        contentDescription = null,
                                        modifier = Modifier.size(AppDimensions.iconSmall),
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                },
                                label = { Text(h.name, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }
                    }
                }
            }
        }
    }
}
