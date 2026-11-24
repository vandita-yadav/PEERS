package com.example.peers.ui.theme

import android.app.Activity
import android.os.Build
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

enum class PeersThemeMode { SystemDefault, AestheticLight, NeonDark }

private val NeonDarkColorScheme = darkColorScheme(
    primary = PeersPrimaryIndigo,
    onPrimary = Color.White,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
)

private val AestheticLightColorScheme = lightColorScheme(
    primary = PeersPrimaryIndigo,
    onPrimary = Color.White,
    background = LightBackground, // Plain white background as requested
    surface = LightSurface,       // Pure white surfaces
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
)

@Composable
fun PeersTheme(
    mode: PeersThemeMode = PeersThemeMode.SystemDefault,
    content: @Composable () -> Unit
) {
    val darkTheme = when (mode) {
        PeersThemeMode.NeonDark -> true
        PeersThemeMode.AestheticLight -> false
        PeersThemeMode.SystemDefault -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) NeonDarkColorScheme else AestheticLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val navBarTint = if (darkTheme) DarkSurface else Color(0xFFF3F4F6)
            @Suppress("DEPRECATION")
            run {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = navBarTint.toArgb()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                // Ensures system nav bar icons (Back, Home, Recents) are dark & crisp on light theme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
