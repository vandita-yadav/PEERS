package com.example.peers.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky

/**
 * Definition of the 13 predefined PEERS student avatars (Part 2 specification).
 */
data class PeerAvatarDefinition(
    val id: Int,
    val title: String,
    val emoji: String,
    val description: String,
    val backgroundColor: Color,
)

object PeersAvatars {
    val palette = listOf(
        PeersPrimaryIndigo, // #9FA1FF
        PeersLavender,      // #B5BAFF
        PeersSky,           // #AEE2FF
        PeersMint,          // #D9F9DF
    )

    val list: List<PeerAvatarDefinition> = listOf(
        PeerAvatarDefinition(0, "African Man", "🧑🏿", "Short hair, smiling face", palette[0]),
        PeerAvatarDefinition(1, "African Woman", "👩🏿", "Afro hairstyle, smiling face", palette[1]),
        PeerAvatarDefinition(2, "North Indian Boy", "👦🏽", "Short hair, friendly smile", palette[2]),
        PeerAvatarDefinition(3, "Northeast Indian Girl", "👧🏼", "Shoulder-length hair, fringe", palette[3]),
        PeerAvatarDefinition(4, "South Indian Girl", "👩🏽", "Long hair, traditional style", palette[0]),
        PeerAvatarDefinition(5, "Sikh Man", "🧔🏽", "Dark turban and beard", palette[1]),
        PeerAvatarDefinition(6, "Muslim Woman", "🧕🏾", "Hijab silhouette", palette[2]),
        PeerAvatarDefinition(7, "Middle Eastern Man", "🧔🏻", "Short beard, thick eyebrows", palette[3]),
        PeerAvatarDefinition(8, "East Asian Woman", "👩🏻", "Bob cut, straight fringe", palette[0]),
        PeerAvatarDefinition(9, "East Asian Man", "🧑🏻", "Side-parted straight hair", palette[1]),
        PeerAvatarDefinition(10, "Southeast Asian Woman", "👩🏽‍🦱", "Long wavy hair", palette[2]),
        PeerAvatarDefinition(11, "Latina Woman", "👱🏽‍♀️", "Curly shoulder-length hair", palette[3]),
        PeerAvatarDefinition(12, "Neutral Student", "🧑‍🎓", "Gender-neutral, round glasses", palette[0]),
    )

    fun get(index: Int?): PeerAvatarDefinition {
        if (index == null || index < 0 || index >= list.size) {
            return list.last()
        }
        return list[index]
    }
}

/**
 * Reusable avatar circle rendering user's photo or avatar.
 */
@Composable
fun PeersAvatarBadge(
    avatarIndex: Int?,
    photoUrl: String? = null,
    size: Dp = 64.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
) {
    val avatar = PeersAvatars.get(avatarIndex)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatar.backgroundColor)
            .then(
                if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, CircleShape)
                else Modifier
            ),
    ) {
        if (avatarIndex != null) {
            Text(
                avatar.emoji,
                fontSize = (size.value * 0.48f).sp,
            )
        } else if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = avatar.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                "🧑‍🎓",
                fontSize = (size.value * 0.48f).sp,
            )
        }
    }
}
