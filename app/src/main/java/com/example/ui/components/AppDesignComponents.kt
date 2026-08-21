package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppMotion
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing

/**
 * Micro-interaction modifier providing smooth, subtle tactile scale feedback on press.
 */
@Composable
fun Modifier.pressFeedback(
    pressedScale: Float = 0.96f,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = tween(durationMillis = AppMotion.durationInstant),
        label = "PressFeedbackScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Standard Reusable Buttons following unified M3 Design System.
 */
enum class AppButtonVariant {
    Primary,
    SecondaryTonal,
    Outlined,
    Danger
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val buttonColors = when (variant) {
        AppButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        AppButtonVariant.SecondaryTonal -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        AppButtonVariant.Outlined -> ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
        AppButtonVariant.Danger -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }

    val shape = RoundedCornerShape(AppRadius.md)
    val height = AppDimensions.buttonHeight

    val content = @Composable {
        if (isLoading) {
            AppCircularProgress(
                size = 20.dp,
                strokeWidth = 2.5.dp,
                color = when (variant) {
                    AppButtonVariant.Primary, AppButtonVariant.Danger -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.iconMedium)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    when (variant) {
        AppButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .pressFeedback(interactionSource = interactionSource)
                    .defaultMinSize(minHeight = height),
                enabled = enabled && !isLoading,
                interactionSource = interactionSource,
                shape = shape,
                colors = buttonColors,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                content()
            }
        }
        else -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .pressFeedback(interactionSource = interactionSource)
                    .defaultMinSize(minHeight = height),
                enabled = enabled && !isLoading,
                interactionSource = interactionSource,
                shape = shape,
                colors = buttonColors,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = AppElevation.none)
            ) {
                content()
            }
        }
    }
}

/**
 * Standard Reusable Card with subtle border, unified elevation, and consistent surface colors.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(AppRadius.lg),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = AppElevation.low,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            modifier = modifier
                .pressFeedback(pressedScale = 0.98f, interactionSource = interactionSource)
                .animateContentSize(),
            shape = shape,
            color = containerColor,
            tonalElevation = elevation,
            border = border,
            interactionSource = interactionSource
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.lg),
                content = content
            )
        }
    } else {
        Surface(
            modifier = modifier.animateContentSize(),
            shape = shape,
            color = containerColor,
            tonalElevation = elevation,
            border = border
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.lg),
                content = content
            )
        }
    }
}

/**
 * Standard Reusable Input / TextField with clear focus border and accessibility support.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
            placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppDimensions.iconMedium)
                    )
                }
            },
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            shape = RoundedCornerShape(AppRadius.md),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            AppErrorInline(
                text = errorMessage,
                modifier = Modifier.padding(start = AppSpacing.xs)
            )
        }
    }
}

/**
 * Standard Reusable Confirmation / Action Dialog with unified typography and actions.
 */
@Composable
fun AppConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    isDanger: Boolean = false,
    icon: ImageVector? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(AppRadius.sheet),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = AppElevation.modal,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(AppSpacing.md)
                .animateContentSize()
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDanger) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.iconLarge)
                        )
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppSpacing.xl))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    AppButton(
                        text = dismissText,
                        onClick = onDismissRequest,
                        variant = AppButtonVariant.SecondaryTonal,
                        modifier = Modifier.weight(1f)
                    )
                    AppButton(
                        text = confirmText,
                        onClick = {
                            onConfirm()
                            onDismissRequest()
                        },
                        variant = if (isDanger) AppButtonVariant.Danger else AppButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Standard Delete Confirmation Dialog with destructive theme and danger action.
 */
@Composable
fun AppDeleteDialog(
    onDismissRequest: () -> Unit,
    title: String = "Delete Confirmation",
    message: String,
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit
) {
    AppConfirmationDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm = onConfirm,
        isDanger = true,
        icon = Icons.Outlined.Delete
    )
}

/**
 * Standard Restore / Unarchive Confirmation Dialog.
 */
@Composable
fun AppRestoreDialog(
    onDismissRequest: () -> Unit,
    title: String = "Restore Item",
    message: String = "Do you want to restore this item back to your active list?",
    confirmText: String = "Restore",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit
) {
    AppConfirmationDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm = onConfirm,
        isDanger = false,
        icon = Icons.Outlined.Unarchive
    )
}

/**
 * Standard Clear / Reset Confirmation Dialog.
 */
@Composable
fun AppClearDialog(
    onDismissRequest: () -> Unit,
    title: String = "Clear All Items?",
    message: String = "Are you sure you want to clear all data? This action cannot be reverted.",
    confirmText: String = "Clear All",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit
) {
    AppConfirmationDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm = onConfirm,
        isDanger = true,
        icon = Icons.Outlined.CleaningServices
    )
}

