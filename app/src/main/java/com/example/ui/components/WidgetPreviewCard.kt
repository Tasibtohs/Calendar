package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.util.CalendarUtils
import com.example.widget.CalendarAppWidget
import com.example.widget.WidgetPreferences
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WidgetPreviewCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Widget Preferences State
    var customPhotoUri by remember { mutableStateOf(WidgetPreferences.getPhotoUri(context)) }
    var presetImage by remember { mutableStateOf(WidgetPreferences.getPresetImage(context)) }
    var overlayOpacity by remember { mutableStateOf(WidgetPreferences.getOverlayOpacity(context)) }
    var showHijri by remember { mutableStateOf(WidgetPreferences.isShowHijri(context)) }
    var showSeason by remember { mutableStateOf(WidgetPreferences.isShowSeason(context)) }
    var showEvents by remember { mutableStateOf(WidgetPreferences.isShowEvents(context)) }
    var selectedWidgetSize by remember { mutableStateOf("Medium (4x2)") } // Small (2x2), Medium (4x2), Large (4x4)

    var showGuideDialog by remember { mutableStateOf(false) }

    // Gallery Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val uriStr = uri.toString()
            customPhotoUri = uriStr
            presetImage = "custom"
            WidgetPreferences.setPhotoUri(context, uriStr)
            Toast.makeText(context, "কাস্টম উইজেট ফটো যুক্ত হয়েছে ✓", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = modifier.fillMaxWidth().testTag("widget_preview_card_root")) {
        // Section Title & How to Add Widget Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Widgets,
                            contentDescription = "Widget Preview",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Text(
                    text = "Android Home Widget Studio (উইজেট স্টুডিও)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            TextButton(
                onClick = { showGuideDialog = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("কিভাবে যোগ করবেন?", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.lg),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            tonalElevation = AppElevation.low
        ) {
            Column(modifier = Modifier.padding(AppSpacing.md)) {
                // Widget Size Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "লাইভ প্রিভিউ সাইজ:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        listOf("Small (2x2)", "Medium (4x2)", "Large (4x4)").forEach { sizeLabel ->
                            val isSelected = selectedWidgetSize == sizeLabel
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedWidgetSize = sizeLabel },
                                label = {
                                    Text(
                                        text = sizeLabel.substringBefore(" "),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // LIVE WIDGET PREVIEW CANVAS
                val cal = Calendar.getInstance()
                val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)
                val dateNum = SimpleDateFormat("d", Locale.getDefault()).format(cal.time)
                val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

                val hijri = CalendarUtils.getHijriDate(cal)
                val bangla = CalendarUtils.getBanglaDate(cal)

                val banglaDayBn = CalendarUtils.toBanglaDigit(bangla.day)
                val banglaYearBn = CalendarUtils.toBanglaDigit(bangla.year)
                val banglaStr = "$banglaDayBn ${bangla.monthNameBn} $banglaYearBn বঙ্গাব্দ"
                val hijriStr = "🌙 ${CalendarUtils.toBanglaDigit(hijri.day)} ${hijri.monthNameBn} ${CalendarUtils.toBanglaDigit(hijri.year)} হিজরি"
                val seasonName = CalendarUtils.getBengaliSeason(bangla.monthNameEn)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .background(Color(0xFF1A202C))
                ) {
                    // Background Image Layer
                    if (customPhotoUri != null) {
                        AsyncImage(
                            model = customPhotoUri,
                            contentDescription = "Widget Background",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val presetDrawable = when (presetImage) {
                            "islamic" -> R.drawable.img_widget_islamic_preset
                            "header" -> R.drawable.default_header_cover
                            "nature" -> R.drawable.img_widget_nature_preset
                            else -> null
                        }
                        if (presetDrawable != null) {
                            Image(
                                painter = painterResource(id = presetDrawable),
                                contentDescription = "Widget Preset",
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Dark Dimming Acrylic Overlay
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = overlayOpacity))
                    )

                    // Foreground Contents based on Size
                    when (selectedWidgetSize) {
                        "Small (2x2)" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppSpacing.md),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = dayName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF60A5FA),
                                        fontSize = 11.sp
                                    )
                                    if (showSeason) {
                                        Surface(
                                            shape = RoundedCornerShape(AppRadius.full),
                                            color = Color.Black.copy(alpha = 0.4f),
                                            border = BorderStroke(1.dp, Color(0x26FFFFFF))
                                        ) {
                                            Text(
                                                text = "🌿 $seasonName",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = banglaDayBn,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24)
                                )

                                Text(
                                    text = "$dateNum $monthYear",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = banglaStr,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (showHijri) {
                                    Text(
                                        text = hijriStr,
                                        color = Color(0xCCE2E8F0),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        "Medium (4x2)" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // Top Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${dayName.uppercase()}, $dateNum ${SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time).uppercase()}",
                                            color = Color(0xFF60A5FA),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "$dateNum $monthYear",
                                            color = Color(0xCCFFFFFF),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (showSeason) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0x4D000000),
                                            border = BorderStroke(1.dp, Color(0x26FFFFFF))
                                        ) {
                                            Text(
                                                text = "🌿 $seasonName",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Middle Traditional Date Highlights
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0x4D000000),
                                    border = BorderStroke(1.dp, Color(0x26FFFFFF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = banglaDayBn,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFBBF24)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = banglaStr,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (showHijri) {
                                                Text(
                                                    text = hijriStr,
                                                    color = Color(0xD9E2E8F0),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                if (showEvents) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📅 আজকের কর্মসূচি: কোনো বিশেষ ইভেন্ট নেই",
                                        color = Color(0xE6CBD5E1),
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Calendar button
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x592D3748),
                                        border = BorderStroke(1.dp, Color(0x40FFFFFF)),
                                        modifier = Modifier.weight(1f).height(30.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CalendarMonth,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ক্যালেন্ডার", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Add Event button
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x592D3748),
                                        border = BorderStroke(1.dp, Color(0x40FFFFFF)),
                                        modifier = Modifier.weight(1f).height(30.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Add,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ইভেন্ট +", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Refresh button
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x592D3748),
                                        border = BorderStroke(1.dp, Color(0x40FFFFFF)),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Refresh,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    // Style button
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0x592D3748),
                                        border = BorderStroke(1.dp, Color(0x40FFFFFF)),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Palette,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "Large (4x4)" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$monthYear",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = banglaStr,
                                        color = Color(0xFFFBBF24),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (showHijri) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = hijriStr,
                                        color = Color(0xCCE2E8F0),
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Mini Month Grid Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                                        Text(
                                            text = it,
                                            color = Color(0xAAFFFFFF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                for (row in 0..2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        for (col in 1..7) {
                                            val dayVal = row * 7 + col
                                            val isToday = dayVal == dateNum.toIntOrNull()
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isToday) Color(0xFF3B82F6) else Color.Transparent,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$dayVal",
                                                        color = if (isToday) Color.White else Color(0xEEFFFFFF),
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                // WIDGET PHOTO CUSTOMIZER (ছবি যোগ ও পরিবর্তন)
                Text(
                    text = "🖼️ উইজেটের ব্যাকগ্রাউন্ড ছবি (Custom Photo & Wallpapers):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                // Action Buttons for Custom Photo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Button(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                        Text("গ্যালারি থেকে ছবি যোগ করুন", fontSize = 12.sp)
                    }

                    if (customPhotoUri != null) {
                        OutlinedButton(
                            onClick = {
                                customPhotoUri = null
                                presetImage = "nature"
                                WidgetPreferences.setPresetImage(context, "nature")
                                Toast.makeText(context, "কাস্টম ছবি রিমুভ করা হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(AppRadius.md)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Aesthetic Preset Wallpapers
                Text(
                    text = "বা প্রিসেট ওয়ালপেপার বেছে নিন:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(AppSpacing.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    val presets = listOf(
                        Triple("nature", "রূপসী বাংলা", R.drawable.img_widget_nature_preset),
                        Triple("islamic", "রয়েল ইসলামিক", R.drawable.img_widget_islamic_preset),
                        Triple("header", "হোম হেডার", R.drawable.default_header_cover),
                        Triple("none", "ডার্ক মিনিমাল", null)
                    )

                    presets.forEach { (id, label, resId) ->
                        val isSelected = customPhotoUri == null && presetImage == id
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(AppRadius.md))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(AppRadius.md)
                                )
                                .clickable {
                                    customPhotoUri = null
                                    presetImage = id
                                    WidgetPreferences.setPresetImage(context, id)
                                    Toast.makeText(context, "প্রিসেট: $label প্রয়োগ করা হয়েছে ✓", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(AppRadius.md),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (resId != null) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = label,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF1E293B))
                                    )
                                }

                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(2.dp)
                                )

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.TopEnd)
                                            .padding(2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Dark Dimming Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ছবির ডার্ক ওভারলে / লেখার স্বচ্ছতা:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(AppRadius.xs)
                    ) {
                        Text(
                            text = "${(overlayOpacity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
                        )
                    }
                }
                Slider(
                    value = overlayOpacity,
                    onValueChange = {
                        overlayOpacity = it
                        WidgetPreferences.setOverlayOpacity(context, it)
                    },
                    valueRange = 0.1f..0.85f
                )

                Spacer(modifier = Modifier.height(AppSpacing.xs))

                // Content Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "হিজরি তারিখ প্রদর্শন করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showHijri,
                        onCheckedChange = {
                            showHijri = it
                            WidgetPreferences.setShowHijri(context, it)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বাংলা ঋতু ব্যাজ প্রদর্শন করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showSeason,
                        onCheckedChange = {
                            showSeason = it
                            WidgetPreferences.setShowSeason(context, it)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "আজকের ইভেন্ট ও টাস্ক লাইন দেখান",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showEvents,
                        onCheckedChange = {
                            showEvents = it
                            WidgetPreferences.setShowEvents(context, it)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Apply & Sync Button
                FilledTonalButton(
                    onClick = {
                        CalendarAppWidget.updateAllWidgets(context)
                        Toast.makeText(context, "ফোনের সকল হোম স্ক্রিন উইজেট সফলভাবে আপডেট হয়েছে! ✓", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(AppRadius.md),
                    modifier = Modifier.fillMaxWidth().testTag("apply_widget_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                    Text("ফোনের হোম স্ক্রিনে উইজেট সিঙ্ক ও রিফ্রেশ করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Guide Dialog on How to Add Widget to Android Launcher
    if (showGuideDialog) {
        AlertDialog(
            onDismissRequest = { showGuideDialog = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Icon(imageVector = Icons.Outlined.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("ফোনে উইজেট ব্যবহারের নিয়ম", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text(
                        text = "আপনার ফোনের হোম স্ক্রিনে স্মার্ট বাংলা ও ইসলামিক ক্যালেন্ডার উইজেট যেভাবে যোগ করবেন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    listOf(
                        "১" to "আপনার ফোনের হোম স্ক্রিনের যেকোনো ফাঁকা জায়গায় ২ সেকেন্ড চাপ দিয়ে ধরে রাখুন (Long Press)।",
                        "২" to "নিচে আসা অপশনগুলো থেকে 'Widgets' (উইজেটস) আইকনে ট্যাপ করুন।",
                        "৩" to "তালিকা স্ক্রল করে 'Calendar' অথবা 'বাংলা ও ইসলামিক ক্যালেন্ডার' খুঁজে নিন।",
                        "৪" to "উইজেটটি চেপে ধরে টেনে আপনার স্ক্রিনের সুবিধাজনক স্থানে বসিয়ে দিন।",
                        "৫" to "উইজেটের সাইজ নিজের ইচ্ছেমতো ছোট বা বড় (Resize) করতে পারেন।"
                    ).forEach { (step, text) ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = step,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showGuideDialog = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("বুঝেছি")
                }
            }
        )
    }
}
