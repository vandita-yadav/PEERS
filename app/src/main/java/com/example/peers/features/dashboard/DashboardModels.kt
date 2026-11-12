package com.example.peers.features.dashboard

import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersSky
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.min

/** Normalises a raw affinity score into a friendly match percentage. */
fun matchPercent(score: Int): Int = min(99, score * 100 / 120).coerceAtLeast(25)

/** A campus/community event shown in the "Top Events" rail. */
data class DashboardEvent(
    val id: String,
    val title: String,
    val organizer: String,
    val date: LocalDate,
    val time: LocalTime,
    val location: String,
    val attendees: Int,
    val emoji: String,
    val accent: androidx.compose.ui.graphics.Color,
)

/** A single explainable signal behind a match score. */
data class MatchReason(val emoji: String, val label: String, val points: Int)

/** A suggested peer for the "People You May Know" rail. */
data class SuggestedPeer(
    val uid: String,
    val name: String,
    val university: String,
    val course: String,
    val year: String,
    val city: String,
    val avatarIndex: Int,
    val interests: List<String>,
    val needs: List<String>,
    val mutualConnections: Int,
    val isDemoPeer: Boolean = true,
) {
    /** Weighted affinity score (per Production-Plan Phase 2 rule set). */
    fun scoreFor(me: DashboardUser): Int = matchReasons(me).sumOf { it.points }

    /** Human-readable "why you match" signals, strongest first. */
    fun matchReasons(me: DashboardUser): List<MatchReason> {
        val reasons = mutableListOf<MatchReason>()
        if (university == me.university) reasons += MatchReason("🎓", "Same university", 30)
        if (course == me.course) reasons += MatchReason("📚", "Same course", 20)
        if (year == me.year) reasons += MatchReason("📅", "Same year", 10)
        if (city == me.city) reasons += MatchReason("📍", "Same city", 10)
        (interests intersect me.interests).forEach {
            reasons += MatchReason("✨", "Shares “$it”", 5)
        }
        (needs intersect me.needs).forEach {
            reasons += MatchReason("🤝", "Also needs “$it”", 5)
        }
        if (mutualConnections > 0) {
            reasons += MatchReason(
                "👥",
                if (mutualConnections == 1) "1 mutual connection"
                else "$mutualConnections mutual connections",
                15 * mutualConnections,
            )
        }
        return reasons
    }

    private infix fun <T> List<T>.intersect(other: List<T>): List<T> =
        this.filter(other::contains)
}

/** The signed-in user, mirrored from Firestore `users/{uid}`. */
data class DashboardUser(
    val uid: String,
    val name: String,
    val email: String,
    val university: String,
    val course: String,
    val year: String,
    val city: String,
    val photoUrl: String?,
    val interests: List<String>,
    val needs: List<String>,
    val countryOfOrigin: String = "",
    val currentCountry: String = "",
    val degreeLevel: String = "",
    val expectedGraduationYear: String = "",
    val bio: String = "",
    val languagesKnown: List<String> = emptyList(),
    val avatarIndex: Int? = null,
)

/** A lightweight in-app notification entry. */
data class PeerNotification(
    val id: String,
    val title: String,
    val body: String,
    val emoji: String,
    val timestampMillis: Long,
    val isUnread: Boolean,
    val type: NotificationType = NotificationType.INFO,
) {
    val isConnectionRequest: Boolean
        get() = type == NotificationType.CONNECTION_REQUEST
}

enum class NotificationType { CONNECTION_REQUEST, CONNECTION_ACCEPTED, REQUEST_SENT, INFO }

object DashboardMockData {
    val today: LocalDate = LocalDate.now()

    val events: List<DashboardEvent> = listOf(
        DashboardEvent(
            "e1", "Freshers' Meetup", "PEERS Community", today.plusDays(2),
            LocalTime.of(17, 0), "Student Center, LPU", 84, "🎉", PeersLavender,
        ),
        DashboardEvent(
            "e2", "IELTS Speaking Practice", "Language Club", today.plusDays(3),
            LocalTime.of(11, 30), "Library Hall B", 32, "🎙️", PeersSky,
        ),
        DashboardEvent(
            "e3", "Budget Cooking Workshop", "Desi Tadka Society", today.plusDays(5),
            LocalTime.of(18, 30), "Common Kitchen, Block C", 57, "🍳", PeersMint,
        ),
        DashboardEvent(
            "e4", "Airport Pickup Pooling", "PEERS Community", today.plusDays(6),
            LocalTime.of(9, 0), "Terminal 3 Meetup", 21, "🛬", PeersLavender,
        ),
        DashboardEvent(
            "e5", "Society Fair Walkthrough", "Students' Union", today.plusDays(8),
            LocalTime.of(14, 0), "Main Quad", 140, "🎪", PeersSky,
        ),
    )

    val suggestedPeers: List<SuggestedPeer> = listOf(
        SuggestedPeer(
            "p1", "Aisha Verma", "Lovely Professional University", "Computer Science",
            "2nd Year", "Phagwara", 1,
            listOf("Academics & Study", "Technology", "Music"), listOf("Make Friends"), 2,
        ),
        SuggestedPeer(
            "p2", "Rohan Mehta", "Lovely Professional University", "Mechanical Engineering",
            "1st Year", "Phagwara", 2,
            listOf("Music", "Vlogging", "Technology"), listOf("Accommodation", "Make Friends"), 1,
        ),
        SuggestedPeer(
            "p3", "Sana Kapoor", "Delhi University", "Economics",
            "3rd Year", "New Delhi", 3,
            listOf("Content Writing", "Academics & Study"), listOf("Jobs & Internships"), 0,
        ),
        SuggestedPeer(
            "p4", "Dev Patel", "VIT", "Computer Science",
            "2nd Year", "Vellore", 2,
            listOf("Technology", "Entrepreneurship"), listOf("Make Friends"), 1,
        ),
        SuggestedPeer(
            "p5", "Meera Nair", "Lovely Professional University", "Computer Science",
            "2nd Year", "Phagwara", 3,
            listOf("Academics & Study", "Dance", "Music"), listOf("Make Friends", "University Help"), 3,
        ),
    )

    val notifications: List<PeerNotification> = run {
        val now = System.currentTimeMillis()
        fun ago(minutes: Long) = now - minutes * 60_000
        listOf(
            PeerNotification("n1", "Connection accepted", "Aisha Verma accepted your connection request.", "🤝", ago(1), true, NotificationType.CONNECTION_ACCEPTED),
            PeerNotification("n2", "New connection request", "Rohan Mehta wants to connect with you.", "👋", ago(10), true, NotificationType.CONNECTION_REQUEST),
            PeerNotification("n3", "Event near you", "Freshers' Meetup starts in 2 days — 84 peers going.", "🎉", ago(60 * 26), false, NotificationType.INFO),
            PeerNotification("n4", "Suggestion", "Meera Nair shares 3 interests with you.", "💡", ago(60 * 29), false, NotificationType.INFO),
        )
    }
}
