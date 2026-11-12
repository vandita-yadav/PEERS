package com.example.peers.features.events

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    viewModel: EventsViewModel = viewModel(),
    initialDate: LocalDate? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(initialDate) {
        if (initialDate != null) {
            viewModel.onDateSelected(initialDate)
        }
    }

    LaunchedEffect(state.events.size, state.selectedDate) {
        if (state.events.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is EventUiEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Header & Search Bar
            EventsHeader(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                onCreateClick = { viewModel.setCreatingEvent(true) }
            )

            // Horizontal Date Calendar Ribbon
            DateCalendarRibbon(
                selectedDate = state.selectedDate,
                onDateSelected = viewModel::onDateSelected,
                events = state.events,
            )

            // Organizer Category Filters
            OrganizerFilterRow(
                selected = state.selectedOrganizer,
                onSelected = viewModel::onOrganizerSelected
            )

            // Events List or Empty State
            if (state.events.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    EventsEmptyState(
                        selectedDate = state.selectedDate,
                        onResetFilter = {
                            viewModel.onDateSelected(null)
                            viewModel.onOrganizerSelected(null)
                            viewModel.onQueryChange("")
                        }
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(state.events, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            isAttending = event.id in state.myAttendance,
                            isSaved = event.id in state.savedEventIds,
                            onToggleRsvp = { viewModel.toggleRsvp(event) },
                            onToggleSave = { viewModel.toggleSave(event) },
                            onClick = { viewModel.openEventDetails(event) },
                        )
                    }
                }
            }
        }
    }

    // Modal: Event Details & Attendee List
    if (state.viewingEvent != null) {
        EventDetailsSheet(
            event = state.viewingEvent!!,
            attendees = state.activeAttendees,
            isAttending = state.viewingEvent!!.id in state.myAttendance,
            isSaved = state.viewingEvent!!.id in state.savedEventIds,
            onDismiss = viewModel::closeEventDetails,
            onToggleRsvp = { viewModel.toggleRsvp(state.viewingEvent!!) },
            onToggleSave = { viewModel.toggleSave(state.viewingEvent!!) },
        )
    }

    // Modal: Create New Event
    if (state.isCreatingEvent) {
        CreateEventSheet(
            onDismiss = { viewModel.setCreatingEvent(false) },
            onCreate = { title, desc, orgName, orgType, venue, city, startMillis, emoji, cap ->
                viewModel.createEvent(title, desc, orgName, orgType, venue, city, startMillis, emoji, cap)
            }
        )
    }
}

// region — Header & Search ---------------------------------------------------------------------

@Composable
private fun EventsHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onCreateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Campus & City Events",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Meetups, study circles & orientations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PeersPrimaryIndigo,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Create", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search events, topics, or venues…", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = PeersPrimaryIndigo,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}

// region — Date Calendar Ribbon ----------------------------------------------------------------

@Composable
private fun DateCalendarRibbon(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    events: List<CommunityEvent>,
) {
    val today = remember { LocalDate.now() }
    val days = remember(today) { (0L..13L).map { today.plusDays(it) } }

    val dayFormatter = remember { DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d", Locale.ENGLISH) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // "All Dates" Chip
        item {
            val isAllSelected = selectedDate == null
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isAllSelected) PeersPrimaryIndigo else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (isAllSelected) PeersPrimaryIndigo else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .height(64.dp)
                    .clickable { onDateSelected(null) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = "All\nUpcoming",
                        textAlign = TextAlign.Center,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(days) { date ->
            val isSelected = selectedDate == date
            val hasEvent = events.any { it.date == date }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) PeersPrimaryIndigo else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) PeersPrimaryIndigo else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .width(52.dp)
                    .height(64.dp)
                    .clickable { onDateSelected(date) }
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize().padding(4.dp)
                ) {
                    Text(
                        text = if (date == today) "TODAY" else date.format(dayFormatter).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date.format(dateFormatter),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                    if (hasEvent && !isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(PeersPrimaryIndigo)
                        )
                    } else {
                        Spacer(Modifier.height(5.dp))
                    }
                }
            }
        }
    }
}

