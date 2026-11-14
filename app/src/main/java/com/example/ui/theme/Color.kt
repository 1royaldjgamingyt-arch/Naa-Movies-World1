package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Ultra-Premium Obsidian, Cinema Crimson & Metallic Gold Palette
val ObsidianDeep = Color(0xFF05050A)
val ObsidianBlack = Color(0xFF090910)
val ObsidianSurface = Color(0xFF11111B)
val ObsidianCard = Color(0xFF171725)
val ObsidianElevated = Color(0xFF212134)
val ObsidianBorder = Color(0xFF2F2F46)

// Glassmorphism Translucent Layers
val ObsidianGlass = Color(0xCF141422)
val ObsidianGlassStrong = Color(0xEB10101A)
val ObsidianGlassElevated = Color(0xD91F1F31)
val GlassSpecularHighlight = Color(0x2BFFFFFF)
val GlassBorderSubtle = Color(0x389CA3C8)

// Cinema Crimson
val CinemaCrimson = Color(0xFFE50914)
val CinemaCrimsonBright = Color(0xFFFF2B38)
val CinemaCrimsonDeep = Color(0xFF8C040C)

// Metallic Gold
val CinemaGold = Color(0xFFF5C518)
val CinemaGoldSoft = Color(0xFFFFE58F)
val CinemaGoldDeep = Color(0xFFB8860B)

val ElectricCyan = Color(0xFF38BDF8)
val EmeraldVerified = Color(0xFF22C55E)

val TextPrimary = Color(0xFFF9FAFE)
val TextSecondary = Color(0xFFAAB0CE)
val TextMuted = Color(0xFF727896)

object CinemaBrushes {
    val goldMetallic = Brush.linearGradient(
        colors = listOf(CinemaGoldSoft, CinemaGold, CinemaGoldDeep)
    )

    val crimsonAction = Brush.horizontalGradient(
        colors = listOf(CinemaCrimsonBright, CinemaCrimson, CinemaCrimsonDeep)
    )

    val glassRim = Brush.linearGradient(
        colors = listOf(
            GlassSpecularHighlight,
            CinemaGold.copy(alpha = 0.28f),
            ObsidianBorder.copy(alpha = 0.45f)
        )
    )

    val cardAmbientGlow = Brush.verticalGradient(
        colors = listOf(
            ObsidianGlassElevated,
            ObsidianGlass
        )
    )
}
