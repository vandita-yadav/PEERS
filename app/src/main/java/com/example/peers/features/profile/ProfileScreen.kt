package com.example.peers.features.profile

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.R
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersDrawableIcon
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky

/**
 * Student's full personal profile screen (opened from Header avatar or Side Menu).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    user: DashboardUser?,
    unreadCount: Int,
    savedEventsCount: Int,
    onBack: () -> Unit,
    onBellClick: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onBack() }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                    Text(
                        "My Profile",
                        fontFamily = GoogleSans,
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
                            PeersDrawableIcon(
                                lightRes = R.drawable.notification_white,
                                darkRes = R.drawable.notification_white,
                                contentDescription = "Notifications",
                                size = 22.dp,
                            )
                        }
                    }
                }

                // Avatar / Cropped Photo with white glow border
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
                    fontFamily = GoogleSans,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                Text(
                    user?.email ?: "",
                    fontFamily = Lato,
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
                        Text(
                            user?.university.orEmpty(),
                            fontFamily = GoogleSans,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        )
                    }
                }

                if (!user?.city.isNullOrBlank() || !user?.currentCountry.isNullOrBlank()) {
                    val locationText = listOfNotNull(
                        user?.city?.takeIf { it.isNotBlank() },
                        user?.currentCountry?.takeIf { it.isNotBlank() }
                    ).joinToString(", ")
                    Text(
                        locationText,
                        fontFamily = Lato,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
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
                Text("Edit Profile", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, color = Color.White)
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
                Text("Settings", fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold)
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
                    count = "$savedEventsCount",
                    label = "Saved Events",
                    lightIcon = R.drawable.events_black,
                    darkIcon = R.drawable.events_white,
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
                    lightIcon = R.drawable.stars_black,
                    darkIcon = R.drawable.stars_white,
                )
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                ProfileStatItem(
                    count = "${user?.needs?.size ?: 0}",
                    label = "Preferences",
                    lightIcon = R.drawable.checklist_black,
                    darkIcon = R.drawable.checklist_white,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── About Me Card ─────────────────────────────────────────────────────
        ProfileSectionCard(
            title = "About Me",
            lightIcon = R.drawable.notes_black,
            darkIcon = R.drawable.notes_white,
        ) {
            if (user?.bio.isNullOrBlank()) {
                Text(
                    "Introduce yourself to campus peers! Share where you're from, your passions, and what you're working on.",
                    fontFamily = Lato,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            } else {
                Text(
                    user?.bio.orEmpty(),
                    fontFamily = Lato,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Academic Journey Card ─────────────────────────────────────────────
        ProfileSectionCard(
            title = "Academic Journey",
            lightIcon = R.drawable.info_black,
            darkIcon = R.drawable.info_white,
        ) {
            AcademicRow("University", user?.university?.ifBlank { "Not specified" } ?: "Not specified")
            AcademicRow("Course", user?.course?.ifBlank { "Not specified" } ?: "Not specified")
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
        ProfileSectionCard(
            title = "Interests & Passions",
            lightIcon = R.drawable.stars_black,
            darkIcon = R.drawable.stars_white,
        ) {
            val interests = user?.interests.orEmpty()
            if (interests.isEmpty()) {
                Text(
                    "No interests selected yet. Pick at least 3 in Edit Profile.",
                    fontFamily = Lato,
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
                                interest,
                                fontFamily = GoogleSans,
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
        ProfileSectionCard(
            title = "What I Need From PEERS",
            lightIcon = R.drawable.handshake_black,
            darkIcon = R.drawable.handshake_white,
        ) {
            val needs = user?.needs.orEmpty()
            if (needs.isEmpty()) {
                Text(
                    "Select what support and PEERS features matter most to you.",
                    fontFamily = Lato,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    needs.forEach { need ->
                        val cleanLabel = need.replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u27BF]"), "").trim()
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Text(
                                cleanLabel.ifBlank { need },
                                fontFamily = Lato,
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
            ProfileSectionCard(
                title = "Languages Known",
                lightIcon = R.drawable.community_black,
                darkIcon = R.drawable.community_white,
            ) {
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
                                lang,
                                fontFamily = Lato,
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
private fun ProfileStatItem(
    count: String,
    label: String,
    @DrawableRes lightIcon: Int,
    @DrawableRes darkIcon: Int,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PeersDrawableIcon(
                lightRes = lightIcon,
                darkRes = darkIcon,
                contentDescription = label,
                size = 16.dp,
            )
            Spacer(Modifier.width(5.dp))
            Text(
                count,
                fontFamily = GoogleSans,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PeersPrimaryIndigo,
            )
        }
        Text(
            label,
            fontFamily = Lato,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    @DrawableRes lightIcon: Int,
    @DrawableRes darkIcon: Int,
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
                PeersDrawableIcon(
                    lightRes = lightIcon,
                    darkRes = darkIcon,
                    contentDescription = title,
                    size = 18.dp,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    fontFamily = GoogleSans,
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
            fontFamily = Lato,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            fontFamily = GoogleSans,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
