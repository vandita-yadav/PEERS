package com.example.peers.features.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.features.profile.PeersAvatarBadge
import com.example.peers.ui.theme.PeersBrandInk
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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    BackHandler { onBack() }

    // Local notifications preference cache
    val prefs = remember { context.getSharedPreferences("peers_settings", Context.MODE_PRIVATE) }
    var masterNotifs by remember { mutableStateOf(prefs.getBoolean("notif_master", true)) }
    var connNotifs by remember { mutableStateOf(prefs.getBoolean("notif_conn", true)) }
    var roomNotifs by remember { mutableStateOf(prefs.getBoolean("notif_room", true)) }
    var eventNotifs by remember { mutableStateOf(prefs.getBoolean("notif_events", true)) }
    var forumNotifs by remember { mutableStateOf(prefs.getBoolean("notif_forum", true)) }

    fun updateNotif(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
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
            SettingsSectionHeader("APPEARANCE", "🎨")
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Theme Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Choose your preferred look across all screens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
                    )

                    ThemeOptionRow(
                        title = "Light Theme",
                        subtitle = "Soft warm off-white (#FAF8F4)",
                        icon = "☀️",
                        selected = themeMode == PeersThemeMode.AestheticLight,
                        onClick = { onSetTheme(PeersThemeMode.AestheticLight) },
                    )
                    ThemeOptionRow(
                        title = "Dark Theme",
                        subtitle = "Modern neon-slate contrast (#0C0A18)",
                        icon = "🌙",
                        selected = themeMode == PeersThemeMode.NeonDark,
                        onClick = { onSetTheme(PeersThemeMode.NeonDark) },
                    )
                    ThemeOptionRow(
                        title = "System Default",
                        subtitle = "Follows your Android device appearance",
                        icon = "⚙️",
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
            SettingsSectionHeader("NOTIFICATIONS", "🔔")
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
            SettingsSectionHeader("ACCOUNT", "👤")
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
                        PeersAvatarBadge(
                            avatarIndex = user?.avatarIndex,
                            photoUrl = user?.photoUrl,
                            size = 54.dp,
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                user?.name ?: "Peer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                user?.email ?: "",
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
                                    "✓ Google Account Verified",
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
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 4: Privacy & Permissions ──────────────────────────────
            SettingsSectionHeader("PRIVACY & PERMISSIONS", "🛡️")
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
                        onClick = { openAppSystemSettings(context) },
                    )
                    PermissionRow(
                        title = "Location Permission",
                        description = "Used to show nearby accommodation and campus events",
                        onClick = { openAppSystemSettings(context) },
                    )
                    PermissionRow(
                        title = "App Notifications",
                        description = "Device level notification permissions",
                        onClick = { openAppSystemSettings(context) },
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
                            Text("🔒", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "PEERS respects student privacy. We only read your name, email, and photo from your Google account. Your personal data is never shared with third-party advertisers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 5: Support & Community ────────────────────────────────
            SettingsSectionHeader("SUPPORT & COMMUNITY", "🤝")
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    ActionRow(
                        emoji = "💌",
                        title = "Invite Friends to PEERS",
                        subtitle = "Share the community with your classmates",
                        onClick = onOpenInvite,
                    )
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    ActionRow(
                        emoji = "⭐",
                        title = "Rate us on Google Play",
                        subtitle = "Help other students find PEERS",
                        onClick = onOpenRateUs,
                    )
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    ActionRow(
                        emoji = "📧",
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
}

@Composable
private fun SettingsSectionHeader(title: String, emoji: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
    ) {
        Text(emoji, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            title,
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
    icon: String,
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
        Text(icon, fontSize = 20.sp)
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
        Icon(
            Icons.Rounded.OpenInNew,
            contentDescription = "Open Settings",
            tint = PeersPrimaryIndigo,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ActionRow(
    emoji: String,
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
        Text(emoji, fontSize = 22.sp)
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
