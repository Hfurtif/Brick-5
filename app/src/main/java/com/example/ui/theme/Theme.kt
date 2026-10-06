package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkMaterialColorScheme = darkColorScheme(
    primary = DarkBrickColors.primary,
    onPrimary = DarkBrickColors.background,
    primaryContainer = DarkBrickColors.surfaceElevated,
    onPrimaryContainer = DarkBrickColors.primary,
    secondary = DarkBrickColors.secondaryGlow,
    onSecondary = DarkBrickColors.background,
    secondaryContainer = DarkBrickColors.surfaceElevated,
    onSecondaryContainer = DarkBrickColors.secondaryGlow,
    tertiary = DarkBrickColors.cyberGold,
    onTertiary = DarkBrickColors.background,
    background = DarkBrickColors.background,
    onBackground = DarkBrickColors.textPrimary,
    surface = DarkBrickColors.surface,
    onSurface = DarkBrickColors.textPrimary,
    surfaceVariant = DarkBrickColors.surfaceElevated,
    onSurfaceVariant = DarkBrickColors.textMuted,
    outline = DarkBrickColors.surfaceBorder,
    error = DarkBrickColors.coralDanger,
    onError = DarkBrickColors.textPrimary
)

private val LightMaterialColorScheme = lightColorScheme(
    primary = LightBrickColors.primary,
    onPrimary = Color.White,
    primaryContainer = LightBrickColors.surfaceElevated,
    onPrimaryContainer = LightBrickColors.primary,
    secondary = LightBrickColors.secondary,
    onSecondary = Color.White,
    secondaryContainer = LightBrickColors.surfaceElevated,
    onSecondaryContainer = LightBrickColors.secondary,
    tertiary = LightBrickColors.cyberGold,
    onTertiary = Color.White,
    background = LightBrickColors.background,
    onBackground = LightBrickColors.textPrimary,
    surface = LightBrickColors.surface,
    onSurface = LightBrickColors.textPrimary,
    surfaceVariant = LightBrickColors.surfaceElevated,
    onSurfaceVariant = LightBrickColors.textMuted,
    outline = LightBrickColors.surfaceBorder,
    error = LightBrickColors.coralDanger,
    onError = Color.White
)

@Composable
fun BrickTheme(
    themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT"
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark // Automatic system detection
    }

    val brickColors = if (isDark) DarkBrickColors else LightBrickColors
    val materialColorScheme = if (isDark) DarkMaterialColorScheme else LightMaterialColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = brickColors.background.toArgb()
                window.navigationBarColor = brickColors.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalBrickColors provides brickColors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}
