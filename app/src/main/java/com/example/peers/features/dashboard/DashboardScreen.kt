package com.example.peers.features.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peers.features.accommodation.AccommodationScreen
import com.example.peers.features.assistant.AssistantScreen
import com.example.peers.features.budget.BudgetPlannerScreen
import com.example.peers.features.checklist.ChecklistScreen
import com.example.peers.features.community.ConnectionState
import com.example.peers.features.dashboard.components.InviteFriendSheet
import com.example.peers.features.dashboard.components.PeersHeader
import com.example.peers.features.events.EventsScreen
import com.example.peers.features.forum.ForumScreen
import com.example.peers.features.profile.EditProfileScreen
import com.example.peers.features.profile.PeersAvatarBadge
import com.example.peers.features.profile.ProfileScreen
import com.example.peers.features.settings.SettingsScreen
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import com.example.peers.ui.theme.PeersThemeMode
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Bottom-navigation destinations for the app shell. */
private enum class DashTab(val label: String) {
    HOME("Home"), COMMUNITY("Community"), EVENTS("Events"), PROFILE("Profile")
}

/**
 * App shell: modal side drawer + bottom navigation + tab content.
 */
@Composable
fun DashboardScreen(
    themeMode: PeersThemeMode,
    onToggleTheme: () -> Unit,
    onSetTheme: ((PeersThemeMode) -> Unit)? = null,
    onSignOut: () -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel(),
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(DashTab.HOME) }
    var targetEventDate by remember { mutableStateOf<LocalDate?>(null) }

    var showNotifications by remember { mutableStateOf(false) }
    var showAccommodation by remember { mutableStateOf(false) }
    var showBudget by remember { mutableStateOf(false) }
    var showChecklist by remember { mutableStateOf(false) }
    var showAssistant by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showInviteSheet by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val user by dashboardViewModel.user.collectAsStateWithLifecycle()
    val unreadCount by dashboardViewModel.unreadCount.collectAsStateWithLifecycle()
    val connectionStates by dashboardViewModel.connectionStates.collectAsStateWithLifecycle()
    val savedEventIds by dashboardViewModel.savedEventIds.collectAsStateWithLifecycle()
    val actionMessage by dashboardViewModel.actionMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            dashboardViewModel.consumeActionMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PeersDrawerSheet(
                viewModel = dashboardViewModel,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onClose = { scope.launch { drawerState.close() } },
                onNavigateToNotifications = {
                    showNotifications = true
                    showAssistant = false
                    showAccommodation = false
                    showBudget = false
                    showChecklist = false
                    showEditProfile = false
                    showSettings = false
                    scope.launch { drawerState.close() }
                },
                onNavigateToAccommodation = {
                    showAccommodation = true
                    showAssistant = false
                    showNotifications = false
                    showBudget = false
                    showChecklist = false
                    showEditProfile = false
                    showSettings = false
                    scope.launch { drawerState.close() }
                },
                onNavigateToEvents = {
                    currentTab = DashTab.EVENTS
                    showAssistant = false
                    showNotifications = false
                    showAccommodation = false
                    showBudget = false
                    showChecklist = false
                    showEditProfile = false
                    showSettings = false
                    scope.launch { drawerState.close() }
                },
                onNavigateToBudget = {
                    showBudget = true
                    showAssistant = false
                    showNotifications = false
                    showAccommodation = false
                    showChecklist = false
                    showEditProfile = false
                    showSettings = false
                    scope.launch { drawerState.close() }
                },
                onNavigateToChecklist = {
                    showChecklist = true
                    showAssistant = false
                    showNotifications = false
                    showAccommodation = false
                    showBudget = false
                    showEditProfile = false
                    showSettings = false
                    scope.launch { drawerState.close() }
                },
                onNavigateToAssistant = {
                    showAssistant = true
                    showChecklist = false
                    showNotifications = false
                    showAccommodation = false
                    showBudget = false
                    showEditProfile = false
                    showSettings = false
                    scope.launch { drawerState.close() }
                },
                onNavigateToInvite = {
                    showInviteSheet = true
                    scope.launch { drawerState.close() }
                },
                onNavigateToSettings = {
                    showSettings = true
                    showAssistant = false
                    showChecklist = false
                    showNotifications = false
                    showAccommodation = false
                    showBudget = false
                    showEditProfile = false
                    scope.launch { drawerState.close() }
                },
                onNavigateToRateUs = {
                    scope.launch { drawerState.close() }
                    openPlayStore(context)
                },
                onConfirmSignOut = {
                    scope.launch { drawerState.close() }
                    showSignOutDialog = true
                },
            )
        },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!showEditProfile && !showSettings && !showNotifications && !showAccommodation && !showBudget && !showChecklist && !showAssistant) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        DashTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = {
                                    currentTab = tab
                                    if (tab != DashTab.EVENTS) {
                                        targetEventDate = null
                                    }
                                    showNotifications = false
                                    showAccommodation = false
                                    showBudget = false
                                    showChecklist = false
                                    showAssistant = false
                                    showEditProfile = false
                                    showSettings = false
                                },
                                icon = {
                                    when (tab) {
                                        DashTab.HOME -> Icon(Icons.Rounded.Home, null)
                                        DashTab.COMMUNITY -> Icon(Icons.Rounded.Groups, null)
                                        DashTab.EVENTS -> Icon(Icons.Rounded.CalendarMonth, null)
                                        DashTab.PROFILE -> Icon(Icons.Rounded.Person, null)
                                    }
                                },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = PeersPrimaryIndigo,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = PeersPrimaryIndigo,
                                ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            when {
                showEditProfile -> {
                    EditProfileScreen(
                        user = user,
                        onBack = { showEditProfile = false },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showSettings -> {
                    SettingsScreen(
                        user = user,
                        themeMode = themeMode,
                        onSetTheme = { newMode ->
                            onSetTheme?.invoke(newMode) ?: onToggleTheme()
                        },
                        onOpenInvite = { showInviteSheet = true },
                        onOpenRateUs = { openPlayStore(context) },
                        onRequestSignOut = { showSignOutDialog = true },
                        onBack = { showSettings = false },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showNotifications -> {
                    NotificationsScreen(
                        viewModel = dashboardViewModel,
                        onBack = { showNotifications = false },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showAccommodation -> {
                    AccommodationScreen(
                        onBack = { showAccommodation = false },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showBudget -> {
                    BudgetPlannerScreen(
                        onBack = { showBudget = false },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showChecklist -> {
                    ChecklistScreen(
                        user = user,
                        onBack = { showChecklist = false },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showAssistant -> {
                    AssistantScreen(
                        onBack = { showAssistant = false },
                        onNavigateToChecklist = {
                            showAssistant = false
                            showChecklist = true
                        },
                        onNavigateToBudget = {
                            showAssistant = false
                            showBudget = true
                        },
                        onNavigateToAccommodation = {
                            showAssistant = false
                            showAccommodation = true
                        },
                        onNavigateToCommunity = {
                            showAssistant = false
                            currentTab = DashTab.COMMUNITY
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                else -> {
                    if (currentTab == DashTab.PROFILE) {
                        ProfileScreen(
                            user = user,
                            unreadCount = unreadCount,
                            connectionCount = connectionStates.values.count { it == ConnectionState.CONNECTED },
                            savedEventsCount = savedEventIds.size,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onBellClick = { showNotifications = true },
                            onEditProfile = { showEditProfile = true },
                            onOpenSettings = { showSettings = true },
                            modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        )
                    } else {
                        Column(Modifier.padding(innerPadding)) {
                            PeersHeader(
                                user = user,
                                unreadCount = unreadCount,
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onBellClick = { showNotifications = true },
                            )
                            Box(Modifier.fillMaxSize()) {
                                when (currentTab) {
                                    DashTab.HOME -> DashboardHome(
                                        viewModel = dashboardViewModel,
                                        onOpenAccommodation = { showAccommodation = true },
                                        onOpenBudget = { showBudget = true },
                                        onOpenChecklist = { showChecklist = true },
                                        onOpenAssistant = { showAssistant = true },
                                        onOpenEvents = { date ->
                                            targetEventDate = date
                                            currentTab = DashTab.EVENTS
                                        },
                                        onOpenCommunity = { currentTab = DashTab.COMMUNITY },
                                    )
                                    DashTab.COMMUNITY -> ForumScreen()
                                    DashTab.EVENTS -> EventsScreen(initialDate = targetEventDate)
                                    DashTab.PROFILE -> Unit
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Invite Friends
    if (showInviteSheet) {
        InviteFriendSheet(onDismiss = { showInviteSheet = false })
    }

    // Confirmation Dialog: Sign Out (Part 8 specification)
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    "Sign out of PEERS?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Text(
                    "You will need to sign in again with your Google account to use PEERS.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

// region — Side drawer -------------------------------------------------------------------------

@Composable
private fun PeersDrawerSheet(
    viewModel: DashboardViewModel,
    themeMode: PeersThemeMode,
    onToggleTheme: () -> Unit,
    onClose: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAccommodation: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    onNavigateToAssistant: () -> Unit = {},
    onNavigateToInvite: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRateUs: () -> Unit,
    onConfirmSignOut: () -> Unit,
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val isDark = themeMode == PeersThemeMode.NeonDark

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(310.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Profile banner ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(PeersPrimaryIndigo, PeersLavender)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 26.dp),
            ) {
                Column {
                    PeersAvatarBadge(
                        avatarIndex = user?.avatarIndex,
                        photoUrl = user?.photoUrl,
                        size = 68.dp,
                        borderWidth = 3.dp,
                        borderColor = Color.White.copy(alpha = 0.85f),
                    )
                    Text(
                        user?.name ?: "Peer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        user?.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    if (!user?.university.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            Text(
                                "🎓 ${user?.university}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                DrawerSectionLabel("EXPLORE")
                DrawerItem("🔔", "Notifications", onClick = onNavigateToNotifications)
                DrawerItem("🤖", "AI Student Assistant", onClick = onNavigateToAssistant)
                DrawerItem("🏠", "Accommodation", onClick = onNavigateToAccommodation)
                DrawerItem("🎪", "Events", onClick = onNavigateToEvents)
                DrawerItem("💰", "Budget Planner", onClick = onNavigateToBudget)
                DrawerItem("📝", "Student Checklist", onClick = onNavigateToChecklist)

                DrawerSectionLabel("COMMUNITY")
                DrawerItem("💌", "Invite Friends", onClick = onNavigateToInvite)

                DrawerSectionLabel("PREFERENCES")
                DrawerItem("⚙️", "Settings", onClick = onNavigateToSettings)

                // Dark theme switch row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onToggleTheme() }
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                ) {
                    Text("🌙", fontSize = 20.sp)
                    Text(
                        "Dark Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp),
                    )
                    Switch(
                        checked = isDark,
                        onCheckedChange = { onToggleTheme() },
                    )
                }

                DrawerItem("⭐", "Rate Us", onClick = onNavigateToRateUs)

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                DrawerItem("🚪", "Sign Out", tint = MaterialTheme.colorScheme.error, onClick = onConfirmSignOut)
            }
        }
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 10.dp, top = 14.dp, bottom = 4.dp),
    )
}

@Composable
private fun DrawerItem(
    emoji: String,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = tint,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}

// endregion

/**
 * Launches the Google Play Store app or falls back to browser (Part 6 spec).
 */
private fun openPlayStore(context: Context) {
    val pkg = context.packageName
    val marketUri = Uri.parse("market://details?id=$pkg")
    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    runCatching {
        context.startActivity(marketIntent)
    }.onFailure {
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        runCatching {
            context.startActivity(webIntent)
        }.onFailure {
            Toast.makeText(context, "Couldn't open the Play Store. Please try again later.", Toast.LENGTH_SHORT).show()
        }
    }
}
