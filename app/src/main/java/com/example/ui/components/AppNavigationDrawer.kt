package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils

@Composable
fun AppDrawerContent(
    viewModel: CalendarViewModel,
    currentTab: Int,
    onNavigateToTab: (Int) -> Unit,
    onOpenBirthdays: () -> Unit,
    onOpenAnniversaries: () -> Unit,
    onOpenHolidays: () -> Unit,
    onOpenCountdowns: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenDateCalculator: () -> Unit = {},
    onOpenFreeTime: () -> Unit = {},
    onOpenArchive: () -> Unit = {},
    onOpenBackupRestore: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenSupport: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val customAppName by viewModel.customAppName.collectAsState()
    val activeTagline by viewModel.activeTagline.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val hijriAdjustment by viewModel.hijriDayAdjustment.collectAsState()

    val dateFormatted = remember(selectedDate) {
        CalendarUtils.formatDate(selectedDate.timeInMillis, "EEEE, d MMMM yyyy")
    }
    val banglaDate = remember(selectedDate) {
        CalendarUtils.getBanglaDate(selectedDate).formattedBn
    }
    val hijriDate = remember(selectedDate, hijriAdjustment) {
        CalendarUtils.getHijriDate(selectedDate, hijriAdjustment).formattedEn
    }


    ModalDrawerSheet(
        modifier = Modifier
            .widthIn(max = 330.dp)
            .fillMaxHeight()
            .testTag("app_navigation_drawer"),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(bottomEnd = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .padding(start = AppSpacing.lg, end = AppSpacing.lg, top = AppSpacing.xl, bottom = AppSpacing.lg)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "App Logo",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = customAppName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.3).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (activeTagline.isNotBlank()) {
                                    Text(
                                        text = activeTagline,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.xxs))

                        // Compact Date Card in Header
                        Surface(
                            shape = RoundedCornerShape(AppRadius.sm),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)) {
                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = banglaDate,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = hijriDate,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Main Sections Group
            Text(
                text = "প্রধান সেকশন (Main)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xxs)
            )

            DrawerNavItem(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home,
                label = "হোম (Home)",
                isSelected = currentTab == 0,
                testTag = "drawer_item_home",
                onClick = {
                    onNavigateToTab(0)
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.CalendarMonth,
                selectedIcon = Icons.Filled.CalendarMonth,
                label = "ক্যালেন্ডার (Calendar)",
                isSelected = currentTab == 1,
                testTag = "drawer_item_calendar",
                onClick = {
                    onNavigateToTab(1)
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.CheckCircleOutline,
                selectedIcon = Icons.Filled.CheckCircle,
                label = "টাস্ক তালিকা (Tasks)",
                isSelected = currentTab == 2,
                testTag = "drawer_item_tasks",
                onClick = {
                    onNavigateToTab(2)
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.StickyNote2,
                selectedIcon = Icons.Filled.StickyNote2,
                label = "নোটবুক (Notes)",
                isSelected = currentTab == 3,
                testTag = "drawer_item_notes",
                onClick = {
                    onNavigateToTab(3)
                    onCloseDrawer()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // Features & Tools Group
            Text(
                text = "বিশেষ ফিচার ও টুলস (Features)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xxs)
            )

            DrawerNavItem(
                icon = Icons.Outlined.Cake,
                selectedIcon = Icons.Filled.Cake,
                label = "জন্মদিন তালিকা (Birthdays)",
                isSelected = false,
                testTag = "drawer_item_birthdays",
                onClick = {
                    onCloseDrawer()
                    onOpenBirthdays()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.FavoriteBorder,
                selectedIcon = Icons.Filled.Favorite,
                label = "বিবাহবার্ষিকী (Anniversaries)",
                isSelected = false,
                testTag = "drawer_item_anniversaries",
                onClick = {
                    onCloseDrawer()
                    onOpenAnniversaries()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.Celebration,
                selectedIcon = Icons.Filled.Celebration,
                label = "বাৎসরিক ছুটি (Holidays)",
                isSelected = false,
                testTag = "drawer_item_holidays",
                onClick = {
                    onCloseDrawer()
                    onOpenHolidays()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.Calculate,
                selectedIcon = Icons.Filled.Calculate,
                label = "তারিখ ক্যালকুলেটর (Date Tools)",
                isSelected = false,
                testTag = "drawer_item_calculator",
                onClick = {
                    onCloseDrawer()
                    onOpenDateCalculator()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.Schedule,
                selectedIcon = Icons.Filled.Schedule,
                label = "ফ্রি সময় ফাইন্ডার (Free Time)",
                isSelected = false,
                testTag = "drawer_item_free_time",
                onClick = {
                    onCloseDrawer()
                    onOpenFreeTime()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.Analytics,
                selectedIcon = Icons.Filled.Analytics,
                label = "পরিসংখ্যান ও অ্যানালিটিক্স (Stats)",
                isSelected = false,
                testTag = "drawer_item_statistics",
                onClick = {
                    onCloseDrawer()
                    onOpenStatistics()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.Archive,
                selectedIcon = Icons.Filled.Archive,
                label = "আর্কাইভ ও এক্সপোর্ট (Archive)",
                isSelected = false,
                testTag = "drawer_item_archive",
                onClick = {
                    onCloseDrawer()
                    onOpenArchive()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.HourglassEmpty,
                selectedIcon = Icons.Filled.HourglassFull,
                label = "কাউন্টডাউন টাইমার (Countdowns)",
                isSelected = false,
                testTag = "drawer_item_countdowns",
                onClick = {
                    onCloseDrawer()
                    onOpenCountdowns()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.CloudUpload,
                selectedIcon = Icons.Filled.CloudUpload,
                label = "ব্যাকআপ ও রিস্টোর (Backup)",
                isSelected = false,
                testTag = "drawer_item_backup",
                onClick = {
                    onCloseDrawer()
                    onOpenBackupRestore()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // Preferences & System Group
            Text(
                text = "সিস্টেম ও সহায়তা (Preferences)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xxs)
            )

            DrawerNavItem(
                icon = Icons.Outlined.Settings,
                selectedIcon = Icons.Filled.Settings,
                label = "সেটিংস (Settings)",
                isSelected = currentTab == 4,
                testTag = "drawer_item_settings",
                onClick = {
                    onNavigateToTab(4)
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.Info,
                selectedIcon = Icons.Filled.Info,
                label = "অ্যাপ সম্পর্কে (About)",
                isSelected = false,
                testTag = "drawer_item_about",
                onClick = {
                    onCloseDrawer()
                    onOpenAbout()
                }
            )

            DrawerNavItem(
                icon = Icons.Outlined.SupportAgent,
                selectedIcon = Icons.Filled.SupportAgent,
                label = "সহায়তা ও ফিডব্যাক (Support)",
                isSelected = false,
                testTag = "drawer_item_support",
                onClick = {
                    onCloseDrawer()
                    onOpenSupport()
                }
            )

            Spacer(modifier = Modifier.height(AppSpacing.xl))
        }
    }
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = AppSpacing.sm, vertical = 2.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(AppRadius.md),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            unselectedContainerColor = Color.Transparent
        )
    )
}
