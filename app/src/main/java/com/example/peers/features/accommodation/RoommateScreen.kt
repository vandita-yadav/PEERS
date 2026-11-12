package com.example.peers.features.accommodation

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.peers.ui.theme.PeersPrimaryIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoommateScreen(
    onBack: () -> Unit,
    viewModel: AccommodationViewModel,
) {
    val seekers by viewModel.seekers.collectAsStateWithLifecycle()
    val requests by viewModel.roommateRequests.collectAsStateWithLifecycle()

    var tab by remember { mutableIntStateOf(0) }
    var contactRequest by remember { mutableStateOf<RoommateRequest?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Roommate matching",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Find someone to share your stay",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = PeersPrimaryIndigo,
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Find a roommate") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = {
                    Text(
                        if (requests.any { it.incoming && it.status == RoommateRequestStatus.PENDING }) {
                            "Requests •"
                        } else {
                            "Requests"
                        }
                    )
                })
            }

            if (tab == 0) {
                SeekerList(
                    seekers = seekers,
                    onSend = { seeker ->
                        viewModel.sendRoommateRequestToSeeker(seeker)
                    },
                )
            } else {
                RequestList(
                    requests = requests,
                    onRespond = viewModel::respondToRoommateRequest,
                    onViewContact = { contactRequest = it },
                )
            }
        }
    }

    contactRequest?.let { request ->
        ContactDialog(request = request, onDismiss = { contactRequest = null })
    }
}

@Composable
private fun SeekerList(
    seekers: List<RoommateSeeker>,
    onSend: (RoommateSeeker) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "Students looking for a roommate",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Send a request to introduce yourself. Contact details stay hidden until they accept.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
            )
        }
        items(seekers, key = { it.uid }) { seeker ->
            SeekerCard(seeker = seeker, onSend = { onSend(seeker) })
        }
    }
}

@Composable
private fun SeekerCard(seeker: RoommateSeeker, onSend: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(46.dp).clip(CircleShape).background(seeker.accent),
                ) {
                    Text(seeker.emoji, fontSize = 22.sp)
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        seeker.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "${seeker.course} · ${seeker.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    "₹${"%,d".format(seeker.budget)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PeersPrimaryIndigo,
                )
            }

            Text(
                "📍 ${seeker.area}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 10.dp),
            ) {
                items(seeker.interests) { interest ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            interest,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onSend,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Icon(Icons.Rounded.PersonAdd, null, Modifier.size(18.dp))
                Text("  Send roommate request")
            }
        }
    }
}

@Composable
private fun RequestList(
    requests: List<RoommateRequest>,
    onRespond: (RoommateRequest, Boolean) -> Unit,
    onViewContact: (RoommateRequest) -> Unit,
) {
    val incoming = requests.filter { it.incoming }
    val outgoing = requests.filterNot { it.incoming }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "Incoming requests",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        if (incoming.isEmpty()) {
            item { EmptyRow("No incoming requests right now.") }
        }
        items(incoming, key = { it.id }) { request ->
            IncomingRequestCard(
                request = request,
                onAccept = { onRespond(request, true) },
                onDecline = { onRespond(request, false) },
                onViewContact = { onViewContact(request) },
            )
        }
        item {
            Text(
                "Sent requests",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (outgoing.isEmpty()) {
            item { EmptyRow("Requests you send will appear here.") }
        }
        items(outgoing, key = { it.id }) { request ->
            OutgoingRequestCard(request)
        }
    }
}

@Composable
private fun IncomingRequestCard(
    request: RoommateRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onViewContact: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    request.fromName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(request.status)
            }
            Text(
                "about ${request.listingTitle}",
                style = MaterialTheme.typography.labelMedium,
                color = PeersPrimaryIndigo,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                request.message.ifBlank { "Would like to connect about sharing this stay." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            when (request.status) {
                RoommateRequestStatus.PENDING -> Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                    ) { Text("Decline") }
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                        modifier = Modifier.weight(1f),
                    ) { Text("Accept") }
                }

                RoommateRequestStatus.ACCEPTED -> Button(
                    onClick = onViewContact,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Icon(Icons.Rounded.MarkEmailRead, null, Modifier.size(18.dp))
                    Text("  See ways you can connect")
                }

                RoommateRequestStatus.DECLINED -> Text(
                    "You declined this request.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun OutgoingRequestCard(request: RoommateRequest) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    request.listingTitle.ifBlank { request.fromName },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    request.message.ifBlank { "Roommate request sent" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            StatusChip(request.status)
        }
    }
}

@Composable
private fun StatusChip(status: RoommateRequestStatus) {
    val (label, color) = when (status) {
        RoommateRequestStatus.PENDING -> "Pending" to Color(0xFFB27A1F)
        RoommateRequestStatus.ACCEPTED -> "Accepted" to Color(0xFF1F6B3B)
        RoommateRequestStatus.DECLINED -> "Declined" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun EmptyRow(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 6.dp),
    )
}

/** "Here are ways you can connect" — only the contact details they agreed to share. */
@Composable
private fun ContactDialog(request: RoommateRequest, onDismiss: () -> Unit) {
    val entries = request.contact.visibleEntries()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ways you can connect") },
        text = {
            Column {
                Text(
                    "${request.fromName} accepted your roommate request.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (entries.isEmpty()) {
                    Text(
                        "They didn't share any contact details. Continue the conversation in your connection chat.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                } else {
                    entries.forEach { (label, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(vertical = 8.dp),
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
            ) { Text("Done") }
        },
    )
}
