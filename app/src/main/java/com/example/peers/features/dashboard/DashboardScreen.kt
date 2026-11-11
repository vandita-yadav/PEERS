package com.example.peers.features.dashboard

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.CalendarMonth
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peers.features.auth.AuthViewModel
import com.example.peers.features.dashboard.components.PeersHeader
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import kotlinx.coroutines.launch

/** Bottom-navigation destinations for the app shell. */
private enum class DashTab(val label: String) {
    HOME("Home"), COMMUNITY("Community"), EVENTS("Events"), PROFILE("Profile")
}

/**
 * App shell: modal side drawer + bottom navigation + tab content.
 */
@Composable
fun DashboardScreen(
    onSignOut: () -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel(),
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(DashTab.HOME) }
    var showNotifications by remember { mutableStateOf(false) }
    val user by dashboardViewModel.user.collectAsStateWithLifecycle()
    val unreadCount by dashboardViewModel.unreadCount.collectAsStateWithLifecycle()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PeersDrawerSheet(
                viewModel = dashboardViewModel,
                onSignOut = onSignOut,
                onNavigateToNotifications = {
                    showNotifications = true
                    scope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    DashTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
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
            },
        ) { innerPadding ->
            if (showNotifications) {
                NotificationsScreen(
                    viewModel = dashboardViewModel,
                    onBack = { showNotifications = false },
                    modifier = Modifier.padding(innerPadding),
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
                            DashTab.HOME -> DashboardHome(dashboardViewModel)
                            DashTab.COMMUNITY -> PlaceholderTab("🫂", "Community is coming next — connections & feed.")
                            DashTab.EVENTS -> PlaceholderTab("🎪", "Full event calendar arrives with the Events phase.")
                            DashTab.PROFILE -> PlaceholderTab("🧑", "Your public profile page is next.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderTab(emoji: String, message: String) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(32.dp),
    ) {
        Text(emoji, fontSize = 44.sp)
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

// region — Side drawer -------------------------------------------------------------------------

@Composable
private fun PeersDrawerSheet(
    viewModel: DashboardViewModel,
    onSignOut: () -> Unit,
    onNavigateToNotifications: () -> Unit,
) {
    val user by viewModel.user.collectAsStateWithLifecycle()

    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Profile section
            val emojis = listOf("🧑🏿", "👩🏿", "👦🏽", "👧🏼", "👩🏽", "🧔🏽", "🧕🏾")
            val pastels = listOf(PeersPrimaryIndigo, PeersLavender, PeersSky, com.example.peers.ui.theme.PeersMint)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(pastels[(user?.uid?.hashCode() ?: 0).mod(pastels.size)]),
            ) {
                Text(emojis[(user?.uid?.hashCode() ?: 0).mod(emojis.size)], fontSize = 30.sp)
            }
            Text(
                user?.name ?: "Peer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp))

            DrawerItem("🔔", "Notifications", onNavigateToNotifications)
            DrawerItem("🎪", "Events") { }
            DrawerItem("💰", "Budget Planner") { }
            DrawerItem("⭐", "Rate Us") { }

            HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp))

            DrawerItem("🚪", "Sign Out", onSignOut)
        }
    }
}

@Composable
private fun DrawerItem(emoji: String, label: String, onClick: () -> Unit) {
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
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}

// endregion