/**
 * Standard Permission Rationale Dialog.
 */
@Composable
fun AppPermissionDialog(
    onDismissRequest: () -> Unit,
    title: String,
    description: String,
    grantButtonText: String = "Allow Permission",
    dismissText: String = "Not Now",
    icon: ImageVector = Icons.Outlined.Security,
    onGrant: () -> Unit
) {
    AppConfirmationDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = description,
        confirmText = grantButtonText,
        dismissText = dismissText,
        onConfirm = onGrant,
        isDanger = false,
        icon = icon
    )
}

/**
 * Standard Reusable Bottom Sheet Container with Drag Handle and unified header.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    trailingAction: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = AppRadius.sheet, topEnd = AppRadius.sheet),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = AppElevation.high,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outlineVariant
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xl)
                .padding(bottom = AppSpacing.xxl)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    trailingAction?.invoke()
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .pressFeedback()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(AppDimensions.iconSmall)
                        )
                    }
                }
            }

            content()
        }
    }
}

/**
 * Standard Reusable Chip for filters, tags, and category selection.
 */
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    colorAccent: Color? = null
) {
    val activeColor = colorAccent ?: MaterialTheme.colorScheme.primary
    val backgroundColor = if (selected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow
    val borderColor = if (selected) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val contentColor = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppRadius.full),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        interactionSource = interactionSource,
        modifier = modifier
            .pressFeedback(pressedScale = 0.94f, interactionSource = interactionSource)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(AppDimensions.iconSmall)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

/**
 * Standard Reusable Full/Large Empty State component.
 */
@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_empty_state"),
        shape = RoundedCornerShape(AppRadius.lg),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.md))

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
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = AppSpacing.md)
                )
            }

            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                AppButton(
                    text = actionText,
                    onClick = onActionClick,
                    variant = AppButtonVariant.SecondaryTonal
                )
            }
        }
    }
}

/**
 * Standard Reusable Compact / Inline Empty State component (used inside cards/sections).
 */
@Composable
fun AppCompactEmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onActionClick != null) Modifier.clickable { onActionClick() } else Modifier),
        shape = RoundedCornerShape(AppRadius.md),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppDimensions.iconSmall)
                    )
                }
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            if (actionText != null && onActionClick != null) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Standard Reusable Snackbar host content layout based on Design System tokens.
 */
@Composable
fun AppSnackbar(
    message: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismissClick: (() -> Unit)? = null,
    isError: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val icon = leadingIcon ?: when {
        isError -> Icons.Outlined.ErrorOutline
        actionLabel.equals("Undo", ignoreCase = true) -> Icons.Outlined.Undo
        message.contains("Deleted", ignoreCase = true) || message.contains("মুছে", ignoreCase = true) -> Icons.Outlined.DeleteOutline
        message.contains("Saved", ignoreCase = true) || message.contains("Success", ignoreCase = true) -> Icons.Outlined.CheckCircle
        else -> Icons.Outlined.Info
    }

    Surface(
        shape = RoundedCornerShape(AppRadius.lg),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.inverseSurface
        },
        border = BorderStroke(
            1.dp,
            if (isError) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.15f)
            }
        ),
        tonalElevation = AppElevation.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
            .testTag("app_snackbar")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Surface(
                shape = CircleShape,
                color = if (isError) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.2f)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.inversePrimary
                        },
                        modifier = Modifier.size(AppDimensions.iconSmall)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.sm))

            // Message text
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.inverseOnSurface
                },
                modifier = Modifier.weight(1f)
            )

            // Action Button
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                TextButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(AppRadius.sm),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.inversePrimary
                        }
                    ),
                    modifier = Modifier.testTag("snackbar_action_button")
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Dismiss Button
            if (onDismissClick != null) {
                IconButton(
                    onClick = onDismissClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Dismiss",
                        tint = if (isError) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                        },
                        modifier = Modifier.size(AppDimensions.iconSmall)
                    )
                }
            }
        }
    }
}

/**
 * Standard Reusable Snackbar Composable wrapping SnackbarData.
 */
@Composable
fun AppSnackbar(
    snackbarData: SnackbarData,
    isError: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    AppSnackbar(
        message = snackbarData.visuals.message,
        actionLabel = snackbarData.visuals.actionLabel,
        onActionClick = { snackbarData.performAction() },
        onDismissClick = { snackbarData.dismiss() },
        isError = isError,
        leadingIcon = leadingIcon
    )
}

/**
 * Standard Reusable SnackbarHost for host screens.
 */
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.testTag("app_snackbar_host")
    ) { snackbarData ->
        AppSnackbar(snackbarData = snackbarData)
    }
}
