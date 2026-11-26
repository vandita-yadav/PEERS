package com.example.peers.features.dashboard

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.peers.R
import com.example.peers.features.dashboard.components.EventDot
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersCoral
import com.example.peers.ui.theme.PeersDrawableIcon
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPink
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import com.example.peers.ui.theme.PeersWarmYellow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val homeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)

/**
 * Content of the Home tab:
 * - Unified Date Calendar Slider & Today's/Selected-Day Events (synced with Events tab)
 * - PEERS AI Student Guide Banner
 * - Discover What Interests You (with Discussion, Rentals & Transport, etc.; no People You May Know)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardHome(
    viewModel: DashboardViewModel,
    onOpenAccommodation: () -> Unit = {},
    onOpenRentals: () -> Unit = {},
    onOpenBudget: () -> Unit = {},
    onOpenEvents: (LocalDate?) -> Unit = {},
    onOpenCommunity: () -> Unit = {},
    onOpenContacts: () -> Unit = {},
    onOpenChecklist: () -> Unit = {},
    onOpenAssistant: () -> Unit = {},
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val allEvents by viewModel.events.collectAsStateWithLifecycle()
    val savedEventIds by viewModel.savedEventIds.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var isRefreshing by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refreshAll { isRefreshing = false }
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
        item { DateSlider(today, selectedDate, allEvents, onDateSelected = { selectedDate = it }) }

        item {
            val shown = allEvents.filter { it.date == selectedDate }
            SectionHeader(
                title = if (selectedDate == today) "Today's Events" else "Events on ${selectedDate.format(homeDateFormatter)}",
                actionLabel = "View All",
                onAction = { onOpenEvents(selectedDate) },
            )
            if (shown.isEmpty()) {
                EmptyStateCard(
                    message = "No events on this day.\nBrowse the full calendar instead!",
                    onClick = { onOpenEvents(null) },
                )
            } else {
                EventsRow(
                    events = shown,
                    savedEventIds = savedEventIds,
                    onToggleSaved = viewModel::toggleSavedEvent,
                    onOpenEvent = { onOpenEvents(selectedDate) },
                )
            }
        }

        item {
            Spacer(Modifier.height(10.dp))
            AiAssistantBanner(onClick = onOpenAssistant)
        }

        item {
            val userNeeds = user?.needs.orEmpty()
            SectionHeader(
                title = if (userNeeds.isNotEmpty()) "Discover What Interests You" else "Discover What Interests You",
                actionLabel = "",
                onAction = { }
            )
            DiscoverGrid(
                userNeeds = userNeeds,
                onOpen = { item ->
                    when (item.route) {
                        "community" -> onOpenCommunity()
                        "events" -> onOpenEvents(null)
                        "accommodation" -> onOpenAccommodation()
                        "rentals" -> onOpenRentals()
                        "budget" -> onOpenBudget()
                        "checklist" -> onOpenChecklist()
                        "assistant" -> onOpenAssistant()
                        "contacts" -> onOpenContacts()
                    }
                }
            )
        }
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
private fun EventsRow(
    events: List<DashboardEvent>,
    savedEventIds: Set<String>,
    onToggleSaved: (DashboardEvent) -> Unit,
    onOpenEvent: (DashboardEvent) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(events, key = { it.id }) { event ->
            EventCard(
                event = event,
                isSaved = event.id in savedEventIds,
                onToggleSaved = { onToggleSaved(event) },
                onClick = { onOpenEvent(event) },
            )
        }
    }
}

@Composable
private fun EventCard(
    event: DashboardEvent,
    isSaved: Boolean,
    onToggleSaved: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .width(250.dp)
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(event.accent, MaterialTheme.colorScheme.surface)
                        )
                    ),
            ) {
                // Date badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier.padding(10.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            event.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = PeersPrimaryIndigo,
                        )
                        Text(
                            "${event.date.dayOfMonth}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                // Save for later
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                ) {
                    IconButton(onClick = onToggleSaved, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isSaved) Icons.Rounded.Bookmark
                            else Icons.Rounded.BookmarkBorder,
                            contentDescription = if (isSaved) {
                                "Remove ${event.title} from saved events"
                            } else {
                                "Save ${event.title} for later"
                            },
                            tint = PeersPrimaryIndigo,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                // Drawable Event Icon
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 18.dp),
                ) {
                    PeersDrawableIcon(
                        lightRes = R.drawable.events_black,
                        darkRes = R.drawable.events_white,
                        contentDescription = null,
                        size = 34.dp,
                    )
                }
            }

            Text(
                event.title,
                fontFamily = GoogleSans,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                event.organizer,
                fontFamily = Lato,
                style = MaterialTheme.typography.labelMedium,
                color = PeersPrimaryIndigo,
                modifier = Modifier.padding(top = 2.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 10.dp),
            ) {
                AttendeeInitialStack(event.attendees)
                Text(
                    " ${event.attendees} going",
                    fontFamily = Lato,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Icon(
                    Icons.Rounded.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    " ${event.location} · ${event.time}",
                    fontFamily = Lato,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AttendeeInitialStack(count: Int) {
    val letters = listOf("A", "R", "S")
    val colors = listOf(PeersLavender, PeersPrimaryIndigo, PeersSky)
    Row {
        letters.forEachIndexed { index, letter ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = if (index == 0) 0.dp else (-8).dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors[index])
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
            ) {
                Text(
                    text = letter,
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.White,
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(x = (-8).dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
        ) {
            Text(
                "+${(count - letters.size).coerceAtLeast(0)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
            )
        }
    }
}

// endregion

// region — Discover grid ---------------------------------------------------------------------

private data class DiscoverItem(
    @DrawableRes val lightIcon: Int,
    @DrawableRes val darkIcon: Int,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val route: String,
    val needKey: String,
)

private val allDiscoverItems = listOf(
    DiscoverItem(
        R.drawable.community_black, R.drawable.community_white,
        "Discussion", "Join other peers in the discussion", PeersLavender, "community", "Discussion"
    ),
    DiscoverItem(
        R.drawable.events_black, R.drawable.events_white,
        "Events & Activities", "What's happening on campus", PeersSky, "events", "Events"
    ),
    DiscoverItem(
        R.drawable.accomodation_black, R.drawable.accomodation_white,
        "Accommodation", "Rooms & flatmates", PeersMint, "accommodation", "Accommodation"
    ),
    DiscoverItem(
        R.drawable.job_black, R.drawable.job_white,
        "Rentals & Transport", "Bikes, shuttles & student exchange", PeersWarmYellow, "rentals", "Rentals"
    ),
    DiscoverItem(
        R.drawable.budget_black, R.drawable.budget_white,
        "Budget & Finance", "Split expenses smartly", PeersPink, "budget", "Budget"
    ),
    DiscoverItem(
        R.drawable.checklist_black, R.drawable.checklist_white,
        "Student Checklist", "Arrival & prep tasks", PeersCoral, "checklist", "Visa"
    ),
    DiscoverItem(
        R.drawable.info_black, R.drawable.info_white,
        "University Help", "Campus helplines & contacts", PeersSky, "contacts", "University Help"
    ),
    DiscoverItem(
        R.drawable.handshake_black, R.drawable.handshake_white,
        "Helplines & Contacts", "Hospital, hostel & safety numbers", PeersPink, "contacts", "Helplines"
    ),
)

@Composable
private fun DiscoverGrid(
    userNeeds: List<String>,
    onOpen: (DiscoverItem) -> Unit,
) {
    val prioritized = remember(userNeeds) {
        if (userNeeds.isEmpty()) {
            allDiscoverItems.take(6).map { it to false }
        } else {
            val matched = allDiscoverItems.filter { item ->
                userNeeds.any { need ->
                    need.contains(item.needKey, ignoreCase = true) ||
                        (item.route == "rentals" && need.contains("Transport", ignoreCase = true)) ||
                        (item.route == "community" && need.contains("Make Friends", ignoreCase = true))
                }
            }
            val remaining = allDiscoverItems.filter { it !in matched }
            val combined = matched.map { it to true } + remaining.take((6 - matched.size).coerceAtLeast(0)).map { it to false }
            combined
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 20.dp),
    ) {
        prioritized.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { (item, isForYou) ->
                    DiscoverCard(
                        item = item,
                        isForYou = isForYou,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpen(item) }
                    )
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DiscoverCard(
    item: DiscoverItem,
    isForYou: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp,
            pressedElevation = 8.dp,
        ),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            item.accent.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(item.accent.copy(alpha = 0.72f)),
                ) {
                    PeersDrawableIcon(
                        lightRes = item.lightIcon,
                        darkRes = item.darkIcon,
                        contentDescription = item.title,
                        size = 22.dp,
                    )
                }
                Spacer(Modifier.weight(1f))
                if (isForYou) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PeersPrimaryIndigo.copy(alpha = 0.14f),
                    ) {
                        Text(
                            "For You",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PeersPrimaryIndigo,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                } else {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Text(
                item.title,
                fontFamily = GoogleSans,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                item.subtitle,
                fontFamily = Lato,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                minLines = 2,
            )
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
            .padding(start = 20.dp, end = 8.dp, top = 20.dp, bottom = 10.dp),
    ) {
        Text(
            title,
            fontFamily = GoogleSans,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel.isNotBlank()) {
            TextButton(onClick = onAction) {
                Text(actionLabel, fontFamily = GoogleSans, color = PeersPrimaryIndigo)
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
private fun EmptyStateCard(message: String, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp, horizontal = 16.dp),
        ) {
            PeersDrawableIcon(
                lightRes = R.drawable.calendar_black,
                darkRes = R.drawable.calendar_white,
                contentDescription = null,
                size = 30.dp,
            )
            Text(
                message,
                fontFamily = Lato,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun AiAssistantBanner(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(PeersLavender, PeersSky, PeersMint)
            )
        ),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(PeersLavender.copy(alpha = 0.35f), PeersSky.copy(alpha = 0.35f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                PeersDrawableIcon(
                    lightRes = R.drawable.aibot_black,
                    darkRes = R.drawable.aibot_white,
                    contentDescription = "PEERS AI Guide",
                    size = 24.dp,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "PEERS AI Student Guide",
                        fontFamily = GoogleSans,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PeersLavender.copy(alpha = 0.45f)
                    ) {
                        Text(
                            "AI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333366),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    "Ask anything about housing, rentals, budgeting, checklists, or campus events",
                    fontFamily = Lato,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Open AI Guide",
                tint = PeersPrimaryIndigo,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// endregion
