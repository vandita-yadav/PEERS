package com.example.peers.features.budget

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_months WHERE monthKey = :monthKey")
    fun observeMonth(monthKey: String): Flow<BudgetMonthEntity?>

    @Query("SELECT * FROM budget_months WHERE monthKey = :monthKey")
    suspend fun getMonth(monthKey: String): BudgetMonthEntity?

    @Transaction
    @Query("SELECT * FROM budget_months WHERE monthKey = :monthKey")
    fun observeMonthWithExpenses(monthKey: String): Flow<MonthWithExpenses?>

    @Query("SELECT * FROM budget_months ORDER BY monthKey DESC")
    fun observeMonths(): Flow<List<BudgetMonthEntity>>

    @Query("SELECT * FROM expenses WHERE monthKey = :monthKey ORDER BY createdAt DESC, id DESC")
    fun observeExpenses(monthKey: String): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT category, estimated, SUM(amount) AS total FROM expenses " +
            "WHERE monthKey = :monthKey GROUP BY category, estimated"
    )
    fun observeCategoryTotals(monthKey: String): Flow<List<CategoryTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMonth(month: BudgetMonthEntity)

    @Query("UPDATE budget_months SET monthlyBudget = :budget, savingsGoal = :savingsGoal, " +
        "updatedAt = :updatedAt WHERE monthKey = :monthKey")
    suspend fun updateBudget(monthKey: String, budget: Int, savingsGoal: Int, updatedAt: Long)

    @Insert
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)

    @Query("DELETE FROM expenses WHERE monthKey = :monthKey")
    suspend fun clearMonth(monthKey: String)

    @Query("SELECT COUNT(*) FROM expenses WHERE monthKey = :monthKey")
    suspend fun expenseCount(monthKey: String): Int
}
