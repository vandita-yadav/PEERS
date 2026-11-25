package com.example.peers.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size

/** Returns true when the active Material color scheme has a dark background. */
@Composable
fun isPeersDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/**
 * Renders the user-provided black (light mode) or white (dark mode) vector icon
 * from `res/drawable` automatically based on the active theme.
 */
@Composable
fun PeersDrawableIcon(
    @DrawableRes lightRes: Int,
    @DrawableRes darkRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color? = null,
) {
    val isDark = isPeersDarkTheme()
    val resId = if (isDark) darkRes else lightRes
    if (tint != null) {
        Icon(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier.size(size),
        )
    } else {
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = modifier.size(size),
        )
    }
}
