package com.example.peers.features.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.peers.features.dashboard.components.EventDot
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersPrimaryIndigo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val homeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)

/** Content of the Home tab: dates, top events, PYMK, discover grid. */
@Composable
fun DashboardHome(viewModel: DashboardViewModel) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { DateSlider(today, selectedDate, DashboardMockData.events, onDateSelected = { selectedDate = it }) }

        item {
            val shown = DashboardMockData.events.filter { it.date == selectedDate }
            SectionHeader(
                title = if (selectedDate == today) "Today's Events" else "Events on ${selectedDate.format(homeDateFormatter)}",
                actionLabel = "View All",
                onAction = { },
            )
            if (shown.isEmpty()) {
                EmptyStateCard(
                    emoji = "🗓️",
                    message = "No events on this day.\nBrowse the full calendar instead!",
                )
            } else {
                EventsRow(shown)
            }
        }

        item {
            SectionHeader(title = "People You May Know", actionLabel = "View All", onAction = { })
            val me = user
            if (me == null) {
                EmptyStateCard(emoji = "🫂", message = "Sign in to meet peers from your university!")
            } else {
                val ranked = DashboardMockData.suggestedPeers
                    .map { it to it.scoreFor(me) }
                    .sortedByDescending { it.second }
                    .take(3)
                PeersRow(ranked)
            }
        }

        item {
            SectionHeader(title = "Discover What Interests You", actionLabel = "", onAction = { })
            DiscoverGrid()
        }
    }
}

// region — Date calendar slider -------------------------------------------------------------

@Composable
private fun DateSlider(
    today: LocalDate,
    selectedDate: LocalDate,
    events: List<DashboardEvent>,
    onDateSelected: (LocalDate) -> Unit,
) {
    val days = (0..13L).map { today.plusDays(it) }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(days) { _, date ->
            val isSelected = date == selectedDate
            val hasEvent = events.any { it.date == date }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) PeersPrimaryIndigo else MaterialTheme.colorScheme.surface,
                    border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                    shadowElevation = if (isSelected) 4.dp else 0.dp,
                    modifier = Modifier.clickable { onDateSelected(date) },
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    ) {
                        Text(
                            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "${date.dayOfMonth}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                if (hasEvent) {
                    EventDot(PeersPrimaryIndigo, Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

// endregion

// region — Top Events ------------------------------------------------------------------------

@Composable
private fun EventsRow(events: List<DashboardEvent>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(events, key = { it.id }) { event ->
            EventCard(event)
        }
    }
}

@Composable
private fun EventCard(event: DashboardEvent) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.width(230.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(event.accent, MaterialTheme.colorScheme.surface)
                        )
                    ),
            ) {
                Text(event.emoji, fontSize = 34.sp)
            }
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                "${event.date.format(homeDateFormatter)} · ${event.time}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Icon(
                    Icons.Rounded.Handshake,
                    contentDescription = null,
                    tint = PeersPrimaryIndigo,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    " ${event.attendees} going · ${event.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

// endregion

// region — People You May Know ---------------------------------------------------------------

@Composable
private fun PeersRow(ranked: List<Pair<SuggestedPeer, Int>>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(ranked, key = { it.first.uid }) { (peer, score) ->
            PeerCard(peer, score)
        }
    }
}

@Composable
private fun PeerCard(peer: SuggestedPeer, score: Int) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.width(190.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            val emojis = listOf("🧑🏿", "👩🏿", "👦🏽", "👧🏼", "👩🏽", "🧔🏽", "🧕🏾")
            val pastels = listOf(PeersLavender, PeersPrimaryIndigo, com.example.peers.ui.theme.PeersSky)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(pastels[peer.avatarIndex.mod(pastels.size)]),
            ) {
                Text(emojis[peer.avatarIndex.mod(emojis.size)], fontSize = 28.sp)
            }
            Text(
                peer.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "${peer.course} · ${peer.year}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    if (score >= 45) "⭐ Great match" else "✨ Matches you",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            FilledTonalButton(
                onClick = { /* Connect flow → Community phase */ },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            ) {
                Text("Connect")
            }
        }
    }
}

// endregion

// region — Discover grid ---------------------------------------------------------------------

private data class DiscoverItem(val emoji: String, val title: String, val subtitle: String)

private val discoverItems = listOf(
    DiscoverItem("🤝", "Make Friends", "Find peers near you"),
    DiscoverItem("🎪", "Events", "What's happening on campus"),
    DiscoverItem("🏠", "Accommodation", "Rooms & flatmates"),
    DiscoverItem("💼", "Jobs & Internships", "Career opportunities"),
    DiscoverItem("💰", "Budget & Finance", "Split expenses smartly"),
    DiscoverItem("🧠", "University Help", "Notes & mentors"),
)

@Composable
private fun DiscoverGrid() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 20.dp),
    ) {
        discoverItems.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.weight(1f).clickable { },
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(item.emoji, fontSize = 26.sp)
                            Text(
                                item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            Text(
                                item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// endregion

// region — Shared section helpers ------------------------------------------------------------

@Composable
private fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 22.dp, bottom = 12.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel.isNotBlank()) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = PeersPrimaryIndigo)
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = PeersPrimaryIndigo,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(emoji: String, message: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
        ) {
            Text(emoji, fontSize = 30.sp)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

// endregion
