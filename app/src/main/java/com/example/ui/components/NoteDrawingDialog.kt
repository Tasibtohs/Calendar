package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.util.DrawingPoint
import com.example.util.DrawingStroke
import com.example.util.NoteUtils

private val PALETTE_COLORS = listOf(
    "#000000", // Black
    "#0D47A1", // Navy Blue
    "#1E88E5", // Light Blue
    "#00897B", // Teal
    "#2E7D32", // Green
    "#C62828", // Red
    "#8E24AA", // Purple
    "#F57F17", // Amber
    "#5D4037"  // Brown
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDrawingDialog(
    initialDrawingJson: String? = null,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val strokes = remember {
        mutableStateListOf<DrawingStroke>().apply {
            addAll(NoteUtils.parseDrawingStrokes(initialDrawingJson))
        }
    }

    var currentColorHex by remember { mutableStateOf("#0D47A1") }
    var currentStrokeWidth by remember { mutableFloatStateOf(6f) }
    var isEraserMode by remember { mutableStateOf(false) }

    var currentPoints = remember { mutableStateListOf<DrawingPoint>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
            shape = RoundedCornerShape(AppRadius.lg),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.md)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                        Text(
                            text = "স্কেচ ও ড্রয়িং প্যাড",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        // Undo
                        IconButton(
                            onClick = {
                                if (strokes.isNotEmpty()) {
                                    strokes.removeAt(strokes.lastIndex)
                                }
                            },
                            enabled = strokes.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                        }

                        // Clear All
                        IconButton(
                            onClick = {
                                strokes.clear()
                                currentPoints.clear()
                            },
                            enabled = strokes.isNotEmpty()
                        ) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear Canvas")
                        }

                        // Save Button
                        Button(
                            onClick = {
                                val json = NoteUtils.serializeDrawingStrokes(strokes)
                                onSave(json)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(AppRadius.md)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সংরক্ষণ")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.xs))

                // Canvas Drawing Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(AppRadius.md))
                        .background(Color.White)
                        .border(
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            RoundedCornerShape(AppRadius.md)
                        )
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(isEraserMode, currentColorHex, currentStrokeWidth) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPoints.clear()
                                        currentPoints.add(DrawingPoint(offset.x, offset.y))
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentPoints.add(DrawingPoint(change.position.x, change.position.y))
                                    },
                                    onDragEnd = {
                                        if (currentPoints.isNotEmpty()) {
                                            val strokeColor = if (isEraserMode) "#FFFFFF" else currentColorHex
                                            val strokeWidth = if (isEraserMode) currentStrokeWidth * 3f else currentStrokeWidth
                                            strokes.add(
                                                DrawingStroke(
                                                    points = currentPoints.toList(),
                                                    colorHex = strokeColor,
                                                    strokeWidth = strokeWidth
                                                )
                                            )
                                            currentPoints.clear()
                                        }
                                    },
                                    onDragCancel = {
                                        currentPoints.clear()
                                    }
                                )
                            }
                    ) {
                        // Draw completed strokes
                        strokes.forEach { stroke ->
                            if (stroke.points.size > 1) {
                                val path = Path().apply {
                                    moveTo(stroke.points.first().x, stroke.points.first().y)
                                    for (i in 1 until stroke.points.size) {
                                        lineTo(stroke.points[i].x, stroke.points[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = Color(android.graphics.Color.parseColor(stroke.colorHex)),
                                    style = Stroke(
                                        width = stroke.strokeWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            } else if (stroke.points.size == 1) {
                                drawCircle(
                                    color = Color(android.graphics.Color.parseColor(stroke.colorHex)),
                                    radius = stroke.strokeWidth / 2f,
                                    center = Offset(stroke.points.first().x, stroke.points.first().y)
                                )
                            }
                        }

                        // Draw live active stroke
                        if (currentPoints.size > 1) {
                            val activeColor = if (isEraserMode) Color.LightGray else Color(android.graphics.Color.parseColor(currentColorHex))
                            val path = Path().apply {
                                moveTo(currentPoints.first().x, currentPoints.first().y)
                                for (i in 1 until currentPoints.size) {
                                    lineTo(currentPoints[i].x, currentPoints[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = activeColor,
                                style = Stroke(
                                    width = if (isEraserMode) currentStrokeWidth * 3f else currentStrokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    if (strokes.isEmpty() && currentPoints.isEmpty()) {
                        Text(
                            text = "এখানে আঙুল বা পেন দিয়ে স্কেচ আঁকুন...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                // Toolbar Controls: Stroke Width, Eraser Mode, Color Palette
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppRadius.md),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        // Row 1: Brush Size and Eraser Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Brush sizes
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(3f to "Fine", 7f to "Medium", 14f to "Bold").forEach { (width, label) ->
                                    val isSelected = currentStrokeWidth == width && !isEraserMode
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            currentStrokeWidth = width
                                            isEraserMode = false
                                        },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                        shape = RoundedCornerShape(AppRadius.full)
                                    )
                                }
                            }

                            // Eraser Mode
                            FilterChip(
                                selected = isEraserMode,
                                onClick = { isEraserMode = !isEraserMode },
                                label = { Text("Eraser (মুছুন)", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.AutoFixNormal,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(AppRadius.full)
                            )
                        }

                        // Row 2: Color Palette
                        if (!isEraserMode) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PALETTE_COLORS.forEach { hex ->
                                    val isSelected = currentColorHex.equals(hex, ignoreCase = true)
                                    val color = Color(android.graphics.Color.parseColor(hex))
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                currentColorHex = hex
                                                isEraserMode = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (hex == "#000000" || hex == "#0D47A1" || hex == "#C62828") Color.White else Color.Black,
                                                modifier = Modifier.size(16.dp)
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
    }
}

@Composable
fun NoteDrawingThumbnail(
    drawingJson: String,
    modifier: Modifier = Modifier
) {
    val strokes = remember(drawingJson) { NoteUtils.parseDrawingStrokes(drawingJson) }
    if (strokes.isEmpty()) return

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(Color.White)
            .border(BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)), RoundedCornerShape(AppRadius.sm))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minX = strokes.flatMap { it.points }.minOfOrNull { it.x } ?: 0f
            val maxX = strokes.flatMap { it.points }.maxOfOrNull { it.x } ?: size.width
            val minY = strokes.flatMap { it.points }.minOfOrNull { it.y } ?: 0f
            val maxY = strokes.flatMap { it.points }.maxOfOrNull { it.y } ?: size.height

            val contentWidth = (maxX - minX).coerceAtLeast(1f)
            val contentHeight = (maxY - minY).coerceAtLeast(1f)

            val scaleX = (size.width - 16f) / contentWidth
            val scaleY = (size.height - 16f) / contentHeight
            val scale = minOf(scaleX, scaleY).coerceAtMost(1f)

            strokes.forEach { stroke ->
                if (stroke.points.size > 1) {
                    val path = Path().apply {
                        val first = stroke.points.first()
                        moveTo(8f + (first.x - minX) * scale, 8f + (first.y - minY) * scale)
                        for (i in 1 until stroke.points.size) {
                            val pt = stroke.points[i]
                            lineTo(8f + (pt.x - minX) * scale, 8f + (pt.y - minY) * scale)
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color(android.graphics.Color.parseColor(stroke.colorHex)),
                        style = Stroke(
                            width = (stroke.strokeWidth * scale).coerceAtLeast(1.5f),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
