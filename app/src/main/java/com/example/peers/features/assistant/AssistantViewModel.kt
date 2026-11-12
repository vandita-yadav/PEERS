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
        text = "Hello! 👋 I'm your PEERS AI Guide.\n\nI can help answer your questions about student visas, finding housing, opening local bank accounts, managing monthly budgets, or applying for on-campus student jobs.\n\nAsk me anything or tap one of the quick suggestions below!",
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

        val userMessage = ChatMessage(
            text = cleanText,
            isUser = true,
            status = MessageStatus.SENT
        )

        _messages.value = _messages.value + userMessage
        _isThinking.value = true

        viewModelScope.launch {
            try {
                val (replyText, action) = repository.getResponse(cleanText)
                val aiMessage = ChatMessage(
                    text = replyText,
                    isUser = false,
                    status = MessageStatus.SENT,
                    action = action
                )
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = "I ran into an issue connecting to the network. Please check your connection or try asking again.",
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
