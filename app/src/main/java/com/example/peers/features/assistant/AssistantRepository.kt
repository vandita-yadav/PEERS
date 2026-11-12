package com.example.peers.features.assistant

import android.util.Log
import com.example.peers.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssistantRepository {

    private val geminiKey: String = BuildConfig.GEMINI_API_KEY.trim()

    private val generativeModel: GenerativeModel? by lazy {
        if (geminiKey.isNotBlank()) {
            GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiKey,
                systemInstruction = com.google.ai.client.generativeai.type.content {
                    text(
                        """
                        You are PEERS AI Guide, a warm, knowledgeable, and empathetic study-abroad student advisor.
                        You specialize in helping international students navigate university life, visa regulations, budgeting, housing, finding student jobs, campus culture, and adjusting to life abroad.
                        Always provide concise, structured, bulleted, actionable guidance. Mention relevant PEERS features (such as the Student Checklist, Budget Planner, or Accommodation Finder) when applicable. Keep your tone encouraging and supportive.
                        """.trimIndent()
                    )
                }
            )
        } else {
            null
        }
    }

    suspend fun getResponse(userMessage: String): Pair<String, AssistantAction?> = withContext(Dispatchers.IO) {
        val model = generativeModel
        if (model != null) {
            try {
                val response = model.generateContent(userMessage)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    // Check if response suggests any action
                    val action = when {
                        text.contains("Checklist", ignoreCase = true) -> AssistantAction("View Student Checklist", AssistantActionType.OPEN_CHECKLIST)
                        text.contains("Budget", ignoreCase = true) -> AssistantAction("Open Budget Planner", AssistantActionType.OPEN_BUDGET)
                        text.contains("Accommodation", ignoreCase = true) || text.contains("housing", ignoreCase = true) -> AssistantAction("Explore Accommodation", AssistantActionType.OPEN_ACCOMMODATION)
                        else -> null
                    }
                    return@withContext (text to action)
                }
            } catch (e: Exception) {
                Log.w("AssistantRepository", "Gemini API failed or quota reached, falling back to Knowledge Base: ${e.message}")
            }
        }

        // Offline / Spark / Free tier fallback engine
        StudyAbroadKnowledgeBase.generateResponse(userMessage)
    }

    fun isGeminiKeyConfigured(): Boolean = geminiKey.isNotBlank()
}
