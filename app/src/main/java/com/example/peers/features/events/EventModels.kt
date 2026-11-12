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
            title = "Freshers' Meetup",
            description = "Meet students from across campuses, swap stories and find your people. Snacks and ice-breakers included.",
            organizerName = "PEERS Community",
            organizerType = EventOrganizerType.COMMUNITY,
            venue = "Student Center",
            city = "Phagwara",
            startMillis = at(2, 17, 0),
            emoji = "🎉",
            capacity = 120,
            attendeeCount = 84,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
        CommunityEvent(
            id = "demo-event-2",
            title = "IELTS Speaking Practice",
            description = "A relaxed speaking circle for IELTS aspirants. Bring a notebook and one topic you want to practise.",
            organizerName = "Language Club",
            organizerType = EventOrganizerType.COMMUNITY,
            venue = "Library Hall B",
            city = "Phagwara",
            startMillis = at(3, 11, 30),
            emoji = "🎙️",
            capacity = 40,
            attendeeCount = 32,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
        CommunityEvent(
            id = "demo-event-3",
            title = "Budget Cooking Workshop",
            description = "Cook a week of budget-friendly meals in one hour. Recipes and a shopping list go home with you.",
            organizerName = "Desi Tadka Society",
            organizerType = EventOrganizerType.STUDENT,
            venue = "Common Kitchen, Block C",
            city = "Phagwara",
            startMillis = at(5, 18, 30),
            emoji = "🍳",
            capacity = 30,
            attendeeCount = 22,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
        CommunityEvent(
            id = "demo-event-4",
            title = "Airport Pickup Pooling",
            description = "Flying in around the same time? Share a cab, split the fare and arrive together.",
            organizerName = "PEERS Community",
            organizerType = EventOrganizerType.STUDENT,
            venue = "Terminal 3 Meetup",
            city = "Delhi",
            startMillis = at(6, 9, 0),
            emoji = "🛬",
            capacity = 20,
            attendeeCount = 11,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
        CommunityEvent(
            id = "demo-event-5",
            title = "Society Fair Walkthrough",
            description = "Explore every student society on campus in one afternoon and sign up for the ones that fit you.",
            organizerName = "Students' Union",
            organizerType = EventOrganizerType.UNIVERSITY,
            venue = "Main Quad",
            city = "Phagwara",
            startMillis = at(8, 14, 0),
            emoji = "🎪",
            capacity = 300,
            attendeeCount = 140,
            createdBy = "peers",
            createdAt = System.currentTimeMillis(),
        ),
    )
}
