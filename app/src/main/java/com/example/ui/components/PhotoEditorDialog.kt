package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.CalendarUtils
import com.example.util.ImageEditUtils
import com.example.util.PhotoFilterPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

enum class PhotoEditorStep {
    ADJUST_AND_FILTER,
    FRAME_CUSTOMIZATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorDialog(
    sourceUri: Uri,
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentStep by remember { mutableStateOf(PhotoEditorStep.ADJUST_AND_FILTER) }

    // Adjustments
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var selectedFilter by remember { mutableStateOf(PhotoFilterPreset.NATURAL) }

    // Pan & Zoom state for cropping
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Frame Customization State
    val initialRadius by viewModel.coverCornerRadiusDp.collectAsState()
    val initialOverlay by viewModel.coverOverlayOpacity.collectAsState()
    val initialBadge by viewModel.coverBadgeText.collectAsState()
    val initialBorderStyle by viewModel.coverBorderStyle.collectAsState()
    val initialBorderWidth by viewModel.coverBorderWidth.collectAsState()

    var cornerRadiusDp by remember { mutableFloatStateOf(initialRadius.toFloat()) }
    var overlayOpacity by remember { mutableFloatStateOf(initialOverlay) }
    var badgeText by remember { mutableStateOf(initialBadge) }
    var borderStyle by remember { mutableStateOf(initialBorderStyle) } // "None", "Solid", "Gradient"
    var borderWidth by remember { mutableIntStateOf(initialBorderWidth) }

    // Load initial bitmap
    LaunchedEffect(sourceUri) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val bmp = ImageEditUtils.loadSampledBitmapFromUri(context, sourceUri, 1200, 800)
            originalBitmap = bmp
            previewBitmap = bmp
        }
        isLoading = false
    }

    // Refresh preview when adjustments change
    fun updatePreview() {
        val orig = originalBitmap ?: return
        coroutineScope.launch(Dispatchers.Default) {
            val processed = ImageEditUtils.applyAdjustmentsAndFilter(
                source = orig,
                rotationAngle = rotationAngle,
                brightness = brightness,
                contrast = contrast,
                saturation = saturation,
                filterPreset = selectedFilter
            )
            withContext(Dispatchers.Main) {
                previewBitmap = processed
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("photo_editor_dialog"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (currentStep == PhotoEditorStep.ADJUST_AND_FILTER) "ছবি সম্পাদনা (Edit Photo)" else "ফ্রেম ও স্টাইল (Frame & Style)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (currentStep == PhotoEditorStep.FRAME_CUSTOMIZATION) {
                                    currentStep = PhotoEditorStep.ADJUST_AND_FILTER
                                } else {
                                    onDismiss()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentStep == PhotoEditorStep.FRAME_CUSTOMIZATION) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                                contentDescription = "Back / Close"
                            )
                        }
                    },
                    actions = {
                        if (currentStep == PhotoEditorStep.ADJUST_AND_FILTER) {
                            Button(
                                onClick = { currentStep = PhotoEditorStep.FRAME_CUSTOMIZATION },
                                shape = RoundedCornerShape(AppRadius.sm),
                                modifier = Modifier.testTag("photo_editor_next_button")
                            ) {
                                Text("পরবর্তী (Next)")
                            }
                        } else {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val bmp = previewBitmap ?: originalBitmap
                                        if (bmp != null) {
                                            val savedUri = withContext(Dispatchers.IO) {
                                                val squareBmp = ImageEditUtils.cropToSquare(bmp)
                                                ImageEditUtils.saveBitmapToInternalStorage(context, squareBmp)
                                            }
                                            viewModel.setCoverPhotoUri(savedUri.toString())
                                            viewModel.setCoverCornerRadiusDp(cornerRadiusDp.toInt())
                                            viewModel.setCoverOverlayOpacity(overlayOpacity)
                                            viewModel.setCoverBadgeText(badgeText)
                                            viewModel.setCoverBorderStyle(borderStyle)
                                            viewModel.setCoverBorderWidth(borderWidth)
                                            viewModel.setCoverFilterPreset(selectedFilter.name)
                                        }
                                        onSaved()
                                    }
                                },
                                shape = RoundedCornerShape(AppRadius.sm),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("photo_editor_save_button")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("সেভ করুন (Save)")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Preview Card with Live Frame, Border, and Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
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

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .then(borderModifier)
                                .clip(frameShape)
                                .clipToBounds()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            val bmp = previewBitmap
                            if (bmp != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            translationX = offsetX
                                            translationY = offsetY
                                        }
                                        .pointerInput(Unit) {
                                            detectTransformGestures { _, pan, zoom, _ ->
                                                scale = (scale * zoom).coerceIn(1f, 3f)
                                                offsetX += pan.x
                                                offsetY += pan.y
                                            }
                                        }
                                )
                            }

                            // Dark Gradient Overlay
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

                            // Live Overlay Text: Badge + Date
                            val nowCal = Calendar.getInstance()
                            val dayNameBn = CalendarUtils.getBanglaDayName(nowCal.get(Calendar.DAY_OF_WEEK))
                            val dateBnStr = CalendarUtils.formatGregorianInBangla(nowCal)
                            val formattedDateHeader = "$dayNameBn ● $dateBnStr"

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(AppSpacing.md)
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
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Step 1 Controls: Adjustments & Filters
                    if (currentStep == PhotoEditorStep.ADJUST_AND_FILTER) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.lg)
                        ) {
                            // Rotate Button & Reset
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ফিল্টার ও এডজাস্টমেন্ট (Filter & Adjustment)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                    OutlinedButton(
                                        onClick = {
                                            rotationAngle = (rotationAngle + 90f) % 360f
                                            updatePreview()
                                        },
                                        shape = RoundedCornerShape(AppRadius.sm),
                                        contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Outlined.RotateRight, contentDescription = "Rotate", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Rotate 90°", style = MaterialTheme.typography.labelSmall)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                            rotationAngle = 0f
                                            brightness = 0f
                                            contrast = 1f
                                            saturation = 1f
                                            selectedFilter = PhotoFilterPreset.NATURAL
                                            updatePreview()
                                        },
                                        shape = RoundedCornerShape(AppRadius.sm),
                                        contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = 4.dp)
                                    ) {
                                        Text("Reset", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            // Filter Presets Row
                            Text(
                                text = "প্রিসেট ফিল্টার (Presets):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.xs))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(PhotoFilterPreset.values()) { preset ->
                                    val isSelected = selectedFilter == preset
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedFilter = preset
                                            updatePreview()
                                        },
                                        label = { Text(preset.displayName) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        shape = RoundedCornerShape(AppRadius.full)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(AppSpacing.lg))

                            // Sliders: Brightness, Contrast, Saturation
                            // Brightness
                            Text(
                                text = "উজ্জ্বলতা (Brightness): ${brightness.toInt()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Slider(
                                value = brightness,
                                onValueChange = {
                                    brightness = it
                                    updatePreview()
                                },
                                valueRange = -50f..50f
                            )

                            // Contrast
                            Text(
                                text = "কনট্রাস্ট (Contrast): ${String.format("%.1f", contrast)}x",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Slider(
                                value = contrast,
                                onValueChange = {
                                    contrast = it
                                    updatePreview()
                                },
                                valueRange = 0.5f..1.5f
                            )

                            // Saturation
                            Text(
                                text = "স্যাচুরেশন (Saturation): ${String.format("%.1f", saturation)}x",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Slider(
                                value = saturation,
                                onValueChange = {
                                    saturation = it
                                    updatePreview()
                                },
                                valueRange = 0f..2f
                            )
                            
                            Spacer(modifier = Modifier.height(AppSpacing.xl))
                        }
                    }

                    // Step 2 Controls: Frame Customization
                    if (currentStep == PhotoEditorStep.FRAME_CUSTOMIZATION) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.lg)
                        ) {
                            Text(
                                text = "ফ্রেম ও স্টাইল কাস্টমাইজ করুন",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            // Custom Badge Text Input
                            OutlinedTextField(
                                value = badgeText,
                                onValueChange = { badgeText = it },
                                label = { Text("কাস্টম ব্যাজ টেক্সট (Badge Label)") },
                                placeholder = { Text("Personal Dashboard") },
                                singleLine = true,
                                shape = RoundedCornerShape(AppRadius.md),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            // Corner Radius Slider
                            Text(
                                text = "ফ্রেম কর্নার রেডিয়াস (Corner Radius): ${cornerRadiusDp.toInt()} dp",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                            ) {
                                val radiusPresets = listOf(
                                    "No Frame" to 0f,
                                    "Small" to 8f,
                                    "Medium" to 16f,
                                    "Large" to 24f,
                                    "Pill" to 32f
                                )
                                radiusPresets.forEach { (label, r) ->
                                    FilterChip(
                                        selected = cornerRadiusDp == r,
                                        onClick = { cornerRadiusDp = r },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                        shape = RoundedCornerShape(AppRadius.sm)
                                    )
                                }
                            }
                            Slider(
                                value = cornerRadiusDp,
                                onValueChange = { cornerRadiusDp = it },
                                valueRange = 0f..36f
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            // Border Style
                            Text(
                                text = "বর্ডার স্টাইল (Border Style):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                listOf("None" to "বর্ডার ছাড়া", "Solid" to "সলিড কালার", "Gradient" to "গ্র্যাডিয়েন্ট").forEach { (styleKey, title) ->
                                    FilterChip(
                                        selected = borderStyle == styleKey,
                                        onClick = { borderStyle = styleKey },
                                        label = { Text(title) },
                                        shape = RoundedCornerShape(AppRadius.full)
                                    )
                                }
                            }

                            if (borderStyle != "None") {
                                Spacer(modifier = Modifier.height(AppSpacing.sm))
                                Text(
                                    text = "বর্ডার পুরুত্ব (Border Width): $borderWidth dp",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Slider(
                                    value = borderWidth.toFloat(),
                                    onValueChange = { borderWidth = it.toInt() },
                                    valueRange = 1f..4f,
                                    steps = 2
                                )
                            }

                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            // Overlay Darkness Slider
                            Text(
                                text = "ওভারলে অন্ধকার (Overlay Darkness): ${(overlayOpacity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Slider(
                                value = overlayOpacity,
                                onValueChange = { overlayOpacity = it },
                                valueRange = 0.1f..0.85f
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.xl))
                        }
                    }
                }
            }
        }
    }
}
