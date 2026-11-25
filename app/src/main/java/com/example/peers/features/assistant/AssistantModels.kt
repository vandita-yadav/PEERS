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
    OPEN_COMMUNITY,
    OPEN_RENTALS
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
)

val defaultPromptSuggestions = listOf(
    PromptSuggestion(
        id = "rentals_transport",
        label = "Local Rentals & Transport",
        prompt = "What are the best budget bike, scooty, or cycle rental options and cab/shuttle tips for students on PEERS?",
        category = "Transport",
    ),
    PromptSuggestion(
        id = "lease_tips",
        label = "Lease & Housing Check",
        prompt = "What should a student inspect and verify before booking a PG or flat on PEERS?",
        category = "Accommodation",
    ),
    PromptSuggestion(
        id = "budget_plan",
        label = "Monthly Student Budget",
        prompt = "How should I plan my monthly rent, food, and transit expenses using the PEERS Budget Planner?",
        category = "Budget",
    ),
    PromptSuggestion(
        id = "campus_events",
        label = "Campus Events & Clubs",
        prompt = "How can I find upcoming workshops, meetups, and student activities inside PEERS?",
        category = "Events",
    ),
    PromptSuggestion(
        id = "visa_checklist",
        label = "Arrival & Visa Checklist",
        prompt = "What key documents and tasks should I finish in my first week using the Student Checklist?",
        category = "Checklist",
    ),
    PromptSuggestion(
        id = "forum_discussion",
        label = "Peer Discussions",
        prompt = "How can I ask questions and get advice from seniors in the PEERS Discussion Forum?",
        category = "Community",
    ),
)
