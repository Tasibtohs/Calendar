package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Color Palette (Navy Blue Primary, Emerald Secondary, Amber Accent)
val PrimaryLight = Color(0xFF0D47A1)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFDDE6F7)
val OnPrimaryContainerLight = Color(0xFF001B3F)

val SecondaryLight = Color(0xFF0F766E)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCCFBF1)
val OnSecondaryContainerLight = Color(0xFF042F2E)

val TertiaryLight = Color(0xFFB45309)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFEF3C7)
val OnTertiaryContainerLight = Color(0xFF451A03)

val BackgroundLight = Color(0xFFF8FAFC)
val OnBackgroundLight = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF0F172A)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceVariantLight = Color(0xFF475569)

val OutlineLight = Color(0xFF94A3B8)
val OutlineVariantLight = Color(0xFFE2E8F0)
val ErrorLight = Color(0xFFDC2626)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF7F1D1D)

// Dark Theme Color Palette (Crisp Indigo-Blue Primary, Mint Secondary, Warm Gold Tertiary, Slate Dark Canvas)
val PrimaryDark = Color(0xFF93C5FD)
val OnPrimaryDark = Color(0xFF0A2540)
val PrimaryContainerDark = Color(0xFF1E3A8A)
val OnPrimaryContainerDark = Color(0xFFDBEAFE)

val SecondaryDark = Color(0xFF2DD4BF)
val OnSecondaryDark = Color(0xFF042F2E)
val SecondaryContainerDark = Color(0xFF134E4A)
val OnSecondaryContainerDark = Color(0xFFCCFBF1)

val TertiaryDark = Color(0xFFFBBF24)
val OnTertiaryDark = Color(0xFF451A03)
val TertiaryContainerDark = Color(0xFF78350F)
val OnTertiaryContainerDark = Color(0xFFFEF3C7)

val BackgroundDark = Color(0xFF0F172A)
val OnBackgroundDark = Color(0xFFF8FAFC)
val SurfaceDark = Color(0xFF1E293B)
val OnSurfaceDark = Color(0xFFF8FAFC)
val SurfaceVariantDark = Color(0xFF334155)
val OnSurfaceVariantDark = Color(0xFFCBD5E1)

val OutlineDark = Color(0xFF64748B)
val OutlineVariantDark = Color(0xFF334155)
val ErrorDark = Color(0xFFF87171)
val OnErrorDark = Color(0xFF450A0A)
val ErrorContainerDark = Color(0xFF7F1D1D)
val OnErrorContainerDark = Color(0xFFFEE2E2)

// AMOLED Pure Black Backgrounds
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0C0F14)
val AmoledSurfaceVariant = Color(0xFF161B22)

// Theme Accent Palettes
data class AccentColorConfig(
    val id: String,
    val name: String,
    val nameBn: String,
    val previewColor: Color,
    val primaryLight: Color,
    val primaryContainerLight: Color,
    val primaryDark: Color,
    val primaryContainerDark: Color,
    val secondaryLight: Color,
    val secondaryDark: Color
)

val AvailableAccents = listOf(
    AccentColorConfig(
        id = "RoyalBlue",
        name = "Royal Navy Blue",
        nameBn = "রয়্যাল নেভি ব্লু",
        previewColor = Color(0xFF0D47A1),
        primaryLight = Color(0xFF0D47A1),
        primaryContainerLight = Color(0xFFDDE6F7),
        primaryDark = Color(0xFF93C5FD),
        primaryContainerDark = Color(0xFF1E3A8A),
        secondaryLight = Color(0xFF0F766E),
        secondaryDark = Color(0xFF2DD4BF)
    ),
    AccentColorConfig(
        id = "EmeraldGreen",
        name = "Islamic Emerald Green",
        nameBn = "ইসলামিক পান্না সবুজ",
        previewColor = Color(0xFF15803D),
        primaryLight = Color(0xFF15803D),
        primaryContainerLight = Color(0xFFDCFCE7),
        primaryDark = Color(0xFF86EFAC),
        primaryContainerDark = Color(0xFF14532D),
        secondaryLight = Color(0xFF0D9488),
        secondaryDark = Color(0xFF5EEAD4)
    ),
    AccentColorConfig(
        id = "SunsetAmber",
        name = "Sunset Amber / Orange",
        nameBn = "সূর্যাস্ত অ্যাম্বার",
        previewColor = Color(0xFFC2410C),
        primaryLight = Color(0xFFC2410C),
        primaryContainerLight = Color(0xFFFFEDD5),
        primaryDark = Color(0xFFFDBA74),
        primaryContainerDark = Color(0xFF7C2D12),
        secondaryLight = Color(0xFFD97706),
        secondaryDark = Color(0xFFFCD34D)
    ),
    AccentColorConfig(
        id = "DeepPurple",
        name = "Deep Purple / Violet",
        nameBn = "গাঢ় বেগুনি",
        previewColor = Color(0xFF6B21A8),
        primaryLight = Color(0xFF6B21A8),
        primaryContainerLight = Color(0xFFF3E8FF),
        primaryDark = Color(0xFFD8B4FE),
        primaryContainerDark = Color(0xFF581C87),
        secondaryLight = Color(0xFF9333EA),
        secondaryDark = Color(0xFFE9D5FF)
    ),
    AccentColorConfig(
        id = "RubyCrimson",
        name = "Ruby Crimson / Red",
        nameBn = "রুবি লাল",
        previewColor = Color(0xFFB91C1C),
        primaryLight = Color(0xFFB91C1C),
        primaryContainerLight = Color(0xFFFEE2E2),
        primaryDark = Color(0xFFFCA5A5),
        primaryContainerDark = Color(0xFF7F1D1D),
        secondaryLight = Color(0xFFBE123C),
        secondaryDark = Color(0xFFFDA4AF)
    ),
    AccentColorConfig(
        id = "OceanTeal",
        name = "Ocean Cyan / Teal",
        nameBn = "সাগর নীল / টিল",
        previewColor = Color(0xFF0E7490),
        primaryLight = Color(0xFF0E7490),
        primaryContainerLight = Color(0xFFCFFAFE),
        primaryDark = Color(0xFF67E8F9),
        primaryContainerDark = Color(0xFF164E63),
        secondaryLight = Color(0xFF0284C7),
        secondaryDark = Color(0xFF7DD3FC)
    )
)
