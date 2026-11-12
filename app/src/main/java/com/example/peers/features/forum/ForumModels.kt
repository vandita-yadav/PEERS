package com.example.peers.features.forum

import java.util.concurrent.TimeUnit

enum class ForumCommunityType(val label: String) {
    UNIVERSITY("University"),
    CITY("City"),
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
)

enum class ReportReason(val label: String) {
    SPAM("Spam or promotion"),
    HARASSMENT("Harassment or hate"),
    MISINFORMATION("Misinformation"),
    OTHER("Something else"),
}

/** One-off UI side effects (CO2: SharedFlow for events, not state). */
sealed interface ForumUiEvent {
    data class Message(val text: String) : ForumUiEvent
}

object ForumDemo {
    val communities: List<ForumCommunity> = listOf(
        ForumCommunity("lpu", "LPU Campus", ForumCommunityType.UNIVERSITY, "🎓", "Lovely Professional University", 1240),
        ForumCommunity("du", "DU Circle", ForumCommunityType.UNIVERSITY, "🏛️", "University of Delhi", 860),
        ForumCommunity("vit", "VIT Vellore", ForumCommunityType.UNIVERSITY, "⚙️", "VIT Vellore", 720),
        ForumCommunity("delhi", "Delhi Students", ForumCommunityType.CITY, "🌆", "New Delhi", 1100),
        ForumCommunity("mumbai", "Mumbai Students", ForumCommunityType.CITY, "🌊", "Mumbai", 980),
        ForumCommunity("bengaluru", "Bengaluru Students", ForumCommunityType.CITY, "🌿", "Bengaluru", 890),
    )

    fun posts(now: Long = System.currentTimeMillis()): List<ForumPost> {
        fun ago(minutes: Long) = now - TimeUnit.MINUTES.toMillis(minutes)
        return listOf(
            ForumPost(
                id = "demo-post-1",
                title = "Cheapest way to get from Delhi airport to Phagwara?",
                body = "Landing at T3 around 11pm next Friday. Is the overnight bus from Kashmere Gate still the best option, or should I pool a cab with other students?",
                authorUid = "demo-aisha",
                authorName = "Aisha Verma",
                authorEmoji = "👩🏽",
                communityId = "lpu",
                communityName = "LPU Campus",
                upvoteCount = 23,
                commentCount = 8,
                createdAt = ago(42),
            ),
            ForumPost(
                id = "demo-post-2",
                title = "Best places to find part-time work near campus",
                body = "I can do weekend shifts and some remote content editing. Where do students usually find flexible work that doesn't clash with classes?",
                authorUid = "demo-rohan",
                authorName = "Rohan Mehta",
                authorEmoji = "🧔🏽",
                communityId = "delhi",
                communityName = "Delhi Students",
                upvoteCount = 41,
                commentCount = 14,
                createdAt = ago(180),
            ),
            ForumPost(
                id = "demo-post-3",
                title = "Anyone else joining the October intake at VIT?",
                body = "Would love to make a small group before orientation. I'm from Jaipur, moving for the first time and slightly nervous about hostel life.",
                authorUid = "demo-sana",
                authorName = "Sana Kapoor",
                authorEmoji = "👩🏻",
                communityId = "vit",
                communityName = "VIT Vellore",
                upvoteCount = 17,
                commentCount = 6,
                createdAt = ago(60 * 9),
            ),
            ForumPost(
                id = "demo-post-4",
                title = "Budget grocery list for a week (under ₹1,200)",
                body = "Sharing the list I use every week: dal, rice, eggs, seasonal veggies, curd, oats and bananas. It covers breakfast, lunch and dinner without eating out.",
                authorUid = "demo-meera",
                authorName = "Meera Nair",
                authorEmoji = "👩🏾",
                communityId = "mumbai",
                communityName = "Mumbai Students",
                upvoteCount = 67,
                commentCount = 19,
                createdAt = ago(60 * 26),
            ),
        )
    }

    fun comments(postId: String, now: Long = System.currentTimeMillis()): List<ForumComment> {
        fun ago(minutes: Long) = now - TimeUnit.MINUTES.toMillis(minutes)
        return when (postId) {
            "demo-post-1" -> listOf(
                ForumComment(
                    "demo-comment-1", postId, "demo-rohan", "Rohan Mehta", "🧔🏽",
                    "The overnight bus is still the cheapest. Book on the state transport site a day early.",
                    ago(30),
                ),
                ForumComment(
                    "demo-comment-2", postId, "demo-sana", "Sana Kapoor", "👩🏻",
                    "Three of us are landing around the same time. We're splitting a cab — DM me!",
                    ago(12),
                ),
            )
            "demo-post-4" -> listOf(
                ForumComment(
                    "demo-comment-3", postId, "demo-aisha", "Aisha Verma", "👩🏽",
                    "Adding peanut butter and frozen peas changed my weekly budget completely.",
                    ago(60 * 20),
                ),
            )
            else -> emptyList()
        }
    }
}
