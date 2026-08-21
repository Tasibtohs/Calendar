package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderCoverPhoto(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coverUri by viewModel.coverPhotoUri.collectAsState()
    val heightDp by viewModel.coverHeightDp.collectAsState()
    val overlayOpacity by viewModel.coverOverlayOpacity.collectAsState()
    val cornerRadiusDp by viewModel.coverCornerRadiusDp.collectAsState()
    val badgeText by viewModel.coverBadgeText.collectAsState()
    val borderStyle by viewModel.coverBorderStyle.collectAsState()
    val borderWidth by viewModel.coverBorderWidth.collectAsState()

    var showOptionsSheet by remember { mutableStateOf(false) }
    var selectedEditorUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoEditor by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedEditorUri = it
            showPhotoEditor = true
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedEditorUri = tempCameraUri
            showPhotoEditor = true
        }
    }

    val frameShape = RoundedCornerShape(cornerRadiusDp.dp)
    val borderModifier = when (borderStyle) {
        "Solid" -> Modifier.border(
            BorderStroke(borderWidth.dp, MaterialTheme.colorScheme.primary),
            frameShape
        )
        "Gradient" -> Modifier.border(
            BorderStroke(
                borderWidth.dp,
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            ),
            frameShape
        )
        else -> Modifier
    }

    val nowCal = Calendar.getInstance()
    val dayNameBn = CalendarUtils.getBanglaDayName(nowCal.get(Calendar.DAY_OF_WEEK))
    val dateBnStr = CalendarUtils.formatGregorianInBangla(nowCal)
    val formattedDateHeader = "$dayNameBn ● $dateBnStr"

    if (coverUri != null) {
        // --- DISPLAY WHEN PHOTO IS SELECTED ---
        Box(
            modifier = modifier
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
                .fillMaxWidth()
                .aspectRatio(1f)
                .then(borderModifier)
                .clip(frameShape)
                .clickable { showOptionsSheet = true }
                .testTag("header_cover_photo_box")
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(coverUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Cover Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark Gradient Overlay from bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = overlayOpacity * 0.35f),
                                Color.Black.copy(alpha = overlayOpacity)
                            )
                        )
                    )
            )

            // Edit Cover Circular Button (Pencil Icon)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(AppSpacing.md),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                tonalElevation = AppElevation.medium
            ) {
                IconButton(
                    onClick = { showOptionsSheet = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_cover_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Cover Photo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bottom Overlay: Custom Badge + Bengali Date
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(AppSpacing.lg)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(AppRadius.xs),
                ) {
                    Text(
                        text = badgeText.ifBlank { "Personal Dashboard" },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = formattedDateHeader,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    } else {
        // --- DISPLAY WHEN PHOTO IS NOT SELECTED (PREMIUM PLACEHOLDER) ---
        Surface(
            modifier = modifier
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs)
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(frameShape)
                .clickable { showOptionsSheet = true }
                .testTag("header_cover_placeholder_box"),
            shape = frameShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = AppElevation.low,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .padding(AppSpacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.AddAPhoto,
                                contentDescription = "Add Cover Photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    Text(
                        text = "কভার ছবি যোগ করুন (Add Cover Photo)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xxs))

                    Text(
                        text = "আপনার Dashboard-কে ব্যক্তিগত ও আকর্ষণীয় করে তুলুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Photo Options
    if (showOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsSheet = false },
            shape = RoundedCornerShape(topStart = AppRadius.sheet, topEnd = AppRadius.sheet),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            },
            modifier = Modifier.testTag("cover_customize_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg)
                    .padding(bottom = AppSpacing.xxl)
            ) {
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
                                    imageVector = Icons.Outlined.PhotoSizeSelectActual,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(AppDimensions.iconMedium)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "কভার ফটো ব্যবস্থাপনা (Cover Photo)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ছবি সিলেক্ট ও ফ্রেম কাস্টমাইজ করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { showOptionsSheet = false },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(AppDimensions.iconMedium)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                // Options List
                // 1. Gallery
                ListItem(
                    headlineContent = { Text("গ্যালারি থেকে বেছে নিন (Gallery)") },
                    supportingContent = { Text("আপনার ডিভাইস থেকে ছবি সিলেক্ট করুন") },
                    leadingContent = {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.md))
                        .clickable {
                            showOptionsSheet = false
                            galleryLauncher.launch("image/*")
                        }
                        .testTag("cover_gallery_option")
                )

                // 2. Camera
                ListItem(
                    headlineContent = { Text("ক্যামেরা দিয়ে তুলুন (Camera)") },
                    supportingContent = { Text("নতুন ছবি ক্যাপচার করুন") },
                    leadingContent = {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.md))
                        .clickable {
                            showOptionsSheet = false
                            val photoFile = File(context.cacheDir, "cover_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        }
                        .testTag("cover_camera_option")
                )

                // 3. Edit Current Photo / Frame
                if (coverUri != null) {
                    ListItem(
                        headlineContent = { Text("ফ্রেম ও ফিল্টার পরিবর্তন করুন (Edit & Frame)") },
                        supportingContent = { Text("বর্তমান ছবির কালার, ফিল্টার ও ফ্রেম পরিবর্তন করুন") },
                        leadingContent = {
                            Icon(Icons.Outlined.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppRadius.md))
                            .clickable {
                                showOptionsSheet = false
                                selectedEditorUri = Uri.parse(coverUri)
                                showPhotoEditor = true
                            }
                            .testTag("cover_edit_existing_option")
                    )

                    // 4. Remove Cover Photo
                    ListItem(
                        headlineContent = { Text("বর্তমান ছবি মুছুন (Remove Photo)", color = MaterialTheme.colorScheme.error) },
                        supportingContent = { Text("ডিফল্ট প্লেসহোল্ডারে ফেরত যান") },
                        leadingContent = {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppRadius.md))
                            .clickable {
                                showOptionsSheet = false
                                viewModel.setCoverPhotoUri(null)
                            }
                            .testTag("cover_remove_option")
                    )
                }
            }
        }
    }

    // Photo Editor Dialog
    if (showPhotoEditor && selectedEditorUri != null) {
        PhotoEditorDialog(
            sourceUri = selectedEditorUri!!,
            viewModel = viewModel,
            onDismiss = { showPhotoEditor = false },
            onSaved = { showPhotoEditor = false }
        )
    }
}
