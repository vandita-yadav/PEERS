package com.example.peers.features.events

import java.time.LocalDate
import java.time.LocalTime

enum class EventOrganizerType(val label: String) {
    STUDENT("Student"),
    UNIVERSITY("University"),
    COMMUNITY("Community"),
}

data class CommunityEvent(
    val id: String,
    val title: String,
    val description: String,
    val organizerName: String,
    val organizerType: EventOrganizerType,
    val venue: String,
    val city: String,
    val startMillis: Long,
    val emoji: String,
    val capacity: Int,
    val attendeeCount: Int,
    val createdBy: String,
    val createdAt: Long,
) {
    val date: LocalDate
        get() = java.time.Instant.ofEpochMilli(startMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()

    val time: LocalTime
        get() = java.time.Instant.ofEpochMilli(startMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalTime()
}

data class EventAttendee(
    val uid: String,
    val name: String,
    val emoji: String,
)

/** One-off UI side effects emitted through a SharedFlow. */
sealed interface EventUiEvent {
    data class Message(val text: String) : EventUiEvent
}

val genuineLpuVenues = listOf(
    "Shanti Devi Mittal Auditorium (Block 35)",
    "Baldev Raj Mittal UniPolis",
    "Amphitheatre, LPU",
    "Indoor Sports Complex & Playgrounds",
    "LPU Unimall",
    "Block 13 (DSW & Arts & Culture)",
    "Block 25 · Room 101",
    "Block 26 · Room 201",
    "Block 27 · Room 303",
    "Block 28 · Room 404",
    "Block 33 · Room 303",
    "Block 34 · Room 201",
    "Block 36 · Room 607",
    "Block 38 · Room 404",
)

/** Seeded events keep the Events tab and dashboard useful before the first sync. */
fun demoEvents(today: LocalDate = LocalDate.now()): List<CommunityEvent> {
    fun at(dayOffset: Long, hour: Int, minute: Int): Long =
        today.plusDays(dayOffset).atTime(hour, minute)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    return listOf(
        CommunityEvent(
            id = "demo-event-1",
            title = "Freshers' Cultural Induction Night",
            description = "Cultural programs, inductions, and talks welcoming new students. Hosted indoors with seating for 2,000–3,000+.",
            organizerName = "Division of Arts and Culture (DSW)",
            organizerType = EventOrganizerType.UNIVERSITY,
            venue = "Shanti Devi Mittal Auditorium (Block 35)",
            city = "Phagwara",
            startMillis = at(1, 17, 0),
            emoji = "",
            capacity = 2500,
            attendeeCount = 412,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
        CommunityEvent(
            id = "demo-event-2",
            title = "UniPolis Mega Concert & Student Expo",
            description = "Open-air student fest featuring live bands, club stalls, and project exhibitions under the UniPolis dome.",
            organizerName = "Student Welfare Wing (DSW)",
            organizerType = EventOrganizerType.UNIVERSITY,
            venue = "Baldev Raj Mittal UniPolis",
            city = "Phagwara",
            startMillis = at(2, 18, 0),
            emoji = "",
            capacity = 3000,
            attendeeCount = 680,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
        CommunityEvent(
            id = "demo-event-3",
            title = "Open Mic & Acoustic Evening",
            description = "Performances, poetry, and tiered open-air campus gatherings. Register via Block 13 DSW Arts desk.",
            organizerName = "Division of Arts and Culture (DSW)",
            organizerType = EventOrganizerType.STUDENT,
            venue = "Amphitheatre, LPU (Tiered Open-Air)",
            city = "Phagwara",
            startMillis = at(3, 17, 30),
            emoji = "",
            capacity = 400,
            attendeeCount = 135,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
        CommunityEvent(
            id = "demo-event-4",
            title = "Full-Stack Sprint & IELTS Workshop",
            description = "Peer-led coding session in Block 34 Room 201 and parallel speaking circle in Block 36 Room 607.",
            organizerName = "PEERS Tech & Language Club",
            organizerType = EventOrganizerType.STUDENT,
            venue = "Block 34 · Room 201 & Block 36 · Room 607",
            city = "Phagwara",
            startMillis = at(4, 15, 0),
            emoji = "",
            capacity = 80,
            attendeeCount = 46,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
        CommunityEvent(
            id = "demo-event-5",
            title = "Inter-Hostel Badminton & Basketball Trials",
            description = "Open trials and pickup matches for hostel teams across indoor courts and grounds.",
            organizerName = "Sports & Wellness Society",
            organizerType = EventOrganizerType.COMMUNITY,
            venue = "Indoor Sports Complex & Playgrounds",
            city = "Phagwara",
            startMillis = at(5, 16, 30),
            emoji = "",
            capacity = 200,
            attendeeCount = 92,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
        CommunityEvent(
            id = "demo-event-6",
            title = "Student Clubs & Arts Auditions Showcase",
            description = "Meet cultural, theatre, music, and student welfare coordinators at Block 13 & Unimall plaza.",
            organizerName = "DSW & LPU Unimall Desk",
            organizerType = EventOrganizerType.UNIVERSITY,
            venue = "Block 13 (DSW) & LPU Unimall",
            city = "Phagwara",
            startMillis = at(6, 11, 30),
            emoji = "",
            capacity = 350,
            attendeeCount = 178,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
    )
}
