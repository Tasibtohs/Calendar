package com.example.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for Spacing, Corner Radius, Elevation and Dimension tokens across the app.
 */
object AppSpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
}

object AppRadius {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val dialog: Dp = 24.dp
    val sheet: Dp = 28.dp
    val full: Dp = 999.dp
}

object AppElevation {
    val none: Dp = 0.dp
    val low: Dp = 2.dp
    val medium: Dp = 4.dp
    val dialog: Dp = 6.dp
    val high: Dp = 8.dp
    val modal: Dp = 16.dp
}

object AppDimensions {
    val minTouchTarget: Dp = 48.dp
    val iconSmall: Dp = 16.dp
    val iconMedium: Dp = 20.dp
    val iconLarge: Dp = 24.dp
    val iconAvatar: Dp = 40.dp
    val buttonHeight: Dp = 48.dp
    val inputHeight: Dp = 56.dp
}

/**
 * Standard Motion & Animation timing tokens.
 * Fast, responsive and subtle micro-interactions.
 */
object AppMotion {
    const val durationInstant = 100
    const val durationFast = 180
    const val durationNormal = 280
    const val durationMedium = 380
    const val durationSlow = 500

    val springBouncy = androidx.compose.animation.core.spring<Float>(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
    )

    val springSubtle = androidx.compose.animation.core.spring<Float>(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
    )
}
