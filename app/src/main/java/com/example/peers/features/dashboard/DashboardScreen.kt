package com.example.peers.features.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peers.R
import com.example.peers.features.accommodation.AccommodationScreen
import com.example.peers.features.assistant.AssistantScreen
import com.example.peers.features.budget.BudgetPlannerScreen
import com.example.peers.features.checklist.ChecklistScreen
import com.example.peers.features.contacts.ImportantContactsScreen
import com.example.peers.features.dashboard.components.InviteFriendSheet
import com.example.peers.features.dashboard.components.PeersHeader
import com.example.peers.features.events.EventsScreen
import com.example.peers.features.forum.ForumScreen
import com.example.peers.features.profile.EditProfileScreen
import com.example.peers.features.profile.PeersAvatarBadge
import com.example.peers.features.profile.ProfileScreen
import com.example.peers.features.rentals.RentalsTransportScreen
import com.example.peers.features.settings.SettingsScreen
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersDrawableIcon
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersThemeMode
import com.example.peers.ui.theme.isPeersDarkTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Bottom-navigation destinations for the app shell (Settings replaces Profile tab). */
private enum class DashTab(val label: String) {
    HOME("Home"), COMMUNITY("Discussion"), EVENTS("Events"), SETTINGS("Settings")
}

/**
 * App shell: modal side drawer + bottom navigation + tab & overlay routing.
 * Handles Android system Back cleanly across drawer, overlays, and bottom tabs.
 */
