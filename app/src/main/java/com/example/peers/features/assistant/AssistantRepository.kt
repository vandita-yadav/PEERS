package com.example.peers.features.assistant

import android.util.Log
import com.example.peers.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssistantRepository {

    private val geminiKey: String = BuildConfig.GEMINI_API_KEY.trim()

    private val systemPrompt = """
        You are PEERS AI Guide, the friendly in-app student advisor inside the PEERS Android app.
        Scope strictly to PEERS app features and campus student life covered by PEERS:
        - Nearby Accommodation (PGs, flats, verification, inspections)
        - Rentals & Transport (bike/scooty/cycle rentals, campus shuttles, airport cab pooling, upcoming Student Exchange)
        - Campus Events & Meetups
        - Community Discussions (asking questions and connecting in campus/city forums)
        - Student Checklist (documents, visa prep, banking, health, campus arrival)
        - Budget Planner (monthly budget, rent, food, travel tracking)

        Style rules:
        - Speak in a warm, natural, conversational tone like a helpful senior student (2 to 4 natural sentences).
        - Avoid long 10-bullet walls of text or stiff templates unless the user explicitly asks for a list.
        - Do not use emojis.
        - Mention the exact PEERS screen when helpful (for example: Rentals & Transport, Accommodation, Budget Planner, Student Checklist, Events, or Discussion Forum).
        - If asked about topics completely unrelated to student life or PEERS, politely explain that you focus on PEERS student resources and offer help with campus housing, transport, events, or budgeting.
    """.trimIndent()

    private val candidateModels = listOf("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash")

    private fun buildModel(modelName: String): GenerativeModel = GenerativeModel(
        modelName = modelName,
        apiKey = geminiKey,
        systemInstruction = content { text(systemPrompt) }
    )

    suspend fun getResponse(
        userMessage: String,
        recentHistory: List<ChatMessage> = emptyList(),
    ): Pair<String, AssistantAction?> = withContext(Dispatchers.IO) {
        if (geminiKey.isNotBlank()) {
            val contextBlock = buildString {
                val relevant = recentHistory.takeLast(4)
                if (relevant.isNotEmpty()) {
                    appendLine("Recent conversation:")
                    relevant.forEach { msg ->
                        appendLine("${if (msg.isUser) "Student" else "Guide"}: ${msg.text}")
                    }
                    appendLine()
                }
                append("Student: $userMessage")
            }

            for (modelName in candidateModels) {
                try {
                    val response = buildModel(modelName).generateContent(contextBlock)
                    val text = response.text?.trim()
                    if (!text.isNullOrBlank()) {
                        return@withContext (text to inferAction(text, userMessage))
                    }
                } catch (e: Exception) {
                    Log.w("AssistantRepository", "Model $modelName failed: ${e.message}")
                }
            }
        }

        // Conversational PEERS fallback when no GEMINI_API_KEY is configured or network is offline
        StudyAbroadKnowledgeBase.generateResponse(userMessage)
    }

    private fun inferAction(reply: String, query: String): AssistantAction? {
        val combined = "$reply $query"
        return when {
            combined.contains("Rental", ignoreCase = true) ||
                combined.contains("Scooty", ignoreCase = true) ||
                combined.contains("Shuttle", ignoreCase = true) ||
                combined.contains("Transport", ignoreCase = true) ->
                AssistantAction("Open Rentals & Transport", AssistantActionType.OPEN_RENTALS)
            combined.contains("Checklist", ignoreCase = true) ||
                combined.contains("Visa", ignoreCase = true) ->
                AssistantAction("View Student Checklist", AssistantActionType.OPEN_CHECKLIST)
            combined.contains("Budget", ignoreCase = true) ->
                AssistantAction("Open Budget Planner", AssistantActionType.OPEN_BUDGET)
            combined.contains("Accommodation", ignoreCase = true) ||
                combined.contains("Housing", ignoreCase = true) ->
                AssistantAction("Explore Accommodation", AssistantActionType.OPEN_ACCOMMODATION)
            combined.contains("Event", ignoreCase = true) ->
                AssistantAction("Browse Events", AssistantActionType.OPEN_EVENTS)
            combined.contains("Discussion", ignoreCase = true) ||
                combined.contains("Forum", ignoreCase = true) ->
                AssistantAction("Join Discussions", AssistantActionType.OPEN_COMMUNITY)
            else -> null
        }
    }

    fun isGeminiKeyConfigured(): Boolean = geminiKey.isNotBlank()
}
