package com.example.peers.features.accommodation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    BackHandler { onBack() }

    var tab by remember { mutableIntStateOf(0) }
    var contactRequest by remember { mutableStateOf<RoommateRequest?>(null) }
    var selectedSeekerForRequest by remember { mutableStateOf<RoommateSeeker?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    val pendingCount = requests.count { it.incoming && it.status == RoommateRequestStatus.PENDING }
                    Text(if (pendingCount > 0) "Requests ($pendingCount)" else "Requests")
                })
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refreshAll { isRefreshing = false }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                if (tab == 0) {
                    SeekerList(
                        seekers = seekers,
                        outgoingRequests = requests.filterNot { it.incoming },
                        onSend = { seeker -> selectedSeekerForRequest = seeker },
                        onCancel = { req -> viewModel.cancelRoommateRequest(req) },
                    )
                } else {
                    RequestList(
                        requests = requests,
                        onRespond = viewModel::respondToRoommateRequest,
                        onCancelOutgoing = viewModel::cancelRoommateRequest,
                        onViewContact = { contactRequest = it },
                    )
                }
            }
        }
    }

    selectedSeekerForRequest?.let { seeker ->
        var introMsg by remember(seeker.uid) {
            mutableStateOf("Hi ${seeker.name}! I'm also looking for a roommate in ${seeker.area}.")
        }
        var phone by remember(seeker.uid) { mutableStateOf("") }
        var email by remember(seeker.uid) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { selectedSeekerForRequest = null },
            title = { Text("Connect with ${seeker.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Area: ${seeker.area} · Budget ₹${"%,d".format(seeker.budget)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PeersPrimaryIndigo,
                    )
                    OutlinedTextField(
                        value = introMsg,
                        onValueChange = { introMsg = it },
                        label = { Text("Message") },
                        minLines = 2,
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Your Phone (optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Your Email (optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendRoommateRequestToSeeker(
                            seeker = seeker,
                            message = introMsg,
                            contact = ContactInfo(phone = phone, email = email),
                        )
                        selectedSeekerForRequest = null
                    },
                    enabled = introMsg.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                ) { Text("Send Request") }
            },
            dismissButton = {
                TextButton(onClick = { selectedSeekerForRequest = null }) { Text("Cancel") }
            },
        )
    }

    contactRequest?.let { request ->
        ContactDialog(request = request, onDismiss = { contactRequest = null })
    }
}

@Composable
private fun SeekerList(
    seekers: List<RoommateSeeker>,
    outgoingRequests: List<RoommateRequest>,
    onSend: (RoommateSeeker) -> Unit,
    onCancel: (RoommateRequest) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text(
                "Students looking for a roommate",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Send a request to introduce yourself. You will both receive an in-app and phone notification.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
            )
        }
        items(seekers, key = { it.uid }) { seeker ->
            val existing = outgoingRequests.firstOrNull { it.toUid == seeker.uid }
            SeekerCard(
                seeker = seeker,
                existingRequest = existing,
                onSend = { onSend(seeker) },
                onCancel = { if (existing != null) onCancel(existing) },
            )
        }
    }
}

@Composable
private fun SeekerCard(
    seeker: RoommateSeeker,
    existingRequest: RoommateRequest?,
    onSend: () -> Unit,
    onCancel: () -> Unit,
) {
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
                    Text(
                        seeker.name.firstOrNull()?.uppercase() ?: "P",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B30),
                    )
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
                "Area: ${seeker.area}",
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

            when (existingRequest?.status) {
                RoommateRequestStatus.PENDING -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    StatusChip(RoommateRequestStatus.PENDING)
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Rounded.Close, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Withdraw")
                    }
                }
                RoommateRequestStatus.ACCEPTED -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    StatusChip(RoommateRequestStatus.ACCEPTED)
                }
                else -> Button(
                    onClick = onSend,
                    colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Icon(Icons.Rounded.PersonAdd, null, Modifier.size(18.dp))
                    Text("  Send roommate request", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RequestList(
    requests: List<RoommateRequest>,
    onRespond: (RoommateRequest, Boolean) -> Unit,
    onCancelOutgoing: (RoommateRequest) -> Unit,
    onViewContact: (RoommateRequest) -> Unit,
) {
    val incoming = requests.filter { it.incoming }
    val outgoing = requests.filterNot { it.incoming }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
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
            OutgoingRequestCard(
                request = request,
                onCancel = { onCancelOutgoing(request) },
                onViewContact = { onViewContact(request) },
            )
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
private fun OutgoingRequestCard(
    request: RoommateRequest,
    onCancel: () -> Unit,
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
                    request.listingTitle.ifBlank { request.fromName },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                StatusChip(request.status)
            }
            Text(
                request.message.ifBlank { "Roommate request sent" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            when (request.status) {
                RoommateRequestStatus.PENDING -> Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Rounded.Close, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Withdraw Request")
                    }
                }
                RoommateRequestStatus.ACCEPTED -> Button(
                    onClick = onViewContact,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                ) {
                    Icon(Icons.Rounded.Check, null, Modifier.size(16.dp))
                    Text("  Accepted — View Contact")
                }
                RoommateRequestStatus.DECLINED -> Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
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

@Composable
private fun ContactDialog(request: RoommateRequest, onDismiss: () -> Unit) {
    val context = LocalContext.current
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
                        "They haven't listed a phone/email yet. You can check the listing card contact number.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                } else {
                    entries.forEach { (label, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (label) {
                                        "Phone" -> runCatching {
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${value.filter { it.isDigit() || it == '+' }}")))
                                        }
                                        "Email" -> runCatching {
                                            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$value")))
                                        }
                                    }
                                }
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
                                color = PeersPrimaryIndigo,
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
