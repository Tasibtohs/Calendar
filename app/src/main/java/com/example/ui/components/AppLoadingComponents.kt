package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing

/**
 * Shimmer Brush Builder adhering to the active Material 3 Color Scheme.
 */
@Composable
fun rememberShimmerBrush(
    targetValue: Float = 1300f,
    durationMillis: Int = 1200
): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
    )

    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation, y = translateAnimation)
    )
}

/**
 * Modifier extension to apply shimmer effect to any composable shape.
 */
@Composable
fun Modifier.shimmerLoading(
    shape: Shape = RoundedCornerShape(AppRadius.md)
): Modifier {
    val brush = rememberShimmerBrush()
    return this
        .clip(shape)
        .background(brush)
}

/**
 * Single Shimmer Box placeholder component.
 */
@Composable
fun AppShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AppRadius.md),
    height: Dp? = null,
    width: Dp? = null
) {
    val baseModifier = modifier
        .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
        .then(if (height != null) Modifier.height(height) else Modifier)
        .shimmerLoading(shape = shape)

    Box(modifier = baseModifier)
}

/**
 * Single Shimmer Text Line placeholder.
 */
@Composable
fun AppShimmerTextLine(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = 14.dp,
    shape: Shape = RoundedCornerShape(AppRadius.xs)
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .shimmerLoading(shape = shape)
    )
}

/**
 * Skeleton placeholder for a Task item card.
 */
@Composable
fun AppShimmerTaskItem(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shimmer_task_item"),
        shape = RoundedCornerShape(AppRadius.md),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox placeholder
            AppShimmerBox(
                modifier = Modifier.size(24.dp),
                shape = CircleShape
            )

            Spacer(modifier = Modifier.width(AppSpacing.md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                AppShimmerTextLine(widthFraction = 0.75f, height = 16.dp)
                AppShimmerTextLine(widthFraction = 0.45f, height = 12.dp)
            }

            Spacer(modifier = Modifier.width(AppSpacing.sm))

            // Chip placeholder
            AppShimmerBox(
                modifier = Modifier
                    .width(64.dp)
                    .height(24.dp),
                shape = RoundedCornerShape(AppRadius.full)
            )
        }
    }
}

/**
 * Skeleton placeholder for a List of Task items.
 */
@Composable
fun AppShimmerTaskList(
    count: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        repeat(count) {
            AppShimmerTaskItem()
        }
    }
}

/**
 * Skeleton placeholder for an Event item card.
 */
@Composable
fun AppShimmerEventItem(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shimmer_event_item"),
        shape = RoundedCornerShape(AppRadius.md),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color bar indicator
            AppShimmerBox(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(AppRadius.full)
            )

            Spacer(modifier = Modifier.width(AppSpacing.md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                AppShimmerTextLine(widthFraction = 0.65f, height = 16.dp)
                AppShimmerTextLine(widthFraction = 0.4f, height = 12.dp)
            }

            Spacer(modifier = Modifier.width(AppSpacing.sm))

            // Time pill placeholder
            AppShimmerBox(
                modifier = Modifier
                    .width(52.dp)
                    .height(22.dp),
                shape = RoundedCornerShape(AppRadius.full)
            )
        }
    }
}

/**
 * Skeleton placeholder for a List of Event items.
 */
@Composable
fun AppShimmerEventList(
    count: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        repeat(count) {
            AppShimmerEventItem()
        }
    }
}

/**
 * Skeleton placeholder for a Note card.
 */
@Composable
fun AppShimmerNoteCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shimmer_note_card"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppShimmerTextLine(widthFraction = 0.5f, height = 16.dp)
                AppShimmerBox(modifier = Modifier.size(16.dp), shape = CircleShape)
            }

            AppShimmerTextLine(widthFraction = 0.95f, height = 12.dp)
            AppShimmerTextLine(widthFraction = 0.8f, height = 12.dp)
            AppShimmerTextLine(widthFraction = 0.6f, height = 12.dp)

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppShimmerBox(
                    modifier = Modifier
                        .width(48.dp)
                        .height(20.dp),
                    shape = RoundedCornerShape(AppRadius.full)
                )
                AppShimmerTextLine(widthFraction = 0.3f, height = 10.dp)
            }
        }
    }
}

/**
 * Skeleton placeholder for Agenda section cards.
 */
@Composable
fun AppShimmerAgendaCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shimmer_agenda_card"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    AppShimmerBox(modifier = Modifier.size(20.dp), shape = CircleShape)
                    AppShimmerBox(modifier = Modifier.width(100.dp).height(16.dp))
                }
                AppShimmerBox(modifier = Modifier.size(18.dp), shape = CircleShape)
            }

            // Inner Items
            repeat(2) {
                AppShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(AppRadius.sm)
                )
            }
        }
    }
}

/**
 * Standard Circular Progress Indicator conforming to M3 Design System.
 */
@Composable
fun AppCircularProgress(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    strokeWidth: Dp = 3.5.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    CircularProgressIndicator(
        modifier = modifier
            .size(size)
            .testTag("app_circular_progress"),
        color = color,
        strokeWidth = strokeWidth,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round
    )
}

/**
 * Standard Linear Progress Bar conforming to M3 Design System.
 */
@Composable
fun AppLinearProgress(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    height: Dp = 6.dp
) {
    val shape = RoundedCornerShape(AppRadius.full)
    if (progress != null) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "LinearProgressAnimation"
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .testTag("app_linear_progress"),
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )
    } else {
        LinearProgressIndicator(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .testTag("app_linear_progress_indeterminate"),
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Full-screen or Container-level Loading State.
 */
@Composable
fun AppLoadingScreen(
    title: String = "Loading...",
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    indicatorColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_loading_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = indicatorColor.copy(alpha = 0.12f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AppCircularProgress(
                        size = 42.dp,
                        strokeWidth = 3.5.dp,
                        color = indicatorColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Compact / Inline Loading State for Cards or Containers.
 */
@Composable
fun AppCompactLoadingState(
    message: String = "Loading data...",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_compact_loading_state"),
        shape = RoundedCornerShape(AppRadius.md),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            AppCircularProgress(
                size = 20.dp,
                strokeWidth = 2.5.dp
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
