package com.example.peers.features.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssistantViewModel(
    private val repository: AssistantRepository = AssistantRepository()
) : ViewModel() {

    private val initialGreeting = ChatMessage(
        text = "Hi! I'm your PEERS AI Guide. Ask me anything about campus housing, local bike or shuttle rentals, budgeting, arrival checklists, upcoming events, or student discussions—I'll point you right to what you need.",
        isUser = false
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(initialGreeting))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    val suggestions: List<PromptSuggestion> = defaultPromptSuggestions

    val isGeminiLive: Boolean = repository.isGeminiKeyConfigured()

    fun sendMessage(userText: String) {
        val cleanText = userText.trim()
        if (cleanText.isBlank() || _isThinking.value) return

        val historySnapshot = _messages.value
        val userMessage = ChatMessage(
            text = cleanText,
            isUser = true,
            status = MessageStatus.SENT
        )

        _messages.value = _messages.value + userMessage
        _isThinking.value = true

        viewModelScope.launch {
            try {
                val (replyText, action) = repository.getResponse(cleanText, historySnapshot)
                val aiMessage = ChatMessage(
                    text = replyText,
                    isUser = false,
                    status = MessageStatus.SENT,
                    action = action
                )
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = "I ran into a connection snag while replying. Please try asking that again.",
                    isUser = false,
                    status = MessageStatus.ERROR
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isThinking.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(initialGreeting)
    }
}
