package com.example.peers.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Definition and helper utilities for the 67 categorized PEERS avatars across 4 subfolders.
 */
object PeersAvatars {
    val categories = listOf("Female", "Male", "Neutral", "Formal")

    fun getAvatarsForCategory(category: String): List<String> {
        return when (category.lowercase()) {
            "female" -> (1..24).map { "female/woman$it.png" }
            "male" -> (1..24).map { "male/man$it.png" }
            "neutral" -> (1..15).map { "neutral/neutral$it.png" }
            "formal" -> (1..4).map { "formal/formal$it.png" }
            else -> (1..15).map { "neutral/neutral$it.png" }
        }
    }

    fun normalizePath(path: Any?): String {
        return when (path) {
            is String -> {
                if (path.isNotBlank()) path
                else "neutral/neutral1.png"
            }
            is Number -> {
                // Fallback for legacy int indices
                "neutral/neutral1.png"
            }
            else -> "neutral/neutral1.png"
        }
    }
}

/**
 * Reusable avatar circle rendering user's photo or category avatar asset.
 */
@Composable
fun PeersAvatarBadge(
    avatarIndex: Any?,
    photoUrl: String? = null,
    size: Dp = 64.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
) {
    val normalizedPath = PeersAvatars.normalizePath(avatarIndex)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, CircleShape)
                else Modifier
            ),
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AsyncImage(
                model = "file:///android_asset/avatars/$normalizedPath",
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
