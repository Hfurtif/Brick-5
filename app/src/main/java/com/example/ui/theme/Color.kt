package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class BrickColorTokens(
    val background: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val textDark: Color,
    val primary: Color,
    val primaryDim: Color,
    val secondary: Color,
    val secondaryGlow: Color,
    val cyberGold: Color,
    val cyberGoldGlow: Color,
    val emeraldSuccess: Color,
    val coralDanger: Color,
    val amberWarning: Color,
    val glassBorder: Color,
    val isDark: Boolean
)

// BRICK Cyber-Dark Palette
val DarkBrickColors = BrickColorTokens(
    background = Color(0xFF070B14),
    backgroundSecondary = Color(0xFF0D1322),
    surface = Color(0xFF131B2E),
    surfaceElevated = Color(0xFF1A243D),
    surfaceBorder = Color(0xFF243252),
    textPrimary = Color(0xFFF8FAFC),
    textMuted = Color(0xFF94A3B8),
    textDark = Color(0xFF64748B),
    primary = Color(0xFF00E5FF), // Electric Cyan
    primaryDim = Color(0xFF00A3B5),
    secondary = Color(0xFF9D4EDD), // Neon Violet
    secondaryGlow = Color(0xFFC77DFF),
    cyberGold = Color(0xFFFFB703),
    cyberGoldGlow = Color(0xFFFFD166),
    emeraldSuccess = Color(0xFF10B981),
    coralDanger = Color(0xFFFF4D6D),
    amberWarning = Color(0xFFFB8500),
    glassBorder = Color(0x3300E5FF),
    isDark = true
)

// BRICK Cyber-Light Palette (Refined Slate / Pure White / Electric Marine)
val LightBrickColors = BrickColorTokens(
    background = Color(0xFFF1F5F9), // Crisp Slate 100
    backgroundSecondary = Color(0xFFE2E8F0), // Slate 200
    surface = Color(0xFFFFFFFF), // Pure White card surfaces
    surfaceElevated = Color(0xFFF8FAFC), // Slate 50
    surfaceBorder = Color(0xFFCBD5E1), // Slate 300
    textPrimary = Color(0xFF0F172A), // Deep Slate 900
    textMuted = Color(0xFF64748B), // Slate 500
    textDark = Color(0xFF475569), // Slate 600
    primary = Color(0xFF0284C7), // High-contrast Electric Cyan / Sky 600
    primaryDim = Color(0xFF0369A1), // Sky 700
    secondary = Color(0xFF7C3AED), // Vibrant Violet 600
    secondaryGlow = Color(0xFF8B5CF6), // Violet 500
    cyberGold = Color(0xFFD97706), // Amber 600
    cyberGoldGlow = Color(0xFFB45309), // Amber 700
    emeraldSuccess = Color(0xFF059669), // Emerald 600
    coralDanger = Color(0xFFDC2626), // Red 600
    amberWarning = Color(0xFFD97706), // Amber 600
    glassBorder = Color(0x330284C7),
    isDark = false
)

val LocalBrickColors = staticCompositionLocalOf { DarkBrickColors }

// Dynamic Composable Color Getters
val BrickBackground: Color
    @Composable get() = LocalBrickColors.current.background

val BrickBackgroundSecondary: Color
    @Composable get() = LocalBrickColors.current.backgroundSecondary

val BrickSurface: Color
    @Composable get() = LocalBrickColors.current.surface

val BrickSurfaceElevated: Color
    @Composable get() = LocalBrickColors.current.surfaceElevated

val BrickSurfaceBorder: Color
    @Composable get() = LocalBrickColors.current.surfaceBorder

val TextWhite: Color
    @Composable get() = LocalBrickColors.current.textPrimary

val TextMuted: Color
    @Composable get() = LocalBrickColors.current.textMuted

val TextDark: Color
    @Composable get() = LocalBrickColors.current.textDark

val ElectricCyan: Color
    @Composable get() = LocalBrickColors.current.primary

val ElectricCyanDim: Color
    @Composable get() = LocalBrickColors.current.primaryDim

val NeonViolet: Color
    @Composable get() = LocalBrickColors.current.secondary

val NeonVioletGlow: Color
    @Composable get() = LocalBrickColors.current.secondaryGlow

val CyberGold: Color
    @Composable get() = LocalBrickColors.current.cyberGold

val CyberGoldGlow: Color
    @Composable get() = LocalBrickColors.current.cyberGoldGlow

val EmeraldSuccess: Color
    @Composable get() = LocalBrickColors.current.emeraldSuccess

val CoralDanger: Color
    @Composable get() = LocalBrickColors.current.coralDanger

val AmberWarning: Color
    @Composable get() = LocalBrickColors.current.amberWarning

val NeedColor: Color
    @Composable get() = LocalBrickColors.current.primary

val WantColor: Color
    @Composable get() = LocalBrickColors.current.secondaryGlow

val SaveColor: Color
    @Composable get() = LocalBrickColors.current.cyberGold

val GlassWhite10: Color
    @Composable get() = if (LocalBrickColors.current.isDark) Color(0x1AFFFFFF) else Color(0x12000000)

val GlassWhite05: Color
    @Composable get() = if (LocalBrickColors.current.isDark) Color(0x0DFFFFFF) else Color(0x06000000)

val GlassBorder: Color
    @Composable get() = LocalBrickColors.current.glassBorder