@Composable
fun DashboardScreen(
    themeMode: PeersThemeMode,
    onToggleTheme: () -> Unit,
    onSetTheme: ((PeersThemeMode) -> Unit)? = null,
    onSignOut: () -> Unit,
    onDeleteAccount: (onError: (String) -> Unit) -> Unit = {},
    dashboardViewModel: DashboardViewModel = viewModel(),
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(DashTab.HOME) }
    var targetEventDate by remember { mutableStateOf<LocalDate?>(null) }
    var eventsSavedOnly by remember { mutableStateOf(false) }

    var showProfile by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showAccommodation by remember { mutableStateOf(false) }
    var showRentals by remember { mutableStateOf(false) }
    var showContacts by remember { mutableStateOf(false) }
    var showBudget by remember { mutableStateOf(false) }
    var showChecklist by remember { mutableStateOf(false) }
    var showAssistant by remember { mutableStateOf(false) }
    var assistantInitialPrompt by remember { mutableStateOf<String?>(null) }
    var showInviteSheet by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var lastBackPressMs by remember { mutableLongStateOf(0L) }

    val user by dashboardViewModel.user.collectAsStateWithLifecycle()
    val unreadCount by dashboardViewModel.unreadCount.collectAsStateWithLifecycle()
    val savedEventIds by dashboardViewModel.savedEventIds.collectAsStateWithLifecycle()
    val actionMessage by dashboardViewModel.actionMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            dashboardViewModel.consumeActionMessage()
        }
    }

    fun clearAllOverlays() {
        showProfile = false
        showEditProfile = false
        showNotifications = false
        showAccommodation = false
        showRentals = false
        showContacts = false
        showBudget = false
        showChecklist = false
        showAssistant = false
    }

    // Top-level system Back navigation guard:
    // Drawer -> Active overlays -> Non-Home tabs -> Double-back guard on Home.
    val hasActiveOverlay = showEditProfile || showProfile || showNotifications ||
        showAccommodation || showRentals || showContacts || showBudget || showChecklist || showAssistant

    BackHandler(enabled = drawerState.isOpen || !hasActiveOverlay) {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            currentTab != DashTab.HOME -> {
                if (currentTab == DashTab.EVENTS) dashboardViewModel.refreshEvents()
                currentTab = DashTab.HOME
            }
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressMs < 2000L) {
                    (context as? android.app.Activity)?.finish()
                } else {
                    lastBackPressMs = now
                    scope.launch {
                        snackbarHostState.showSnackbar("Press back again to exit PEERS")
                    }
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PeersDrawerSheet(
                viewModel = dashboardViewModel,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onOpenProfile = {
                    clearAllOverlays()
                    showProfile = true
                    scope.launch { drawerState.close() }
                },
                onNavigateToNotifications = {
                    clearAllOverlays()
                    showNotifications = true
                    scope.launch { drawerState.close() }
                },
                onNavigateToAccommodation = {
                    clearAllOverlays()
                    showAccommodation = true
                    scope.launch { drawerState.close() }
                },
                onNavigateToRentals = {
                    clearAllOverlays()
                    showRentals = true
                    scope.launch { drawerState.close() }
                },
                onNavigateToContacts = {
                    clearAllOverlays()
                    showContacts = true
                    scope.launch { drawerState.close() }
                },
                onNavigateToEvents = {
                    clearAllOverlays()
                    eventsSavedOnly = false
                    currentTab = DashTab.EVENTS
                    scope.launch { drawerState.close() }
                },
                onNavigateToBudget = {
                    clearAllOverlays()
                    showBudget = true
                    scope.launch { drawerState.close() }
                },
                onNavigateToChecklist = {
                    clearAllOverlays()
                    showChecklist = true
                    scope.launch { drawerState.close() }
                },
                onNavigateToAssistant = {
                    clearAllOverlays()
                    assistantInitialPrompt = null
                    showAssistant = true
                    scope.launch { drawerState.close() }
                },
                onNavigateToInvite = {
                    showInviteSheet = true
                    scope.launch { drawerState.close() }
                },
                onNavigateToSettings = {
                    clearAllOverlays()
                    currentTab = DashTab.SETTINGS
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
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!hasActiveOverlay) {
                    val isDark = isPeersDarkTheme()
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        DashTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentTab == DashTab.EVENTS && tab != DashTab.EVENTS) {
                                        dashboardViewModel.refreshEvents()
                                    }
                                    currentTab = tab
                                    if (tab != DashTab.EVENTS) {
                                        targetEventDate = null
                                        eventsSavedOnly = false
                                    }
                                    clearAllOverlays()
                                },
                                icon = {
                                    val iconTint = if (selected) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    when (tab) {
                                        DashTab.HOME -> Icon(Icons.Rounded.Home, contentDescription = tab.label)
                                        DashTab.COMMUNITY -> Icon(
                                            painter = painterResource(
                                                if (isDark) R.drawable.community_white else R.drawable.community_black
                                            ),
                                            contentDescription = tab.label,
                                            tint = iconTint,
                                            modifier = Modifier.size(22.dp),
                                        )
                                        DashTab.EVENTS -> Icon(
                                            painter = painterResource(
                                                if (isDark) R.drawable.events_white else R.drawable.events_black
                                            ),
                                            contentDescription = tab.label,
                                            tint = iconTint,
                                            modifier = Modifier.size(22.dp),
                                        )
                                        DashTab.SETTINGS -> Icon(
                                            painter = painterResource(
                                                if (isDark) R.drawable.settings_white else R.drawable.settings_black
                                            ),
                                            contentDescription = tab.label,
                                            tint = iconTint,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        fontFamily = GoogleSans,
                                    )
                                },
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
                showProfile -> {
                    ProfileScreen(
                        user = user,
                        unreadCount = unreadCount,
                        savedEventsCount = savedEventIds.size,
                        onBack = { showProfile = false },
                        onBellClick = {
                            showProfile = false
                            showNotifications = true
                        },
                        onEditProfile = { showEditProfile = true },
                        onOpenSettings = {
                            showProfile = false
                            currentTab = DashTab.SETTINGS
                        },
                        onOpenSavedEvents = {
                            showProfile = false
                            targetEventDate = null
                            eventsSavedOnly = true
                            currentTab = DashTab.EVENTS
                        },
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    )
                }
                showNotifications -> {
                    NotificationsScreen(
                        viewModel = dashboardViewModel,
                        onBack = { showNotifications = false },
                        onProfileClick = {
                            showNotifications = false
                            showProfile = true
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showAccommodation -> {
                    AccommodationScreen(
                        onBack = { showAccommodation = false },
                        user = user,
                        unreadCount = unreadCount,
                        onProfileClick = {
                            showAccommodation = false
                            showProfile = true
                        },
                        onBellClick = {
                            showAccommodation = false
                            showNotifications = true
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showRentals -> {
                    RentalsTransportScreen(
                        onBack = { showRentals = false },
                        user = user,
                        unreadCount = unreadCount,
                        onProfileClick = {
                            showRentals = false
                            showProfile = true
                        },
                        onBellClick = {
                            showRentals = false
                            showNotifications = true
                        },
                        onAskAiRentals = { prompt ->
                            showRentals = false
                            assistantInitialPrompt = prompt
                            showAssistant = true
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showContacts -> {
                    ImportantContactsScreen(
                        user = user,
                        unreadCount = unreadCount,
                        onBack = { showContacts = false },
                        onProfileClick = {
                            showContacts = false
                            showProfile = true
                        },
                        onBellClick = {
                            showContacts = false
                            showNotifications = true
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showBudget -> {
                    BudgetPlannerScreen(
                        onBack = { showBudget = false },
                        user = user,
                        unreadCount = unreadCount,
                        onProfileClick = {
                            showBudget = false
                            showProfile = true
                        },
                        onBellClick = {
                            showBudget = false
                            showNotifications = true
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showChecklist -> {
                    ChecklistScreen(
                        user = user,
                        onBack = { showChecklist = false },
                        unreadCount = unreadCount,
                        onProfileClick = {
                            showChecklist = false
                            showProfile = true
                        },
                        onBellClick = {
                            showChecklist = false
                            showNotifications = true
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                showAssistant -> {
                    AssistantScreen(
                        onBack = { showAssistant = false },
                        user = user,
                        unreadCount = unreadCount,
                        onProfileClick = {
                            showAssistant = false
                            showProfile = true
                        },
                        onBellClick = {
                            showAssistant = false
                            showNotifications = true
                        },
                        initialPrompt = assistantInitialPrompt,
                        onInitialPromptConsumed = { assistantInitialPrompt = null },
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
                        onNavigateToRentals = {
                            showAssistant = false
                            showRentals = true
                        },
                        onNavigateToEvents = {
                            showAssistant = false
                            eventsSavedOnly = false
                            currentTab = DashTab.EVENTS
                        },
                        onNavigateToCommunity = {
                            showAssistant = false
                            currentTab = DashTab.COMMUNITY
                        },
                        onNavigateToContacts = {
                            showAssistant = false
                            showContacts = true
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                else -> {
                    when (currentTab) {
                        DashTab.SETTINGS -> {
                            SettingsScreen(
                                user = user,
                                themeMode = themeMode,
                                onSetTheme = { newMode ->
                                    onSetTheme?.invoke(newMode) ?: onToggleTheme()
                                },
                                onOpenInvite = { showInviteSheet = true },
                                onOpenRateUs = { openPlayStore(context) },
                                onRequestSignOut = { showSignOutDialog = true },
                                onDeleteAccount = onDeleteAccount,
                                onOpenProfile = { showProfile = true },
                                unreadCount = unreadCount,
                                onBellClick = { showNotifications = true },
                                onBack = { currentTab = DashTab.HOME },
                                modifier = Modifier.padding(innerPadding),
                            )
                        }
                        DashTab.COMMUNITY -> {
                            ForumScreen(
                                user = user,
                                unreadCount = unreadCount,
                                onProfileClick = { showProfile = true },
                                onBellClick = { showNotifications = true },
                                onBack = { currentTab = DashTab.HOME },
                                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                            )
                        }
                        DashTab.EVENTS -> {
                            EventsScreen(
                                initialDate = targetEventDate,
                                initialSavedOnly = eventsSavedOnly,
                                user = user,
                                unreadCount = unreadCount,
                                onProfileClick = { showProfile = true },
                                onBellClick = { showNotifications = true },
                                onBack = {
                                    dashboardViewModel.refreshEvents()
                                    eventsSavedOnly = false
                                    currentTab = DashTab.HOME
                                },
                                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                            )
                        }
                        DashTab.HOME -> {
                            Column(Modifier.padding(innerPadding)) {
                                PeersHeader(
                                    user = user,
                                    unreadCount = unreadCount,
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onBellClick = { showNotifications = true },
                                    onProfileClick = { showProfile = true },
                                )
                                DashboardHome(
                                    viewModel = dashboardViewModel,
                                    onOpenAccommodation = { showAccommodation = true },
                                    onOpenRentals = { showRentals = true },
                                    onOpenContacts = { showContacts = true },
                                    onOpenBudget = { showBudget = true },
                                    onOpenChecklist = { showChecklist = true },
                                    onOpenAssistant = {
                                        assistantInitialPrompt = null
                                        showAssistant = true
                                    },
                                    onOpenEvents = { date ->
                                        targetEventDate = date
                                        eventsSavedOnly = false
                                        currentTab = DashTab.EVENTS
                                    },
                                    onOpenCommunity = { currentTab = DashTab.COMMUNITY },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInviteSheet) {
        InviteFriendSheet(onDismiss = { showInviteSheet = false })
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    "Sign out of PEERS?",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Text(
                    "You will need to sign in again to access your PEERS account.",
                    fontFamily = Lato,
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
                    Text("Sign Out", fontFamily = GoogleSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", fontFamily = GoogleSans)
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
    onOpenProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAccommodation: () -> Unit,
    onNavigateToRentals: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    onNavigateToAssistant: () -> Unit,
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
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Clickable Profile Banner -> opens Profile Screen ──────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(PeersPrimaryIndigo, PeersLavender)
                        )
                    )
                    .clickable(onClick = onOpenProfile)
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
                        fontFamily = GoogleSans,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        user?.email ?: "",
                        fontFamily = Lato,
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
                                user?.university.orEmpty(),
                                fontFamily = Lato,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                    Text(
                        "Tap to view profile",
                        fontFamily = Lato,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                DrawerSectionLabel("EXPLORE")
                DrawerItem(R.drawable.notification_black, R.drawable.notification_white, "Notifications", onClick = onNavigateToNotifications)
                DrawerItem(R.drawable.aibot_black, R.drawable.aibot_white, "AI Student Assistant", onClick = onNavigateToAssistant)
                DrawerItem(R.drawable.accomodation_black, R.drawable.accomodation_white, "Accommodation", onClick = onNavigateToAccommodation)
                DrawerItem(R.drawable.job_black, R.drawable.job_white, "Rentals & Transport", onClick = onNavigateToRentals)
                DrawerItem(R.drawable.events_black, R.drawable.events_white, "Events", onClick = onNavigateToEvents)
                DrawerItem(R.drawable.budget_black, R.drawable.budget_white, "Budget Planner", onClick = onNavigateToBudget)
                DrawerItem(R.drawable.checklist_black, R.drawable.checklist_white, "Student Checklist", onClick = onNavigateToChecklist)
                DrawerItem(R.drawable.info_black, R.drawable.info_white, "Helplines & Contacts", onClick = onNavigateToContacts)

                DrawerSectionLabel("COMMUNITY")
                DrawerItem(R.drawable.invite_black, R.drawable.invite_white, "Invite Friends", onClick = onNavigateToInvite)

                DrawerSectionLabel("ACCOUNT & PREFERENCES")
                DrawerItem(R.drawable.community_black, R.drawable.community_white, "My Profile", onClick = onOpenProfile)
                DrawerItem(R.drawable.settings_black, R.drawable.settings_white, "Settings", onClick = onNavigateToSettings)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onToggleTheme() }
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                ) {
                    PeersDrawableIcon(
                        lightRes = R.drawable.darktheme_black,
                        darkRes = R.drawable.darktheme_white,
                        contentDescription = "Dark Theme",
                        size = 22.dp,
                    )
                    Text(
                        "Dark Theme",
                        fontFamily = Lato,
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

                DrawerItem(R.drawable.rate_black, R.drawable.rate_white, "Rate Us", onClick = onNavigateToRateUs)

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                DrawerItem(
                    R.drawable.logout_black,
                    R.drawable.logout_white,
                    "Sign Out",
                    tint = MaterialTheme.colorScheme.error,
                    useIconTint = true,
                    onClick = onConfirmSignOut
                )
            }
        }
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text,
        fontFamily = GoogleSans,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 10.dp, top = 14.dp, bottom = 4.dp),
    )
}

@Composable
private fun DrawerItem(
    @DrawableRes lightIcon: Int,
    @DrawableRes darkIcon: Int,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    useIconTint: Boolean = false,
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
        PeersDrawableIcon(
            lightRes = lightIcon,
            darkRes = darkIcon,
            contentDescription = label,
            size = 22.dp,
            tint = if (useIconTint) tint else null,
        )
        Text(
            label,
            fontFamily = Lato,
            style = MaterialTheme.typography.bodyLarge,
            color = tint,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}

// endregion

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
