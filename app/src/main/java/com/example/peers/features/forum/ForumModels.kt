package com.example.peers.features.forum

import java.util.concurrent.TimeUnit

enum class ForumCommunityType(val label: String) {
    TOPIC("Topic"),
}

data class ForumCommunity(
    val id: String,
    val name: String,
    val type: ForumCommunityType,
    val emoji: String,
    val description: String,
    val memberCount: Int,
)

data class ForumPost(
    val id: String,
    val title: String,
    val body: String,
    val authorUid: String,
    val authorName: String,
    val authorEmoji: String,
    val communityId: String,
    val communityName: String,
    val university: String = "",
    val upvoteCount: Int,
    val commentCount: Int,
    val createdAt: Long,
    val upvotedByMe: Boolean = false,
)

data class ForumComment(
    val id: String,
    val postId: String,
    val authorUid: String,
    val authorName: String,
    val authorEmoji: String,
    val body: String,
    val createdAt: Long,
    val upvoteCount: Int = 0,
    val downvoteCount: Int = 0,
    val myVote: Int = 0, // 1 = upvoted, -1 = downvoted, 0 = neutral
    val replyToId: String = "",
    val replyToAuthorName: String = "",
    val replyToSnippet: String = "",
    val mediaUrl: String = "",
)

enum class ReportReason(val label: String) {
    SPAM("Spam or promotion"),
    HARASSMENT("Harassment or hate"),
    MISINFORMATION("Misinformation"),
    OTHER("Something else"),
}

sealed interface ForumUiEvent {
    data class Message(val text: String) : ForumUiEvent
}

object ForumDemo {
    /** College-independent discussion categories inside every student's own campus forum. */
    val communities: List<ForumCommunity> = listOf(
        ForumCommunity("general", "General", ForumCommunityType.TOPIC, "", "Campus questions & peer advice", 640),
        ForumCommunity("events", "Events", ForumCommunityType.TOPIC, "", "Cultural fests, DSW events & workshops", 520),
        ForumCommunity("placements", "Placements", ForumCommunityType.TOPIC, "", "Internships, mock interviews & prep", 480),
        ForumCommunity("pg_rooms", "PG & Rooms", ForumCommunityType.TOPIC, "", "Hostels, off-campus PGs & flatmates", 590),
        ForumCommunity("academics", "Academics", ForumCommunityType.TOPIC, "", "Subjects, labs, exams & electives", 410),
        ForumCommunity("campus_life", "Campus Life", ForumCommunityType.TOPIC, "", "Food spots, clubs & transport", 530),
    )

    fun posts(university: String = "Lovely Professional University", now: Long = System.currentTimeMillis()): List<ForumPost> {
        val uni = university.ifBlank { "Your Campus" }
        fun ago(minutes: Long) = now - TimeUnit.MINUTES.toMillis(minutes)
        return listOf(
            ForumPost(
                id = "demo-post-1",
                title = "PG rooms & apartments worth visiting near campus?",
                body = "I am shortlisting PGs around Law Gate, Green Valley, and Meheru at $uni. How is Sardar Apartments or Five 50 for inverter backup and Wi-Fi?",
                authorUid = "demo-aisha",
                authorName = "Aisha Verma",
                authorEmoji = "",
                communityId = "pg_rooms",
                communityName = "PG & Rooms",
                university = uni,
                upvoteCount = 23,
                commentCount = 2,
                createdAt = ago(42),
            ),
            ForumPost(
                id = "demo-post-2",
                title = "Placement prep & internship drive schedule this semester",
                body = "Seniors at $uni, which DSA topics and resume templates helped you clear Round 1 technical interviews last month?",
                authorUid = "demo-rohan",
                authorName = "Rohan Mehta",
                authorEmoji = "",
                communityId = "placements",
                communityName = "Placements",
                university = uni,
                upvoteCount = 41,
                commentCount = 1,
                createdAt = ago(180),
            ),
            ForumPost(
                id = "demo-post-3",
                title = "DSW cultural inductions & club registrations at Block 13",
                body = "Does anyone know the audition timings for the Division of Arts and Culture (DSW) events at Mittal Auditorium this week?",
                authorUid = "demo-sana",
                authorName = "Sana Kapoor",
                authorEmoji = "",
                communityId = "events",
                communityName = "Events",
                university = uni,
                upvoteCount = 17,
                commentCount = 0,
                createdAt = ago(60 * 9),
            ),
            ForumPost(
                id = "demo-post-4",
                title = "Best quick study spots & affordable lunch near academic blocks",
                body = "Sharing what worked for me at $uni: Unimall quiet tables before 11 AM and meal combos near Block 34.",
                authorUid = "demo-meera",
                authorName = "Meera Nair",
                authorEmoji = "",
                communityId = "campus_life",
                communityName = "Campus Life",
                university = uni,
                upvoteCount = 67,
                commentCount = 1,
                createdAt = ago(60 * 26),
            ),
        )
    }

    fun comments(postId: String, now: Long = System.currentTimeMillis()): List<ForumComment> {
        fun ago(minutes: Long) = now - TimeUnit.MINUTES.toMillis(minutes)
        return when (postId) {
            "demo-post-1" -> listOf(
                ForumComment(
                    "demo-comment-1", postId, "demo-rohan", "Rohan Mehta", "",
                    "Sardar Apartments and Green Valley are both solid — check individual meter rates before locking in.",
                    ago(30),
                ),
                ForumComment(
                    "demo-comment-2", postId, "demo-sana", "Sana Kapoor", "",
                    "Divjot PG at Simar Enclave also includes inverter backup and RO water. Worth visiting in person.",
                    ago(12),
                ),
            )
            "demo-post-2" -> listOf(
                ForumComment(
                    "demo-comment-3", postId, "demo-meera", "Meera Nair", "",
                    "Focus on arrays, trees, SQL joins, and one clean full-stack or Android project you can explain end-to-end.",
                    ago(60 * 2),
                ),
            )
            "demo-post-4" -> listOf(
                ForumComment(
                    "demo-comment-4", postId, "demo-aisha", "Aisha Verma", "",
                    "Seconding Unimall morning hours — power sockets on the upper deck are super reliable.",
                    ago(60 * 20),
                ),
            )
            else -> emptyList()
        }
    }
}
