package com.example.peers.features.assistant

import java.util.UUID

enum class MessageStatus {
    SENDING,
    SENT,
    ERROR
}

enum class AssistantActionType {
    OPEN_CHECKLIST,
    OPEN_BUDGET,
    OPEN_ACCOMMODATION,
    OPEN_EVENTS,
    OPEN_COMMUNITY
}

data class AssistantAction(
    val label: String,
    val type: AssistantActionType
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val action: AssistantAction? = null
)

data class PromptSuggestion(
    val id: String,
    val label: String,
    val prompt: String,
    val category: String,
    val emoji: String
)

val defaultPromptSuggestions = listOf(
    PromptSuggestion(
        id = "visa_hours",
        label = "Part-Time Work Limits",
        prompt = "What are the legal work hour limits for international students on student visas (like F-1 or UK Student Visa)?",
        category = "Visa & Legal",
        emoji = "🛂"
    ),
    PromptSuggestion(
        id = "bank_account",
        label = "Open Bank Account",
        prompt = "How do I open a student bank account abroad without proof of local credit history?",
        category = "Finances",
        emoji = "💰"
    ),
    PromptSuggestion(
        id = "lease_tips",
        label = "Lease & Flat Inspection",
        prompt = "What should an international student inspect and verify before signing a rental lease agreement?",
        category = "Housing",
        emoji = "🏠"
    ),
    PromptSuggestion(
        id = "campus_jobs",
        label = "Finding On-Campus Jobs",
        prompt = "What are the most effective ways to find and apply for on-campus student assistant jobs?",
        category = "Career",
        emoji = "🎓"
    ),
    PromptSuggestion(
        id = "health_insurance",
        label = "Health Insurance & Doctors",
        prompt = "How does student health insurance work abroad, and how do I register with a local doctor or clinic?",
        category = "Health",
        emoji = "🏥"
    ),
    PromptSuggestion(
        id = "packing_essentials",
        label = "Packing Essentials",
        prompt = "What essential items and documents should international students pack before their flight?",
        category = "Arrival",
        emoji = "✈️"
    )
)
