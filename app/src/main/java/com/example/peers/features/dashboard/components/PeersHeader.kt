package com.example.peers.features.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.peers.R
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.features.profile.PeersAvatarBadge
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersDrawableIcon
import com.example.peers.ui.theme.PeersPrimaryIndigo
import java.time.LocalTime

private enum class DayPart { MORNING, AFTERNOON, EVENING, NIGHT }

/**
 * Common dashboard header: hamburger menu, time-based greeting + university,
 * clickable profile avatar/photo, and a notification bell with an unread badge.
 */
@Composable
fun PeersHeader(
    user: DashboardUser?,
    unreadCount: Int,
    onMenuClick: () -> Unit,
    onBellClick: () -> Unit,
    onProfileClick: () -> Unit = {},
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                Icons.Rounded.Menu,
                contentDescription = "Open menu",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(Modifier.weight(1f).padding(start = 4.dp)) {
            Text(
                text = "$greeting,",
                fontFamily = Lato,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = user?.name?.substringBefore(" ") ?: "Peer",
                fontFamily = GoogleSans,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (!user?.university.isNullOrBlank()) {
                Text(
                    text = user?.university.orEmpty(),
                    fontFamily = Lato,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Clickable Profile icon/avatar near Notification Bell -> opens Profile screen
        PeersAvatarBadge(
            avatarIndex = user?.avatarIndex,
            photoUrl = user?.photoUrl,
            size = 42.dp,
            borderWidth = 2.dp,
            borderColor = PeersPrimaryIndigo.copy(alpha = 0.5f),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onProfileClick)
        )

        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(containerColor = PeersPrimaryIndigo) { Text("$unreadCount") }
                }
            },
            modifier = Modifier.padding(start = 4.dp),
        ) {
            IconButton(onClick = onBellClick) {
                PeersDrawableIcon(
                    lightRes = R.drawable.notification_black,
                    darkRes = R.drawable.notification_white,
                    contentDescription = "Notifications",
                    size = 24.dp,
                )
            }
        }
    }
}

/**
 * Standard non-dashboard header: Back button + Screen Title + Subtitle +
 * optional extra action + Profile Avatar button + Notification Bell with badge.
 */
@Composable
fun PeersSubScreenHeader(
    title: String,
    subtitle: String = "",
    user: DashboardUser?,
    unreadCount: Int,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                ) {
                    Text(
                        text = title,
                        fontFamily = GoogleSans,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            fontFamily = Lato,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                trailingAction?.invoke()

                PeersAvatarBadge(
                    avatarIndex = user?.avatarIndex,
                    photoUrl = user?.photoUrl,
                    size = 38.dp,
                    borderWidth = 2.dp,
                    borderColor = PeersPrimaryIndigo.copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onProfileClick)
                )

                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = PeersPrimaryIndigo) { Text("$unreadCount") }
                        }
                    },
                    modifier = Modifier.padding(start = 2.dp),
                ) {
                    IconButton(onClick = onBellClick, modifier = Modifier.size(40.dp)) {
                        PeersDrawableIcon(
                            lightRes = R.drawable.notification_black,
                            darkRes = R.drawable.notification_white,
                            contentDescription = "Notifications",
                            size = 22.dp,
                        )
                    }
                }
            }
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            )
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
