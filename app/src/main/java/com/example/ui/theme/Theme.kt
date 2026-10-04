package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SafeWalkColorScheme = darkColorScheme(
    primary = AmethystPrimary,
    onPrimary = Color(0xFF1E0A3C),
    primaryContainer = AmethystDeep,
    onPrimaryContainer = Color(0xFFF3E5F5),
    secondary = GuardianCyan,
    onSecondary = Color(0xFF00363A),
    secondaryContainer = GuardianCyanDark,
    onSecondaryContainer = Color(0xFFE0F7FA),
    tertiary = SafeGreen,
    onTertiary = Color(0xFF003914),
    error = AlertRed,
    onError = Color.White,
    errorContainer = AlertRedDark,
    onErrorContainer = Color(0xFFFFEBEE),
    background = MidnightBase,
    onBackground = TextPrimaryDark,
    surface = MidnightCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = MidnightElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = MidnightBorder
)

@Composable
fun SafeWalkTheme(
    darkTheme: Boolean = true, // Force dark-first for night walking safety
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = MidnightBase.toArgb()
                it.navigationBarColor = MidnightBase.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = SafeWalkColorScheme,
        typography = Typography,
        content = content
    )
}
