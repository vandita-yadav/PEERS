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

/** The signed-in user, mirrored from Firestore `users/{uid}`. */
data class DashboardUser(
    val uid: String,
    val name: String,
    val email: String,
    val university: String,
    val course: String,
    val year: String,
    val city: String,
    val currentState: String = "",
    val photoUrl: String?,
    val interests: List<String>,
    val needs: List<String>,
    val countryOfOrigin: String = "",
    val currentCountry: String = "",
    val degreeLevel: String = "",
    val commencingMonth: String = "",
    val commencingYear: String = "",
    val degreeDurationYears: Int = 0,
    val expectedGraduationYear: String = "",
    val phoneCountryCode: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val languagesKnown: List<String> = emptyList(),
    val avatarIndex: Any? = null,
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
    val actionData: String = "",
    val resolvedState: String = "",
) {
    val isRoommateRequest: Boolean
        get() = type == NotificationType.ROOMMATE_REQUEST

    val isAccommodationEnquiry: Boolean
        get() = type == NotificationType.ACCOMMODATION_ENQUIRY
}

enum class NotificationType {
    ROOMMATE_REQUEST,
    ROOMMATE_ACCEPTED,
    REQUEST_SENT,
    ACCOMMODATION_ENQUIRY,
    INFO,
}

object DashboardMockData {
    val today: LocalDate = LocalDate.now()

    val events: List<DashboardEvent> = listOf(
        DashboardEvent(
            "e1", "Freshers' Meetup", "PEERS Community", today.plusDays(2),
            LocalTime.of(17, 0), "Student Center, LPU", 84, "", PeersLavender,
        ),
        DashboardEvent(
            "e2", "IELTS Speaking Practice", "Language Club", today.plusDays(3),
            LocalTime.of(11, 30), "Library Hall B", 32, "", PeersSky,
        ),
        DashboardEvent(
            "e3", "Budget Cooking Workshop", "Desi Tadka Society", today.plusDays(5),
            LocalTime.of(18, 30), "Common Kitchen, Block C", 57, "", PeersMint,
        ),
        DashboardEvent(
            "e4", "Airport Pickup Pooling", "PEERS Community", today.plusDays(6),
            LocalTime.of(9, 0), "Terminal 3 Meetup", 21, "", PeersLavender,
        ),
        DashboardEvent(
            "e5", "Society Fair Walkthrough", "Students' Union", today.plusDays(8),
            LocalTime.of(14, 0), "Main Quad", 140, "", PeersSky,
        ),
    )

    val notifications: List<PeerNotification> = run {
        val now = System.currentTimeMillis()
        fun ago(minutes: Long) = now - minutes * 60_000
        listOf(
            PeerNotification("n1", "Event near you", "Freshers' Cultural Induction Night starts tomorrow at Mittal Auditorium.", "", ago(60), true, NotificationType.INFO),
            PeerNotification("n2", "Campus Discussion", "New answers posted in PG & Rooms for Law Gate stays.", "", ago(60 * 5), false, NotificationType.INFO),
        )
    }
}
