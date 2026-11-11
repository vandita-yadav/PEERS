package com.example.peers.features.dashboard

import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersSky
import java.time.LocalDate
import java.time.LocalTime

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
) {
    /** Weighted affinity score (per Production-Plan Phase 2 rule set). */
    fun scoreFor(me: DashboardUser): Int {
        var score = 0
        if (university == me.university) score += 30
        if (course == me.course) score += 20
        if (year == me.year) score += 10
        if (city == me.city) score += 10
        score += 5 * (interests intersect me.interests).size
        score += 5 * (needs intersect me.needs).size
        score += 15 * mutualConnections
        return score
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
)

/** A lightweight in-app notification entry. */
data class PeerNotification(
    val id: String,
    val title: String,
    val body: String,
    val emoji: String,
    val timestampLabel: String,
    val isUnread: Boolean,
)

object DashboardMockData {
    val today: LocalDate = LocalDate.now()

    val events: List<DashboardEvent> = listOf(
        DashboardEvent(
            "e1", "Freshers' Meetup 🎉", "PEERS Community", today.plusDays(2),
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

    val notifications: List<PeerNotification> = listOf(
        PeerNotification("n1", "Connection accepted", "Aisha Verma accepted your connection request.", "🤝", "Just now", true),
        PeerNotification("n2", "New connection request", "Rohan Mehta wants to connect with you.", "👋", "10 min ago", true),
        PeerNotification("n3", "Event near you", "Freshers' Meetup starts in 2 days — 84 peers going.", "🎉", "Yesterday", false),
        PeerNotification("n4", "Suggestion", "Meera Nair shares 3 interests with you.", "💡", "Yesterday", false),
    )
}
