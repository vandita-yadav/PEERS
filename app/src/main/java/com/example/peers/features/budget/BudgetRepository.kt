package com.example.peers.features.budget

import kotlinx.coroutines.flow.Flow

/**
 * Local source of truth for the budget planner. Room is intentionally the only
 * backing store so the feature stays fully usable offline.
 */
class BudgetRepository(private val dao: BudgetDao) {

    fun observeMonth(monthKey: String): Flow<MonthWithExpenses?> =
        dao.observeMonthWithExpenses(monthKey)

    fun observeCategoryTotals(monthKey: String): Flow<List<CategoryTotal>> =
        dao.observeCategoryTotals(monthKey)

    fun observeMonths(): Flow<List<BudgetMonthEntity>> = dao.observeMonths()

    suspend fun setBudget(monthKey: String, monthlyBudget: Int, savingsGoal: Int) {
        val existing = dao.getMonth(monthKey)
        val budget = monthlyBudget.coerceAtLeast(0)
        val savings = savingsGoal.coerceAtLeast(0)
        if (existing == null) {
            dao.upsertMonth(
                BudgetMonthEntity(
                    monthKey = monthKey,
                    monthlyBudget = budget,
                    savingsGoal = savings,
                )
            )
        } else {
            // Update in place: REPLACE would cascade-delete this month's expenses.
            dao.updateBudget(monthKey, budget, savings, System.currentTimeMillis())
        }
    }

    suspend fun addExpense(expense: ExpenseEntity) {
        ensureMonth(expense.monthKey)
        dao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) = dao.updateExpense(expense)

    suspend fun deleteExpense(id: Long) = dao.deleteExpense(id)

    suspend fun clearMonth(monthKey: String) = dao.clearMonth(monthKey)

    /** Seeds a realistic month once, so the planner has something to show on first open. */
    suspend fun loadSampleMonth(monthKey: String) {
        ensureMonth(monthKey)
        if (dao.expenseCount(monthKey) > 0) return
        dao.updateBudget(monthKey, 14000, 1500, System.currentTimeMillis())
        val now = System.currentTimeMillis()
        dao.insertExpenses(
            listOf(
                ExpenseEntity(
                    monthKey = monthKey,
                    title = "PG rent",
                    category = BudgetCategory.RENT.label,
                    amount = 7500,
                    estimated = false,
                    note = "Monthly, paid on the 1st",
                    createdAt = now - 8 * 86_400_000,
                ),
                ExpenseEntity(
                    monthKey = monthKey,
                    title = "Mess & groceries",
                    category = BudgetCategory.FOOD.label,
                    amount = 2600,
                    estimated = false,
                    note = "Includes weekend chai runs",
                    createdAt = now - 6 * 86_400_000,
                ),
                ExpenseEntity(
                    monthKey = monthKey,
                    title = "Auto to campus",
                    category = BudgetCategory.TRANSPORT.label,
                    amount = 900,
                    estimated = false,
                    createdAt = now - 5 * 86_400_000,
                ),
                ExpenseEntity(
                    monthKey = monthKey,
                    title = "Electricity & water",
                    category = BudgetCategory.UTILITIES.label,
                    amount = 650,
                    estimated = false,
                    createdAt = now - 4 * 86_400_000,
                ),
                ExpenseEntity(
                    monthKey = monthKey,
                    title = "Lab manual printing",
                    category = BudgetCategory.BOOKS.label,
                    amount = 480,
                    estimated = false,
                    createdAt = now - 3 * 86_400_000,
                ),
                ExpenseEntity(
                    monthKey = monthKey,
                    title = "Movie night",
                    category = BudgetCategory.FUN.label,
                    amount = 700,
                    estimated = false,
                    createdAt = now - 2 * 86_400_000,
                ),
                ExpenseEntity(
                    monthKey = monthKey,
                    title = "Phone recharge",
                    category = BudgetCategory.UTILITIES.label,
                    amount = 300,
                    estimated = true,
                    note = "Planned for next week",
                    createdAt = now - 86_400_000,
                ),
                ExpenseEntity(
                    monthKey = monthKey,
                    title = "Winter jacket",
                    category = BudgetCategory.OTHER.label,
                    amount = 1500,
                    estimated = true,
                    note = "Save up before December",
                    createdAt = now - 20 * 3_600_000,
                ),
            )
        )
    }

    private suspend fun ensureMonth(monthKey: String) {
        if (dao.getMonth(monthKey) == null) {
            dao.upsertMonth(BudgetMonthEntity(monthKey = monthKey))
        }
    }
}
