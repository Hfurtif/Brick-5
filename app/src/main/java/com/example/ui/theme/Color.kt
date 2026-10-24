package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val DarkBrickColors = BrickColorTokens(
    background = Color(0xFF0A0E1A),
    backgroundSecondary = Color(0xFF101726),
    surface = Color(0xFF131B2E),
    surfaceElevated = Color(0xFF1A233D),
    surfaceBorder = Color(0xFF243052),
    textPrimary = Color(0xFFF1F5F9),
    textMuted = Color(0xFF94A3B8),
    textDark = Color(0xFF64748B),
    primary = Color(0xFF00E5FF),
    primaryDim = Color(0xFF00A3B5),
    secondary = Color(0xFF8B5CF6),
    secondaryGlow = Color(0xFFA78BFA),
    cyberGold = Color(0xFFFFB800),
    cyberGoldGlow = Color(0xFFFFD700),
    emeraldSuccess = Color(0xFF10B981),
    coralDanger = Color(0xFFEF4444),
    amberWarning = Color(0xFFF59E0B),
    glassBorder = Color(0x3300E5FF),
    isDark = true
)

val LightBrickColors = BrickColorTokens(
    background = Color(0xFFF8FAFC),
    backgroundSecondary = Color(0xFFF1F5F9),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF1F5F9),
    surfaceBorder = Color(0xFFCBD5E1),
    textPrimary = Color(0xFF0F172A),
    textMuted = Color(0xFF64748B),
    textDark = Color(0xFF475569),
    primary = Color(0xFF0284C7),
    primaryDim = Color(0xFF0369A1),
    secondary = Color(0xFF7C3AED),
    secondaryGlow = Color(0xFF8B5CF6),
    cyberGold = Color(0xFFD97706),
    cyberGoldGlow = Color(0xFFB45309),
    emeraldSuccess = Color(0xFF059669),
    coralDanger = Color(0xFFDC2626),
    amberWarning = Color(0xFFD97706),
    glassBorder = Color(0x330284C7),
    isDark = false
)

val LocalBrickColors = staticCompositionLocalOf { DarkBrickColors }

val BrickBackground: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.background

val BrickBackgroundSecondary: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.backgroundSecondary

val BrickSurface: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.surface

val BrickSurfaceElevated: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.surfaceElevated

val BrickSurfaceBorder: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.surfaceBorder

val BrickTextPrimary: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.textPrimary

val BrickTextMuted: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.textMuted

val BrickTextDark: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.textDark

val BrickPrimary: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.primary

val BrickPrimaryDim: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.primaryDim

val BrickSecondary: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.secondary

val BrickSecondaryGlow: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.secondaryGlow

val CyberGold: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.cyberGold

val CyberGoldGlow: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.cyberGoldGlow

val EmeraldSuccess: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.emeraldSuccess

val CoralDanger: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.coralDanger

val AmberWarning: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.amberWarning

val NeedColor: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.primary

val WantColor: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.secondaryGlow

val SaveColor: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.cyberGold

val GlassWhite10: Color
    @Composable @ReadOnlyComposable get() = if (LocalBrickColors.current.isDark) Color(0x1AFFFFFF) else Color(0x120F172A)

val GlassWhite05: Color
    @Composable @ReadOnlyComposable get() = if (LocalBrickColors.current.isDark) Color(0x0EFFFFFF) else Color(0x060F172A)

val GlassBorder: Color
    @Composable @ReadOnlyComposable get() = LocalBrickColors.current.glassBorder
