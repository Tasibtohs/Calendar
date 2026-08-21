package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing

/**
 * Alert severity variants for error/warning banners.
 */
enum class AppAlertVariant {
    Error,
    Warning,
    Info
}

/**
 * Full-screen or large container Error State adhering strictly to the M3 Design System.
 */
@Composable
fun AppErrorState(
    title: String = "Something went wrong",
    message: String = "An unexpected error occurred. Please try again.",
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.ErrorOutline,
    errorCode: String? = null,
    technicalDetails: String? = null,
    retryLabel: String? = "Try Again",
    onRetry: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    var isDetailsExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_error_state"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.xxl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Error Icon Badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            // Error Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (errorCode != null) {
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Surface(
                    shape = RoundedCornerShape(AppRadius.xs),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = AppSpacing.xxs)
                ) {
                    Text(
                        text = "ERROR: $errorCode",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            // Error Subtitle / Description
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = AppSpacing.md)
            )

            // Technical Details Expander (Optional)
            if (!technicalDetails.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.md))
                TextButton(
                    onClick = { isDetailsExpanded = !isDetailsExpanded },
                    shape = RoundedCornerShape(AppRadius.sm)
                ) {
                    Text(
                        text = if (isDetailsExpanded) "Hide Details" else "View Technical Details",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(
                    visible = isDetailsExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(AppRadius.md),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppSpacing.xs)
                    ) {
                        Text(
                            text = technicalDetails,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(AppSpacing.md)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xl))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    AppButton(
                        text = secondaryActionLabel,
                        onClick = onSecondaryAction,
                        variant = AppButtonVariant.Outlined,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                if (retryLabel != null && onRetry != null) {
                    AppButton(
                        text = retryLabel,
                        onClick = onRetry,
                        variant = AppButtonVariant.Primary,
                        icon = Icons.Outlined.Refresh,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

/**
 * Inline Error / Alert Banner for embedded usage in dialogs, forms, or cards.
 */
@Composable
fun AppErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    variant: AppAlertVariant = AppAlertVariant.Error,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismissClick: (() -> Unit)? = null
) {
    val containerColor = when (variant) {
        AppAlertVariant.Error -> MaterialTheme.colorScheme.errorContainer
        AppAlertVariant.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        AppAlertVariant.Info -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when (variant) {
        AppAlertVariant.Error -> MaterialTheme.colorScheme.onErrorContainer
        AppAlertVariant.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
        AppAlertVariant.Info -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val iconColor = when (variant) {
        AppAlertVariant.Error -> MaterialTheme.colorScheme.error
        AppAlertVariant.Warning -> MaterialTheme.colorScheme.tertiary
        AppAlertVariant.Info -> MaterialTheme.colorScheme.secondary
    }

    val icon = when (variant) {
        AppAlertVariant.Error -> Icons.Outlined.ErrorOutline
        AppAlertVariant.Warning -> Icons.Outlined.WarningAmber
        AppAlertVariant.Info -> Icons.Outlined.Info
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_error_banner"),
        shape = RoundedCornerShape(AppRadius.md),
        color = containerColor,
        border = BorderStroke(1.dp, iconColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)
            ) {
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = contentColor
                    )
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }

            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                TextButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(AppRadius.sm),
                    colors = ButtonDefaults.textButtonColors(contentColor = iconColor)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * Compact Inline Error text with micro-icon for form fields or compact cells.
 */
@Composable
fun AppErrorInline(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_error_inline"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs)
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Standard Pop-up Error Dialog built on Design System tokens.
 */
@Composable
fun AppErrorDialog(
    title: String = "Error",
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = "OK",
    onConfirm: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppRadius.dialog),
        modifier = modifier.testTag("app_error_dialog"),
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.ReportProblem,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm?.invoke()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = if (onConfirm != null) {
            {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Cancel")
                }
            }
        } else null
    )
}
