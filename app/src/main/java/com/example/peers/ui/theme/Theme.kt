package com.example.peers.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class PeersThemeMode { SystemDefault, AestheticLight, NeonDark }

private val NeonDarkColorScheme = darkColorScheme(
    primary = PeersPrimaryIndigo,
    background = androidx.compose.ui.graphics.Color(0xFF0C0A18),
    surface = androidx.compose.ui.graphics.Color(0xFF171329),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF25203D),
    onBackground = androidx.compose.ui.graphics.Color(0xFFF4F0FF),
    onSurface = androidx.compose.ui.graphics.Color(0xFFF4F0FF),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC5BCD9),
    outline = androidx.compose.ui.graphics.Color(0xFF776B9C)
)

private val AestheticLightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF6250A4),
    background = androidx.compose.ui.graphics.Color(0xFFFFF9F3),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF2EAF7),
    onBackground = androidx.compose.ui.graphics.Color(0xFF30283B),
    onSurface = androidx.compose.ui.graphics.Color(0xFF30283B),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF665C70),
    outline = androidx.compose.ui.graphics.Color(0xFFD8CCDD)
)

@Composable
fun PeersTheme(
    mode: PeersThemeMode = PeersThemeMode.SystemDefault,
    content: @Composable () -> Unit
) {
    val darkTheme = when (mode) {
        PeersThemeMode.NeonDark -> true
        PeersThemeMode.AestheticLight -> false
        PeersThemeMode.SystemDefault -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) NeonDarkColorScheme else AestheticLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming default Typography.kt is present
        content = content
    )
}
