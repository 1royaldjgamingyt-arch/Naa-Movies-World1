package com.example.admin.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ColorBorderHighlight = Color(0xFF33334B)

private val AdminDarkColorScheme = darkColorScheme(
    primary = CinemaCrimson,
    onPrimary = TextPrimary,
    primaryContainer = CinemaCrimsonDeep,
    onPrimaryContainer = TextPrimary,
    secondary = CinemaGold,
    onSecondary = ObsidianBlack,
    secondaryContainer = ObsidianElevated,
    onSecondaryContainer = CinemaGoldSoft,
    tertiary = ElectricCyan,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ObsidianBlack,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianElevated,
    surfaceContainerHighest = ColorBorderHighlight,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorder.copy(alpha = 0.6f),
    error = CinemaCrimsonBright,
    onError = TextPrimary
)

val AdminShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun NaaMoviesAdminTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AdminDarkColorScheme,
        typography = AppTypography,
        shapes = AdminShapes,
        content = content
    )
}
