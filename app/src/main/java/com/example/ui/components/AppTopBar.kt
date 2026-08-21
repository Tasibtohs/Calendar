package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel

@Composable
fun AppTopBar(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    isScrolled: Boolean = false,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    val customAppName by viewModel.customAppName.collectAsState()
    val activeTagline by viewModel.activeTagline.collectAsState()
    val isTaglineEnabled by viewModel.isTaglineEnabled.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()

    var showMoreMenu by remember { mutableStateOf(false) }
    var showRateAppDialog by remember { mutableStateOf(false) }

    val elevation by animateDpAsState(
        targetValue = if (isScrolled) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "topBarElevation"
    )

    val backgroundColor = if (isScrolled) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
    }

    val displayTitle = title ?: customAppName
    val displaySubtitle = subtitle ?: (if (isTaglineEnabled && activeTagline.isNotBlank()) activeTagline else null)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation)
            .testTag("app_top_bar"),
        color = backgroundColor,
        tonalElevation = if (isScrolled) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Left: Hamburger Menu Icon (48dp Touch Target)
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(AppDimensions.minTouchTarget)
                    .testTag("btn_hamburger_menu")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 2. App Title Block
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.xs)
                    .testTag("app_title_block")
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (displaySubtitle != null && displaySubtitle.isNotBlank()) {
                    Text(
                        text = displaySubtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            lineHeight = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3. Right Action Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs)
            ) {
                // Search Action
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier
                        .size(AppDimensions.minTouchTarget)
                        .testTag("btn_top_bar_search")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Notification Bell with Real-Time Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(AppDimensions.minTouchTarget)
                ) {
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("btn_top_bar_notifications")
                    ) {
                        Icon(
                            imageVector = if (unreadCount > 0) Icons.Default.Notifications else Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = if (unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    if (unreadCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .testTag("notification_badge")
                        ) {
                            Text(
                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                color = MaterialTheme.colorScheme.onError,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // More (⋮) Menu Button
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier
                            .size(AppDimensions.minTouchTarget)
                            .testTag("btn_top_bar_more")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier.testTag("top_bar_dropdown_menu")
                    ) {
                        DropdownMenuItem(
                            text = { Text("সতেজ করুন (Refresh)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                viewModel.refreshAllData()
                                Toast.makeText(context, "ডাটা সফলভাবে সতেজ করা হয়েছে ✓", Toast.LENGTH_SHORT).show()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("ডেটা এক্সপোর্ট (Export)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onOpenExport()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("অ্যাপ শেয়ার (Share App)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Personal Calendar & Planner")
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "My Calendar - একটি প্রফেশনাল অফলাইন পার্সোনাল ক্যালেন্ডার, বাংলা/হিজরী সন, ইভেন্ট ও টাস্ক প্ল্যানার অ্যাপ।"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share My Calendar"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to share app", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("রেটিং দিন (Rate App)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.StarRate,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                showRateAppDialog = true
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = AppSpacing.xxs),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )

                        DropdownMenuItem(
                            text = { Text("অ্যাপ সম্পর্কে (About)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onOpenAbout()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showRateAppDialog) {
        RateAppDialog(onDismiss = { showRateAppDialog = false })
    }
}

@Composable
fun RateAppDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var rating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(AppRadius.dialog),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md)
                .testTag("rate_app_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFB300).copy(alpha = 0.15f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = "অ্যাপটিতে আপনার অভিজ্ঞতা কেমন?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "আপনার মতামত আমাদের ক্যালেন্ডার অ্যাপ্লিকেশনকে আরও সমৃদ্ধ ও উন্নত করতে সাহায্য করে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // 5 Star Rating row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$i Stars",
                                tint = if (i <= rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reviewComment,
                    onValueChange = { reviewComment = it },
                    label = { Text("আপনার পরামর্শ বা মন্তব্য (ঐচ্ছিক)") },
                    placeholder = { Text("অ্যাপটির কোন দিকটি আপনার সবচেয়ে ভালো লেগেছে?") },
                    shape = RoundedCornerShape(AppRadius.md),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppRadius.md)
                    ) {
                        Text("পরে")
                    }

                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                "ধন্যবাদ! আপনার $rating স্টার রেটিং গ্রহণ করা হয়েছে। ⭐",
                                Toast.LENGTH_LONG
                            ).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppRadius.md)
                    ) {
                        Text("সাবমিট")
                    }
                }
            }
        }
    }
}
