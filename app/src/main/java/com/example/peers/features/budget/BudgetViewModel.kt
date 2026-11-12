package com.example.peers.features.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class BudgetUiState(
    val monthKey: String = "",
    val monthLabel: String = "",
    val isCurrentMonth: Boolean = true,
    val budget: Int = 0,
    val savingsGoal: Int = 0,
    val expenses: List<ExpenseEntity> = emptyList(),
    val breakdown: List<CategoryBreakdown> = emptyList(),
    val actualTotal: Int = 0,
    val estimatedTotal: Int = 0,
    val hasMonth: Boolean = false,
    val isLoading: Boolean = true,
) {
    val committed: Int get() = actualTotal + estimatedTotal
    val remaining: Int get() = (budget - committed).coerceAtLeast(0)
    val overspent: Int get() = (committed - budget).coerceAtLeast(0)
    val progress: Float
        get() = if (budget <= 0) 0f else (committed.toFloat() / budget).coerceIn(0f, 1f)
    val isEmptyMonth: Boolean get() = !hasMonth && expenses.isEmpty()
}

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BudgetRepository(BudgetDatabase.get(application).budgetDao())

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BudgetUiState> = selectedMonth
        .flatMapLatest { month ->
            combine(
                repository.observeMonth(month.toString()),
                repository.observeCategoryTotals(month.toString()),
            ) { monthWithExpenses, totals ->
                val monthEntity = monthWithExpenses?.month
                val expenses = monthWithExpenses?.expenses.orEmpty()
                BudgetUiState(
                    monthKey = month.toString(),
                    monthLabel = month.format(monthFormatter),
                    isCurrentMonth = month == YearMonth.now(),
                    budget = monthEntity?.monthlyBudget ?: 0,
                    savingsGoal = monthEntity?.savingsGoal ?: 0,
                    expenses = expenses,
                    breakdown = buildBreakdown(totals),
                    actualTotal = expenses.filterNot { it.estimated }.sumOf { it.amount },
                    estimatedTotal = expenses.filter { it.estimated }.sumOf { it.amount },
                    hasMonth = monthEntity != null,
                    isLoading = false,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BudgetUiState(
                monthKey = YearMonth.now().toString(),
                monthLabel = YearMonth.now().format(monthFormatter),
            ),
        )

    private fun buildBreakdown(totals: List<CategoryTotal>): List<CategoryBreakdown> {
        return totals
            .groupBy { BudgetCategory.fromLabel(it.category) }
            .map { (category, rows) ->
                CategoryBreakdown(
                    category = category,
                    estimated = rows.firstOrNull { it.estimated }?.total ?: 0,
                    actual = rows.firstOrNull { !it.estimated }?.total ?: 0,
                )
            }
            .filter { it.estimated > 0 || it.actual > 0 }
            .sortedByDescending { it.actual + it.estimated }
    }

    fun previousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    fun thisMonth() {
        selectedMonth.value = YearMonth.now()
    }

    fun saveBudget(monthlyBudget: Int, savingsGoal: Int) {
        val key = selectedMonth.value.toString()
        viewModelScope.launch {
            runCatching { repository.setBudget(key, monthlyBudget, savingsGoal) }
                .onSuccess { _message.value = "Monthly budget updated." }
                .onFailure { _message.value = "Couldn't save your budget. Try again." }
        }
    }

    fun loadSampleMonth() {
        val key = selectedMonth.value.toString()
        viewModelScope.launch {
            runCatching { repository.loadSampleMonth(key) }
                .onSuccess { _message.value = "Sample month loaded — edit anything you like." }
                .onFailure { _message.value = "Couldn't load the sample month." }
        }
    }

    fun addExpense(
        title: String,
        category: BudgetCategory,
        amount: Int,
        estimated: Boolean,
        note: String,
    ) {
        if (title.isBlank() || amount <= 0) return
        val key = selectedMonth.value.toString()
        viewModelScope.launch {
            runCatching {
                repository.addExpense(
                    ExpenseEntity(
                        monthKey = key,
                        title = title.trim(),
                        category = category.label,
                        amount = amount,
                        estimated = estimated,
                        note = note.trim(),
                    )
                )
            }.onSuccess {
                _message.value = if (estimated) "Planned expense added." else "Expense added."
            }.onFailure {
                _message.value = "Couldn't add that expense."
            }
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            runCatching { repository.updateExpense(expense) }
                .onSuccess { _message.value = "Expense updated." }
                .onFailure { _message.value = "Couldn't update that expense." }
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteExpense(id) }
                .onSuccess { _message.value = "Expense removed." }
                .onFailure { _message.value = "Couldn't remove that expense." }
        }
    }

    fun clearMonth() {
        val key = selectedMonth.value.toString()
        viewModelScope.launch {
            runCatching { repository.clearMonth(key) }
                .onSuccess { _message.value = "All expenses cleared for this month." }
                .onFailure { _message.value = "Couldn't clear this month." }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
