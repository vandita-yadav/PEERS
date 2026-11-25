package com.example.peers.features.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.DrawableRes
import com.example.peers.R
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.features.dashboard.components.PeersSubScreenHeader
import com.example.peers.features.notifications.PeersNotificationHelper
import com.example.peers.features.profile.PeersAvatarBadge
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersDrawableIcon
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import com.example.peers.ui.theme.PeersThemeMode

/**
 * Settings Screen (Part 5 of Specification):
 * Appearance (Light, Dark, System Default), Notifications toggles,
 * Account information, Privacy & App Permissions, Support, Rate Us, Version.
 */
@Composable
fun SettingsScreen(
    user: DashboardUser?,
    themeMode: PeersThemeMode,
    onSetTheme: (PeersThemeMode) -> Unit,
    onOpenInvite: () -> Unit,
    onOpenRateUs: () -> Unit,
    onRequestSignOut: () -> Unit,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit = {},
    unreadCount: Int = 0,
    onBellClick: () -> Unit = {},
    onDeleteAccount: (onError: (String) -> Unit) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()

    BackHandler { onBack() }

    // Local notifications preference cache
    val prefs = remember { context.getSharedPreferences("peers_settings", Context.MODE_PRIVATE) }
    var masterNotifs by remember { mutableStateOf(prefs.getBoolean("notif_master", true)) }
    var connNotifs by remember { mutableStateOf(prefs.getBoolean("notif_conn", true)) }
    var roomNotifs by remember { mutableStateOf(prefs.getBoolean("notif_room", true)) }
    var eventNotifs by remember { mutableStateOf(prefs.getBoolean("notif_events", true)) }
    var forumNotifs by remember { mutableStateOf(prefs.getBoolean("notif_forum", true)) }

    var cameraGranted by remember { mutableStateOf(PeersNotificationHelper.hasCameraPermission(context)) }
    var locationGranted by remember { mutableStateOf(PeersNotificationHelper.hasLocationPermission(context)) }
    var notifGranted by remember { mutableStateOf(PeersNotificationHelper.hasNotificationPermission(context)) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var showAppSettingsPrompt by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraGranted = granted || PeersNotificationHelper.hasCameraPermission(context)
        if (granted) {
            Toast.makeText(context, "Camera permission granted.", Toast.LENGTH_SHORT).show()
        } else if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            showAppSettingsPrompt = "Camera"
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it } || PeersNotificationHelper.hasLocationPermission(context)
        locationGranted = granted
        if (granted) {
            Toast.makeText(context, "Location permission granted.", Toast.LENGTH_SHORT).show()
        } else if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showAppSettingsPrompt = "Location"
        }
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifGranted = granted || PeersNotificationHelper.hasNotificationPermission(context)
        if (notifGranted) {
            PeersNotificationHelper.showSystemNotification(
                context = context,
                id = 1001,
                title = "PEERS Alerts Active",
                body = "You will now receive campus events, forum replies, and updates on your phone.",
            )
            Toast.makeText(context, "Phone notifications enabled.", Toast.LENGTH_SHORT).show()
        } else if (activity != null && Build.VERSION.SDK_INT >= 33 && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
            showAppSettingsPrompt = "Notifications"
        }
    }

    fun updateNotif(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PeersSubScreenHeader(
                title = "Settings",
                subtitle = "Theme, permissions, alerts & account",
                user = user,
                unreadCount = unreadCount,
                onBack = onBack,
                onProfileClick = onOpenProfile,
                onBellClick = onBellClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            // ── Section 1: Appearance ─────────────────────────────────────────
            SettingsSectionHeader("APPEARANCE", R.drawable.darktheme_black, R.drawable.darktheme_white)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Theme Mode",
                        fontFamily = GoogleSans,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Choose your preferred look across all screens.",
                        fontFamily = Lato,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
                    )

                    ThemeOptionRow(
                        title = "Light Theme",
                        subtitle = "Clean plain white background",
                        lightIcon = R.drawable.darktheme_black,
                        darkIcon = R.drawable.darktheme_white,
                        selected = themeMode == PeersThemeMode.AestheticLight,
                        onClick = { onSetTheme(PeersThemeMode.AestheticLight) },
                    )
                    ThemeOptionRow(
                        title = "Dark Theme",
                        subtitle = "Modern high-contrast dark surface",
                        lightIcon = R.drawable.darktheme_black,
                        darkIcon = R.drawable.darktheme_white,
                        selected = themeMode == PeersThemeMode.NeonDark,
                        onClick = { onSetTheme(PeersThemeMode.NeonDark) },
                    )
                    ThemeOptionRow(
                        title = "System Default",
                        subtitle = "Follows your Android device appearance",
                        lightIcon = R.drawable.settings_black,
                        darkIcon = R.drawable.settings_white,
                        selected = themeMode == PeersThemeMode.SystemDefault,
                        onClick = { onSetTheme(PeersThemeMode.SystemDefault) },
                    )

                    // Theme preview strip
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PeersPrimaryIndigo)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PeersLavender)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PeersSky)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PeersMint)
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "Live Preview",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = PeersPrimaryIndigo,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 2: Notifications ──────────────────────────────────────
            SettingsSectionHeader("NOTIFICATIONS", R.drawable.notification_black, R.drawable.notification_white)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "In-App Notifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                "Master switch for student updates",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = masterNotifs,
                            onCheckedChange = {
                                masterNotifs = it
                                updateNotif("notif_master", it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PeersPrimaryIndigo),
                        )
                    }

                    if (masterNotifs) {
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        NotificationToggleItem(
                            title = "Connection Requests",
                            subtitle = "When peers want to connect with you",
                            checked = connNotifs,
                            onToggle = { connNotifs = it; updateNotif("notif_conn", it) },
                        )
                        NotificationToggleItem(
                            title = "Roommate Requests",
                            subtitle = "Roommate matches and enquiries",
                            checked = roomNotifs,
                            onToggle = { roomNotifs = it; updateNotif("notif_room", it) },
                        )
                        NotificationToggleItem(
                            title = "Campus Events & Meetups",
                            subtitle = "RSVP updates and event reminders",
                            checked = eventNotifs,
                            onToggle = { eventNotifs = it; updateNotif("notif_events", it) },
                        )
                        NotificationToggleItem(
                            title = "Discussions & Forum Replies",
                            subtitle = "When classmates reply or upvote your posts",
                            checked = forumNotifs,
                            onToggle = { forumNotifs = it; updateNotif("notif_forum", it) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 3: Account ────────────────────────────────────────────
            SettingsSectionHeader("ACCOUNT", R.drawable.community_black, R.drawable.community_white)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onOpenProfile)
                            .padding(vertical = 4.dp),
                    ) {
                        PeersAvatarBadge(
                            avatarIndex = user?.avatarIndex,
                            photoUrl = user?.photoUrl,
                            size = 54.dp,
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                user?.name ?: "Peer",
                                fontFamily = GoogleSans,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                user?.email ?: "",
                                fontFamily = Lato,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PeersMint.copy(alpha = 0.35f),
                                modifier = Modifier.padding(top = 4.dp),
                            ) {
                                Text(
                                    "Verified Account · Tap to open Profile",
                                    fontFamily = Lato,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1B6B2E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 14.dp))

                    OutlinedButton(
                        onClick = onRequestSignOut,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Out", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            deleteError = null
                            showDeleteDialog = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Account Permanently", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 4: Privacy & Permissions (In-App Runtime Dialogs) ─────
            SettingsSectionHeader("PRIVACY & PERMISSIONS", R.drawable.info_black, R.drawable.info_white)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    PermissionRow(
                        title = "Camera Permission",
                        description = "Used for capturing your profile picture",
                        granted = cameraGranted,
                        onClick = {
                            if (cameraGranted) {
                                Toast.makeText(context, "Camera permission is already granted.", Toast.LENGTH_SHORT).show()
                            } else {
                                cameraLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                    )
                    PermissionRow(
                        title = "Location Permission",
                        description = "Used to show nearby accommodation and campus events",
                        granted = locationGranted,
                        onClick = {
                            if (locationGranted) {
                                Toast.makeText(context, "Location permission is already granted.", Toast.LENGTH_SHORT).show()
                            } else {
                                locationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    )
                                )
                            }
                        },
                    )
                    PermissionRow(
                        title = "Phone Notifications",
                        description = "Show PEERS alerts in your phone's notification tray",
                        granted = notifGranted,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= 33 && !notifGranted) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else if (notifGranted) {
                                PeersNotificationHelper.showSystemNotification(
                                    context = context,
                                    id = 1002,
                                    title = "PEERS Notification Check",
                                    body = "Phone notifications are working properly.",
                                )
                                Toast.makeText(context, "Sent test notification to your phone!", Toast.LENGTH_SHORT).show()
                            } else {
                                showAppSettingsPrompt = "Notifications"
                            }
                        },
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = PeersPrimaryIndigo,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "PEERS respects student privacy. Your personal photos and account data are not shared with third-party applications without your permission.",
                                fontFamily = Lato,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 5: Support & Community ────────────────────────────────
            SettingsSectionHeader("SUPPORT & COMMUNITY", R.drawable.handshake_black, R.drawable.handshake_white)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    ActionRow(
                        lightIcon = R.drawable.invite_black,
                        darkIcon = R.drawable.invite_white,
                        title = "Invite Friends to PEERS",
                        subtitle = "Share the community with your classmates",
                        onClick = onOpenInvite,
                    )
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    ActionRow(
                        lightIcon = R.drawable.rate_black,
                        darkIcon = R.drawable.rate_white,
                        title = "Rate us on Google Play",
                        subtitle = "Help other students find PEERS",
                        onClick = onOpenRateUs,
                    )
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    ActionRow(
                        lightIcon = R.drawable.info_black,
                        darkIcon = R.drawable.info_white,
                        title = "Contact Support",
                        subtitle = "Reach out to support@peersapp.com",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@peersapp.com")
                                putExtra(Intent.EXTRA_SUBJECT, "PEERS App Student Support")
                            }
                            runCatching { context.startActivity(intent) }
                        },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Footer ────────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            ) {
                Text(
                    "PEERS 1.0 (Build 1)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Student Community Platform • Jetpack Compose & Firebase",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteDialog = false },
            title = { Text("Delete PEERS Account?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "This permanently removes your profile, preferences, and authentication account from our database. This cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    deleteError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeletingAccount,
                    onClick = {
                        isDeletingAccount = true
                        deleteError = null
                        onDeleteAccount { err ->
                            isDeletingAccount = false
                            deleteError = err
                        }
                    },
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Delete Forever", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(enabled = !isDeletingAccount, onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    showAppSettingsPrompt?.let { permName ->
        AlertDialog(
            onDismissRequest = { showAppSettingsPrompt = null },
            title = { Text("$permName Permission Disabled") },
            text = {
                Text("You previously selected 'Don't ask again' for $permName. Would you like to enable it from App Settings?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showAppSettingsPrompt = null
                    openAppSystemSettings(context)
                }) { Text("Open App Settings", color = PeersPrimaryIndigo) }
            },
            dismissButton = {
                TextButton(onClick = { showAppSettingsPrompt = null }) { Text("Not now") }
            },
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    @DrawableRes lightIcon: Int,
    @DrawableRes darkIcon: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
    ) {
        PeersDrawableIcon(
            lightRes = lightIcon,
            darkRes = darkIcon,
            contentDescription = title,
            size = 16.dp,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontFamily = GoogleSans,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    @DrawableRes lightIcon: Int,
    @DrawableRes darkIcon: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        PeersDrawableIcon(
            lightRes = lightIcon,
            darkRes = darkIcon,
            contentDescription = title,
            size = 20.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) PeersPrimaryIndigo else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = PeersPrimaryIndigo),
        )
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PeersPrimaryIndigo),
        )
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = if (granted) PeersMint.copy(alpha = 0.45f) else PeersPrimaryIndigo.copy(alpha = 0.14f),
        ) {
            Text(
                text = if (granted) "Granted" else "Allow",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (granted) Color(0xFF1B6B2E) else PeersPrimaryIndigo,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun ActionRow(
    @DrawableRes lightIcon: Int,
    @DrawableRes darkIcon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        PeersDrawableIcon(
            lightRes = lightIcon,
            darkRes = darkIcon,
            contentDescription = title,
            size = 22.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
    }
}

private fun openAppSystemSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    runCatching { context.startActivity(intent) }
}
