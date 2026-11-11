package com.example.peers.features.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.features.profile.components.PeersIndigoAccent
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import java.time.LocalTime

private enum class DayPart { MORNING, AFTERNOON, EVENING, NIGHT }

/**
 * Common dashboard header: hamburger menu, time-based greeting + university,
 * and a notification bell with an unread badge.
 */
@Composable
fun PeersHeader(
    user: DashboardUser?,
    unreadCount: Int,
    onMenuClick: () -> Unit,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayPart = when {
        LocalTime.now().hour < 12 -> DayPart.MORNING
        LocalTime.now().hour < 17 -> DayPart.AFTERNOON
        LocalTime.now().hour < 21 -> DayPart.EVENING
        else -> DayPart.NIGHT
    }
    val greeting = when (dayPart) {
        DayPart.MORNING -> "Good Morning"
        DayPart.AFTERNOON -> "Good Afternoon"
        DayPart.EVENING -> "Good Evening"
        DayPart.NIGHT -> "Good Night"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                Icons.Rounded.Menu,
                contentDescription = "Open menu",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = "$greeting,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = user?.name?.substringBefore(" ") ?: "Peer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (!user?.university.isNullOrBlank()) {
                Text(
                    text = user?.university.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        PeersAvatar(user, Modifier.size(44.dp))

        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(containerColor = PeersPrimaryIndigo) { Text("$unreadCount") }
                }
            },
            modifier = Modifier.padding(start = 4.dp),
        ) {
            IconButton(onClick = onBellClick) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = PeersPrimaryIndigo,
                )
            }
        }
    }
}

/** Circular avatar: photo if present, otherwise an emoji on a brand pastel. */
@Composable
fun PeersAvatar(user: DashboardUser?, size: Modifier = Modifier) {
    val avatarEmojis = listOf("🧑🏿", "👩🏿", "👦🏽", "👧🏼", "👩🏽", "🧔🏽", "🧕🏾")
    val avatarColors = listOf(
        PeersIndigoAccent,
        PeersLavender,
        PeersSky,
        PeersMint,
    )
    val index = (user?.uid?.hashCode() ?: 0).mod(avatarEmojis.size)

    Surface(shape = CircleShape, modifier = size) {
        if (!user?.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = user?.photoUrl,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(avatarColors[index]),
            ) {
                Text(avatarEmojis[index], style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

/** Small colored dot used as the event-indicator on date chips. */
@Composable
fun EventDot(color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(color)
    )
}
