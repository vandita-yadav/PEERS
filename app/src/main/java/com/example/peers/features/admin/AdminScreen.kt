package com.example.peers.features.admin

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.features.dashboard.components.PeersSubScreenHeader
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    user: DashboardUser?,
    unreadCount: Int,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddAdminDialog by remember { mutableStateOf(false) }
    var newAdminEmail by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    val isAuthorized = AdminConfig.isAdmin(user?.email, context) || viewModel.isCurrentUserAdmin

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PeersSubScreenHeader(
                title = "Admin Console",
                subtitle = "Governance, moderation & broadcasts",
                user = user,
                unreadCount = unreadCount,
                onBack = onBack,
                onProfileClick = onProfileClick,
                onBellClick = onBellClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        if (!isAuthorized) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
            ) {
                Icon(
                    Icons.Rounded.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Restricted Admin Area",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    "Only authorized PEERS administrators can access this console.",
                    fontFamily = Lato,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = PeersPrimaryIndigo,
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Overview", fontSize = 12.sp) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = {
                    Text(if (state.reports.isNotEmpty()) "Moderation (${state.reports.size})" else "Moderation", fontSize = 12.sp)
                })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Listings", fontSize = 12.sp) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Users (${state.users.size})", fontSize = 12.sp) })
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing || state.isLoading,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refreshAll { isRefreshing = false }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (selectedTab) {
                    0 -> AdminOverviewTab(
                        userEmail = user?.email.orEmpty(),
                        state = state,
                        onSendBroadcast = { t, m -> viewModel.sendBroadcast(t, m) },
                        onOpenAddAdmin = { showAddAdminDialog = true },
                        onRemoveCoAdmin = viewModel::removeCoAdmin,
                    )
                    1 -> AdminModerationTab(
                        state = state,
                        onDismissReport = viewModel::dismissReport,
                        onDeletePost = viewModel::deletePost,
                    )
                    2 -> AdminListingsTab(
                        state = state,
                        onVerifyStay = viewModel::verifyAccommodation,
                        onToggleStayAvail = viewModel::toggleAccommodationAvailability,
                        onDeleteStay = viewModel::deleteAccommodation,
                        onDeleteEvent = viewModel::deleteEvent,
                        onDeleteRental = viewModel::deleteRental,
                    )
                    3 -> AdminUsersTab(
                        users = state.users,
                        admins = state.admins,
                        onGrantAdmin = viewModel::addCoAdmin,
                        onDeleteUser = viewModel::deleteUserProfile,
                    )
                }
            }
        }
    }

    if (showAddAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAddAdminDialog = false },
            title = { Text("Add Co-Admin", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter the Google or PEERS login email address of the teammate you want to grant Admin Console access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = newAdminEmail,
                        onValueChange = { newAdminEmail = it },
                        label = { Text("Admin Email") },
                        placeholder = { Text("teammate@gmail.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCoAdmin(newAdminEmail)
                        newAdminEmail = ""
                        showAddAdminDialog = false
                    },
                    enabled = newAdminEmail.contains("@") && newAdminEmail.contains("."),
                    colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                ) { Text("Grant Admin Access") }
            },
            dismissButton = {
                TextButton(onClick = { showAddAdminDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun AdminOverviewTab(
    userEmail: String,
    state: AdminUiState,
    onSendBroadcast: (String, String) -> Unit,
    onOpenAddAdmin: () -> Unit,
    onRemoveCoAdmin: (String) -> Unit,
) {
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastBody by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(PeersPrimaryIndigo, PeersLavender)))
                        .padding(18.dp),
                ) {
                    Text(
                        "PEERS Admin Control Center",
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                    )
                    Text(
                        "Signed in as ${userEmail.ifBlank { "Administrator" }}",
                        fontFamily = Lato,
                        fontSize = 12.5.sp,
                        color = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile("Users", "${state.users.size}", PeersLavender, Modifier.weight(1f))
                StatTile("Stays", "${state.accommodations.size}", PeersSky, Modifier.weight(1f))
                StatTile("Reports", "${state.reports.size}", Color(0xFFFFD9E2), Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile("Discussions", "${state.posts.size}", PeersMint, Modifier.weight(1f))
                StatTile("Events", "${state.events.size}", PeersSky, Modifier.weight(1f))
                StatTile("Rentals", "${state.rentals.size}", PeersLavender, Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Campaign, contentDescription = null, tint = PeersPrimaryIndigo)
                        Spacer(Modifier.width(8.dp))
                        Text("Broadcast Campus Alert", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                    }
                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text("Alert Title") },
                        placeholder = { Text("e.g. Campus Shuttle Update / Exam Schedule") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = broadcastBody,
                        onValueChange = { broadcastBody = it },
                        label = { Text("Alert Message") },
                        placeholder = { Text("Message delivered both in-app and in Android notifications...") },
                        minLines = 2,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            onSendBroadcast(broadcastTitle, broadcastBody)
                            broadcastTitle = ""
                            broadcastBody = ""
                        },
                        enabled = broadcastTitle.isNotBlank() && broadcastBody.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Send Broadcast Alert", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Authorized Admins (${state.admins.size})", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = onOpenAddAdmin,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add", fontSize = 12.sp)
                        }
                    }
                    state.admins.sorted().forEach { email ->
                        val isRoot = AdminConfig.isRootAdmin(email)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) {
                            Icon(Icons.Rounded.Verified, contentDescription = null, tint = PeersPrimaryIndigo, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(email, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            if (isRoot) {
                                Surface(shape = RoundedCornerShape(50), color = PeersPrimaryIndigo.copy(alpha = 0.14f)) {
                                    Text("Root Admin", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PeersPrimaryIndigo, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            } else {
                                IconButton(onClick = { onRemoveCoAdmin(email) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Remove co-admin", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(17.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.25f),
        modifier = modifier,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)) {
            Text(value, fontFamily = GoogleSans, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(label, fontFamily = Lato, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdminModerationTab(
    state: AdminUiState,
    onDismissReport: (String) -> Unit,
    onDeletePost: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("Pending Reports (${state.reports.size})", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        if (state.reports.isEmpty()) {
            item { Text("No reported forum items in queue.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(state.reports, key = { it.id }) { report ->
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Reported ${report.targetType} · ${report.reason}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text("Target ID: ${report.targetId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            OutlinedButton(onClick = { onDismissReport(report.id) }, shape = RoundedCornerShape(10.dp)) {
                                Icon(Icons.Rounded.DoneAll, null, Modifier.size(16.dp))
                                Text("  Dismiss")
                            }
                            if (report.targetType == "post") {
                                Button(
                                    onClick = { onDeletePost(report.targetId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(10.dp),
                                ) { Text("Delete Post") }
                            }
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Recent Discussions (${state.posts.size})", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        items(state.posts, key = { it.id }) { post ->
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(post.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${post.communityName} · ${post.university.ifBlank { "Campus" }} · by ${post.authorName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onDeletePost(post.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete post", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminListingsTab(
    state: AdminUiState,
    onVerifyStay: (com.example.peers.features.accommodation.AccommodationListing) -> Unit,
    onToggleStayAvail: (com.example.peers.features.accommodation.AccommodationListing) -> Unit,
    onDeleteStay: (String) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onDeleteRental: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("Firestore Accommodations (${state.accommodations.size})", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        if (state.accommodations.isEmpty()) {
            item { Text("No Firestore-synced user accommodations yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(state.accommodations, key = { it.id }) { stay ->
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("${stay.title} · ₹${stay.rentPerMonth}/mo", fontWeight = FontWeight.Bold)
                        Text("${stay.address} · Status: ${stay.availability} · Verified: ${stay.verifiedCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            FilledTonalButton(onClick = { onVerifyStay(stay) }) {
                                Icon(Icons.Rounded.CheckCircle, null, Modifier.size(16.dp))
                                Text("  Verify")
                            }
                            OutlinedButton(onClick = { onToggleStayAvail(stay) }) {
                                Text("Toggle Avail.")
                            }
                            IconButton(onClick = { onDeleteStay(stay.id) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete stay", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Firestore Events (${state.events.size})", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        items(state.events, key = { it.id }) { ev ->
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(ev.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${ev.organizerName} · ${ev.venue}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onDeleteEvent(ev.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete event", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Firestore Rentals & Exchange (${state.rentals.size})", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        items(state.rentals, key = { it.id }) { item ->
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(item.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${item.category} · ${item.postedBy} · ${item.rateLabel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onDeleteRental(item.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete rental", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTab(
    users: List<AdminUserRecord>,
    admins: Set<String>,
    onGrantAdmin: (String) -> Unit,
    onDeleteUser: (AdminUserRecord) -> Unit,
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(users, search) {
        val q = search.trim()
        if (q.isBlank()) users
        else users.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.email.contains(q, ignoreCase = true) ||
                it.university.contains(q, ignoreCase = true)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search users by name, email, or university") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        items(filtered, key = { it.uid }) { record ->
            val isAdmin = record.email.trim().lowercase() in admins
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(PeersLavender.copy(alpha = 0.4f)),
                    ) {
                        Text(record.name.firstOrNull()?.uppercase() ?: "S", fontWeight = FontWeight.Bold, color = PeersPrimaryIndigo)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(record.name, fontWeight = FontWeight.Bold)
                        Text(record.email.ifBlank { record.uid }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (record.university.isNotBlank() || record.degreeLevel.isNotBlank()) {
                            Text("${record.degreeLevel} · ${record.university}", style = MaterialTheme.typography.labelSmall, color = PeersPrimaryIndigo)
                        }
                    }
                    if (!isAdmin && record.email.isNotBlank()) {
                        TextButton(onClick = { onGrantAdmin(record.email) }) {
                            Text("Make Admin", fontSize = 11.5.sp, color = PeersPrimaryIndigo)
                        }
                    }
                    if (!AdminConfig.isRootAdmin(record.email)) {
                        IconButton(onClick = { onDeleteUser(record) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Remove user", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
