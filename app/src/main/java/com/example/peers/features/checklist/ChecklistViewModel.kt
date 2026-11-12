package com.example.peers.features.checklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.peers.features.dashboard.DashboardUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChecklistUiStats(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val urgentCount: Int = 0,
    val progressPercent: Int = 0,
)

class ChecklistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChecklistRepository(application)

    private val _selectedCategory = MutableStateFlow(ChecklistCategory.ALL)
    val selectedCategory: StateFlow<ChecklistCategory> = _selectedCategory.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<ChecklistItem>> = _selectedCategory
        .flatMapLatest { category -> repository.observeItems(category) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allItems: StateFlow<List<ChecklistItem>> = repository.observeItems(ChecklistCategory.ALL)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun initialize(user: DashboardUser?) {
        viewModelScope.launch {
            repository.ensureInitialized(user)
        }
    }

    fun setCategory(category: ChecklistCategory) {
        _selectedCategory.value = category
    }

    fun toggleTask(item: ChecklistItem) {
        viewModelScope.launch {
            repository.toggleCompleted(item)
            val action = if (!item.isCompleted) "Completed" else "Unchecked"
            _message.value = "$action: ${item.title}"
        }
    }

    fun addCustomTask(title: String, description: String, category: ChecklistCategory, priority: ChecklistPriority) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val item = ChecklistItem(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                isCustom = true,
            )
            repository.addItem(item)
            _message.value = "Added task: ${item.title}"
        }
    }

    fun deleteTask(item: ChecklistItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _message.value = "Removed task: ${item.title}"
        }
    }

    fun regenerate(user: DashboardUser?) {
        viewModelScope.launch {
            repository.regenerate(user)
            _message.value = "Regenerated personalized checklist."
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
