package com.example.peers.features.dashboard

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import com.example.peers.features.dashboard.components.PeersSubScreenHeader
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.peers.R
import com.example.peers.ui.theme.PeersDrawableIcon
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import com.example.peers.ui.theme.PeersBrandInk
import com.example.peers.ui.theme.PeersLavender
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val notifDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.ENGLISH)

/**
 * In-app notifications (Part 3 spec): grouped list with unread emphasis,
 * inline Accept/Decline, mark-all-read action and an empty state.
 */
@Composable
fun NotificationsScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()

    BackHandler { onBack() }

    // Local resolution of inline Accept/Decline (demo peers; real backend in Community phase).
    var resolvedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val groups = notifications.groupBy { dayLabel(it.timestampMillis) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PeersSubScreenHeader(
                title = "Notifications",
                subtitle = if (unreadCount > 0) "$unreadCount unread alerts" else "All caught up",
                user = user,
                unreadCount = unreadCount,
                onBack = onBack,
                onProfileClick = onProfileClick,
                onBellClick = {},
                trailingAction = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllRead() }) {
                            Text("Read all", color = PeersPrimaryIndigo, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { padding ->
        if (notifications.isEmpty()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            ) {
                PeersDrawableIcon(
                    lightRes = R.drawable.notification_black,
                    darkRes = R.drawable.notification_white,
                    contentDescription = null,
                    size = 44.dp,
                )
                Text(
                    "You're all caught up!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    "Connection requests and event reminders will show up here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                groups.forEach { (day, items) ->
                    item(key = day) {
                        Text(
                            day,
                            style = MaterialTheme.typography.labelLarge,
                            color = PeersPrimaryIndigo,
                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                        )
                    }
                    items(items, key = { it.id }) { notification ->
                        NotificationRow(
                            notification = notification,
                            resolved = resolvedIds.contains(notification.id),
                            onAccept = { resolvedIds = resolvedIds + notification.id },
                            onDecline = { resolvedIds = resolvedIds + notification.id },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: PeerNotification,
    resolved: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    val (iconBg, iconTint) = when (notification.type) {
        NotificationType.CONNECTION_REQUEST -> PeersLavender to PeersBrandInk
        NotificationType.CONNECTION_ACCEPTED -> PeersMint to Color(0xFF1F6B3B)
        NotificationType.REQUEST_SENT -> PeersSky to Color(0xFF1E4C6B)
        NotificationType.INFO -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (notification.isUnread) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.fillMaxWidth()) {
            // Unread accent rail
            if (notification.isUnread) {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(PeersPrimaryIndigo)
                )
            }
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(iconBg),
                    ) {
                        val (lightRes, darkRes) = when (notification.type) {
                            NotificationType.CONNECTION_REQUEST -> R.drawable.request_black to R.drawable.request_white
                            NotificationType.CONNECTION_ACCEPTED -> R.drawable.handshake_black to R.drawable.handshake_white
                            NotificationType.REQUEST_SENT -> R.drawable.invite_black to R.drawable.invite_white
                            NotificationType.INFO -> R.drawable.info_black to R.drawable.info_white
                        }
                        PeersDrawableIcon(
                            lightRes = lightRes,
                            darkRes = darkRes,
                            contentDescription = notification.title,
                            size = 22.dp,
                        )
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                notification.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (notification.isUnread) FontWeight.Bold else FontWeight.Medium,
                                color = iconTint,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                relativeLabel(notification.timestampMillis),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            notification.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (notification.isConnectionRequest && !resolved) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 56.dp, top = 8.dp),
                    ) {
                        FilledTonalButton(onClick = onAccept) {
                            Icon(Icons.Rounded.Check, null, Modifier.size(16.dp))
                            Text("  Accept", fontSize = 13.sp)
                        }
                        AssistChip(
                            onClick = onDecline,
                            label = { Text("Decline") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Close, null, Modifier.size(14.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                } else if (resolved) {
                    Text(
                        "Handled",
                        style = MaterialTheme.typography.labelSmall,
                        color = PeersPrimaryIndigo,
                        modifier = Modifier.padding(start = 56.dp, top = 4.dp),
                    )
                }
            }
        }
    }
}

private fun dayLabel(millis: Long): String {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(notifDateFormatter)
    }
}

private fun relativeLabel(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 172_800_000 -> "Yesterday"
        else -> "${diff / 86_400_000}d ago"
    }
}
