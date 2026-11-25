package com.example.peers.features.assistant

import android.util.Log
import com.example.peers.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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

    private val candidateModels = listOf(
        "gemini-3.5-flash",
        "gemini-3.8-flash",
        "gemini-3.7-flash",
        "gemini-flash-latest",
        "gemini-3.1-flash-lite",
    )

    private fun callGeminiRest(modelName: String, contextBlock: String): String? {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent")
        val payload = JSONObject().apply {
            put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            )
            put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", contextBlock)))
                )
            )
        }.toString()

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("x-goog-api-key", geminiKey)
        }
        return try {
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                Log.w("AssistantRepository", "Model $modelName HTTP $code: $body")
                return null
            }
            val json = JSONObject(body)
            val candidates = json.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val parts = candidates.getJSONObject(0)
                .optJSONObject("content")
                ?.optJSONArray("parts")
                ?: return null
            buildString {
                for (i in 0 until parts.length()) {
                    val text = parts.optJSONObject(i)?.optString("text").orEmpty()
                    if (text.isNotBlank()) append(text)
                }
            }.trim().ifBlank { null }
        } finally {
            conn.disconnect()
        }
    }

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
                    val text = callGeminiRest(modelName, contextBlock)
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
