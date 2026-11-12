package com.example.peers.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky

/**
 * Student's full public/personal profile tab.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    user: DashboardUser?,
    unreadCount: Int,
    connectionCount: Int,
    savedEventsCount: Int,
    onMenuClick: () -> Unit,
    onBellClick: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        // ── Hero Header Banner ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(PeersPrimaryIndigo, PeersLavender.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp, bottom = 26.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Top Action Bar inside banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                ) {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                        )
                    }
                    Text(
                        "My Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onBellClick) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error)
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                            )
                        }
                    }
                }
                // Avatar with white glow border
                PeersAvatarBadge(
                    avatarIndex = user?.avatarIndex,
                    photoUrl = user?.photoUrl,
                    size = 96.dp,
                    borderWidth = 3.dp,
                    borderColor = Color.White.copy(alpha = 0.9f),
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    user?.name ?: "Student Peer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                Text(
                    user?.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp),
                )

                if (!user?.university.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(top = 10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Text("🎓", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                user?.university.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    }
                }

                if (!user?.city.isNullOrBlank() || !user?.currentCountry.isNullOrBlank()) {
                    val locationText = listOfNotNull(
                        user?.city?.takeIf { it.isNotBlank() },
                        user?.currentCountry?.takeIf { it.isNotBlank() }
                    ).joinToString(", ")
                    Text(
                        "📍 $locationText",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }

        // ── Action Buttons Row ────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Button(
                onClick = onEditProfile,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit Profile", fontWeight = FontWeight.Bold, color = Color.White)
            }

            FilledTonalButton(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Settings", fontWeight = FontWeight.SemiBold)
            }
        }

        // ── Stats Row ─────────────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            ) {
                ProfileStatItem(
                    count = "$connectionCount",
                    label = "Connections",
                    emoji = "👥",
                )
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                ProfileStatItem(
                    count = "$savedEventsCount",
                    label = "Saved Events",
                    emoji = "📅",
                )
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                ProfileStatItem(
                    count = "${user?.interests?.size ?: 0}",
                    label = "Interests",
                    emoji = "✨",
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── About Me Card ─────────────────────────────────────────────────────
        ProfileSectionCard(title = "About Me", emoji = "📝") {
            if (user?.bio.isNullOrBlank()) {
                Text(
                    "Introduce yourself to campus peers! Share where you're from, your passions, and what you're working on.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            } else {
                Text(
                    user?.bio.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Academic Journey Card ─────────────────────────────────────────────
        ProfileSectionCard(title = "Academic Journey", emoji = "🎓") {
            AcademicRow("University", user?.university?.ifBlank { "Lovely Professional University" } ?: "Not specified")
            AcademicRow("Course", user?.course?.ifBlank { "Computer Science" } ?: "Not specified")
            if (!user?.degreeLevel.isNullOrBlank()) {
                AcademicRow("Degree Level", user?.degreeLevel.orEmpty())
            }
            if (!user?.year.isNullOrBlank()) {
                AcademicRow("Year of Study", user?.year.orEmpty())
            }
            if (!user?.expectedGraduationYear.isNullOrBlank()) {
                AcademicRow("Expected Graduation", user?.expectedGraduationYear.orEmpty())
            }
            if (!user?.countryOfOrigin.isNullOrBlank()) {
                AcademicRow("Country of Origin", user?.countryOfOrigin.orEmpty())
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Interests & Passions ───────────────────────────────────────────────
        ProfileSectionCard(title = "Interests & Passions", emoji = "💡") {
            val interests = user?.interests.orEmpty()
            if (interests.isEmpty()) {
                Text(
                    "No interests selected yet. Pick at least 3 to match with like-minded students.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    interests.forEachIndexed { index, interest ->
                        val pastels = listOf(PeersLavender, PeersSky, PeersMint)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = pastels[index % pastels.size].copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, pastels[index % pastels.size]),
                        ) {
                            Text(
                                "✨ $interest",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── What I'm Looking For (Needs) ──────────────────────────────────────
        ProfileSectionCard(title = "What I'm Looking For", emoji = "🤝") {
            val needs = user?.needs.orEmpty()
            if (needs.isEmpty()) {
                Text(
                    "Tell peers what support or connections you need.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    needs.forEach { need ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Text(
                                "🎯 $need",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Languages Known ───────────────────────────────────────────────────
        if (!user?.languagesKnown.isNullOrEmpty()) {
            ProfileSectionCard(title = "Languages Known", emoji = "🗣️") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    user?.languagesKnown.orEmpty().forEach { lang ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        ) {
                            Text(
                                "🌐 $lang",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileStatItem(count: String, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PeersPrimaryIndigo,
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    emoji: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Text(emoji, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@Composable
private fun AcademicRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