// region — Category Filter ---------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrganizerFilterRow(
    selected: EventOrganizerType?,
    onSelected: (EventOrganizerType?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text("All Categories", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PeersLavender.copy(alpha = 0.35f),
                    selectedLabelColor = PeersPrimaryIndigo,
                )
            )
        }
        item {
            FilterChip(
                selected = selected == EventOrganizerType.UNIVERSITY,
                onClick = { onSelected(EventOrganizerType.UNIVERSITY) },
                label = { Text("🎓 University", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PeersPrimaryIndigo.copy(alpha = 0.25f),
                    selectedLabelColor = PeersPrimaryIndigo,
                )
            )
        }
        item {
            FilterChip(
                selected = selected == EventOrganizerType.STUDENT,
                onClick = { onSelected(EventOrganizerType.STUDENT) },
                label = { Text("🧑‍🎓 Student Clubs", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PeersSky.copy(alpha = 0.35f),
                    selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        }
        item {
            FilterChip(
                selected = selected == EventOrganizerType.COMMUNITY,
                onClick = { onSelected(EventOrganizerType.COMMUNITY) },
                label = { Text("👥 Community", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PeersMint.copy(alpha = 0.45f),
                    selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        }
    }
}

// region — Event Card --------------------------------------------------------------------------

@Composable
private fun EventCard(
    event: CommunityEvent,
    isAttending: Boolean,
    isSaved: Boolean,
    onToggleRsvp: () -> Unit,
    onToggleSave: () -> Unit,
    onClick: () -> Unit,
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH) }
    val today = remember { LocalDate.now() }

    val formattedDate = when (event.date) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> event.date.format(dateFormatter)
    }
    val formattedTime = event.time.format(timeFormatter)

    val orgColor = when (event.organizerType) {
        EventOrganizerType.UNIVERSITY -> PeersPrimaryIndigo
        EventOrganizerType.STUDENT -> PeersSky
        EventOrganizerType.COMMUNITY -> PeersMint
    }

    val occupancyRatio = if (event.capacity > 0) {
        (event.attendeeCount.toFloat() / event.capacity.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = occupancyRatio, label = "occupancy")

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Emoji Icon + Title + Category Pill + Bookmark
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large circular emoji container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(orgColor.copy(alpha = 0.25f))
                ) {
                    Text(text = event.emoji, fontSize = 24.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Category Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = orgColor.copy(alpha = 0.2f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = event.organizerType.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (event.organizerType == EventOrganizerType.UNIVERSITY) PeersPrimaryIndigo else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        // Bookmark Save Icon
                        IconButton(
                            onClick = onToggleSave,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = if (isSaved) "Remove bookmark" else "Save event",
                                tint = if (isSaved) PeersPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Organizer byline
            Text(
                text = "Organized by ${event.organizerName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            // Venue and Date/Time Info Pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = PeersPrimaryIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$formattedDate • $formattedTime",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = PeersSky,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${event.venue}, ${event.city}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Capacity Bar & Attendee Count
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${event.attendeeCount} attending",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "${event.capacity - event.attendeeCount} spots left",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = PeersPrimaryIndigo,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            Spacer(Modifier.height(14.dp))

            // Bottom Actions: RSVP Button + Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "View Details →",
                        color = PeersPrimaryIndigo,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                if (isAttending) {
                    Button(
                        onClick = onToggleRsvp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PeersPrimaryIndigo,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Attending ✓", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onToggleRsvp,
                        border = BorderStroke(1.dp, PeersPrimaryIndigo),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("RSVP • Free", color = PeersPrimaryIndigo, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// region — Empty State -------------------------------------------------------------------------

@Composable
private fun EventsEmptyState(
    selectedDate: LocalDate?,
    onResetFilter: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PeersLavender.copy(alpha = 0.25f))
        ) {
            Text("🎪", fontSize = 36.sp)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (selectedDate != null) "No events scheduled for this day" else "No matching events found",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Try picking another date from the calendar or resetting your filters to see upcoming events.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onResetFilter,
            colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo, contentColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Show All Events", fontWeight = FontWeight.SemiBold)
        }
    }
}

// region — Event Details Modal -----------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDetailsSheet(
    event: CommunityEvent,
    attendees: List<EventAttendee>,
    isAttending: Boolean,
    isSaved: Boolean,
    onDismiss: () -> Unit,
    onToggleRsvp: () -> Unit,
    onToggleSave: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Emoji + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PeersLavender.copy(alpha = 0.35f))
                ) {
                    Text(event.emoji, fontSize = 28.sp)
                }

                Row {
                    IconButton(onClick = onToggleSave) {
                        Icon(
                            imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) PeersPrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Organized by ${event.organizerName} • ${event.organizerType.label}",
                style = MaterialTheme.typography.bodyMedium,
                color = PeersPrimaryIndigo,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))

            // Time & Venue Callouts
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = PeersPrimaryIndigo)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(event.date.format(dateFormatter), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(event.time.format(timeFormatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = PeersSky)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(event.venue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(event.city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))

            // Description
            Text(
                text = "About this Event",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(20.dp))

            // Attendees List section
            Text(
                text = "Who's Going (${event.attendeeCount} / ${event.capacity})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(10.dp))

            if (attendees.isEmpty()) {
                Text(
                    text = "Be the first to RSVP and join the attendee list!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(attendees, key = { it.uid }) { attendee ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(attendee.emoji, fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(attendee.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Big RSVP Action Button
            Button(
                onClick = onToggleRsvp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAttending) MaterialTheme.colorScheme.surfaceVariant else PeersPrimaryIndigo,
                    contentColor = if (isAttending) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (isAttending) "Cancel RSVP (Currently Attending)" else "RSVP to this Event • Free",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// region — Create Event Modal Sheet -----------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEventSheet(
    onDismiss: () -> Unit,
    onCreate: (
        title: String,
        desc: String,
        organizerName: String,
        organizerType: EventOrganizerType,
        venue: String,
        city: String,
        startMillis: Long,
        emoji: String,
        capacity: Int,
    ) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var organizerName by remember { mutableStateOf("") }
    var organizerType by remember { mutableStateOf(EventOrganizerType.STUDENT) }
    var venue by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Phagwara") }
    var daysFromNow by remember { mutableIntStateOf(2) }
    var hourOfDay by remember { mutableIntStateOf(17) }
    var emoji by remember { mutableStateOf("🎉") }
    var capacityStr by remember { mutableStateOf("50") }

    val emojis = listOf("🎉", "🎙️", "🍳", "🛬", "🎪", "📚", "🍕", "⚽", "☕", "🎮")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Create Campus Event",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Emoji selector
            Text("Pick an Event Icon", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(emojis) { em ->
                    Surface(
                        shape = CircleShape,
                        color = if (emoji == em) PeersPrimaryIndigo.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (emoji == em) BorderStroke(2.dp, PeersPrimaryIndigo) else null,
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { emoji = em }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(em, fontSize = 20.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                placeholder = { Text("e.g. Freshers' Mixer, Web Dev Hacknight") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("What can peers expect? Schedule, snacks, items to bring...") },
                minLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = organizerName,
                    onValueChange = { organizerName = it },
                    label = { Text("Organizer / Society") },
                    placeholder = { Text("e.g. Coding Club") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = capacityStr,
                    onValueChange = { capacityStr = it.filter { char -> char.isDigit() } },
                    label = { Text("Capacity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.width(100.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Organizer Type Selector
            Text("Organizer Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                EventOrganizerType.entries.forEach { type ->
                    val isSel = organizerType == type
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) PeersPrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { organizerType = type }
                    ) {
                        Text(
                            text = type.label,
                            textAlign = TextAlign.Center,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onBackground,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue") },
                    placeholder = { Text("e.g. Student Quad") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            val canSubmit = title.isNotBlank()

            Button(
                onClick = {
                    val targetDate = LocalDate.now().plusDays(daysFromNow.toLong())
                    val millis = targetDate.atTime(hourOfDay, 0)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    onCreate(
                        title,
                        description.ifBlank { "Campus student meetup for $title." },
                        organizerName.ifBlank { "PEERS Community" },
                        organizerType,
                        venue.ifBlank { "Main Campus Quad" },
                        city.ifBlank { "Phagwara" },
                        millis,
                        emoji,
                        capacityStr.toIntOrNull() ?: 50
                    )
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PeersPrimaryIndigo,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Publish Campus Event 🚀", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
