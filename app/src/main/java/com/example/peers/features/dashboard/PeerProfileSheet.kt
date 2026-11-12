package com.example.peers.features.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.community.ConnectionState
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky

private val peerEmojis = listOf("🧑🏿", "👩🏿", "👦🏽", "👧🏼", "👩🏽", "🧔🏽", "🧕🏾")
private val peerPastels = listOf(PeersLavender, PeersSky, PeersMint, Color(0xFFFDE4D0))

/**
 * Rich peer preview: identity, match score ring and explainable "why you match"
 * signals, plus the stateful Connect action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeerProfileSheet(
    peer: SuggestedPeer,
    me: DashboardUser,
    connectionState: ConnectionState,
    onConnect: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reasons = peer.matchReasons(me)
    val score = reasons.sumOf { it.points }
    // Normalise into a friendly percentage (max realistic score ≈ 120).
    val matchPercent = matchPercent(score)
    val animatedPercent by animateFloatAsState(
        targetValue = matchPercent / 100f,
        animationSpec = tween(900),
        label = "matchRing",
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            // ── Avatar with pastel halo ─────────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    peerPastels[peer.avatarIndex.mod(peerPastels.size)],
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        )
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(peerPastels[peer.avatarIndex.mod(peerPastels.size)]),
                ) {
                    Text(
                        peerEmojis[peer.avatarIndex.mod(peerEmojis.size)],
                        fontSize = 42.sp,
                    )
                }
            }

            Text(
                peer.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                "${peer.course} · ${peer.year}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "🎓 ${peer.university}   📍 ${peer.city}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )

            // ── Match ring ──────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val trackColor = MaterialTheme.colorScheme.outlineVariant
                    Canvas(Modifier.size(74.dp)) {
                        val stroke = 7.dp.toPx()
                        val inset = stroke / 2
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                        drawArc(
                            color = PeersPrimaryIndigo,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedPercent,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$matchPercent%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "match",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(Modifier.padding(start = 18.dp)) {
                    Text(
                        "Why you match",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        reasons.take(2).joinToString(" · ") { it.label },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }

            // ── All match signals ──────────────────────────────────────────
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            ) {
                reasons.take(6).forEach { reason ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outlineVariant,
                        ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text(reason.emoji, fontSize = 12.sp)
                            Text(
                                " ${reason.label}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                "  +${reason.points}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PeersPrimaryIndigo,
                            )
                        }
                    }
                }
            }

            // ── Primary action ─────────────────────────────────────────────
            Spacer(Modifier.height(22.dp))
            ConnectActionButton(
                state = connectionState,
                onConnect = onConnect,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                when (connectionState) {
                    ConnectionState.NONE -> "Send a request — they'll be notified instantly."
                    ConnectionState.SENDING -> "Sending your request…"
                    ConnectionState.PENDING -> "Waiting for them to accept. We'll notify you."
                    ConnectionState.CONNECTED -> "You're connected! Start a conversation soon."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

/** Morphing button: Connect → Sending → Request Sent → Connected. */
@Composable
fun ConnectActionButton(
    state: ConnectionState,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Button(
        onClick = onConnect,
        enabled = state == ConnectionState.NONE,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier.height(if (compact) 44.dp else 52.dp),
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "connectState",
        ) { current ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (current) {
                    ConnectionState.NONE -> {
                        Icon(Icons.Rounded.PersonAdd, null, Modifier.size(if (compact) 18.dp else 20.dp))
                        Text(
                            "  Connect",
                            color = PeersPrimaryIndigo,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 13.sp else 15.sp,
                        )
                    }
                    ConnectionState.SENDING -> {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = PeersPrimaryIndigo,
                            modifier = Modifier.size(if (compact) 16.dp else 18.dp),
                        )
                        Text(
                            "  Sending…",
                            color = PeersPrimaryIndigo,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (compact) 13.sp else 15.sp,
                        )
                    }
                    ConnectionState.PENDING -> {
                        Icon(
                            Icons.Rounded.HourglassTop,
                            null,
                            Modifier.size(if (compact) 16.dp else 18.dp),
                            tint = PeersPrimaryIndigo,
                        )
                        Text(
                            "  Request Sent",
                            color = PeersPrimaryIndigo,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (compact) 13.sp else 15.sp,
                        )
                    }
                    ConnectionState.CONNECTED -> {
                        Icon(
                            Icons.Rounded.Check,
                            null,
                            Modifier.size(if (compact) 16.dp else 18.dp),
                            tint = Color(0xFF1F8A4C),
                        )
                        Text(
                            "  Connected",
                            color = Color(0xFF1F8A4C),
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 13.sp else 15.sp,
                        )
                    }
                }
            }
        }
    }
}
