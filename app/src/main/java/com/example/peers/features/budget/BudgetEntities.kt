package com.example.peers.features.budget

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** One planned month. `monthKey` is an ISO year-month such as `2026-11`. */
@Entity(tableName = "budget_months")
data class BudgetMonthEntity(
    @PrimaryKey val monthKey: String,
    val monthlyBudget: Int = 0,
    val savingsGoal: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)

/** A single expense line for a month. Estimated lines are planned, not paid yet. */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = BudgetMonthEntity::class,
            parentColumns = ["monthKey"],
            childColumns = ["monthKey"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("monthKey"), Index(value = ["monthKey", "category"])],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthKey: String,
    val title: String,
    val category: String,
    val amount: Int,
    val estimated: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

/** Month plus its expenses, fetched in one transaction. */
data class MonthWithExpenses(
    @Embedded val month: BudgetMonthEntity,
    @Relation(parentColumn = "monthKey", entityColumn = "monthKey")
    val expenses: List<ExpenseEntity>,
)

/** SQL projection used for the category breakdown chart. */
data class CategoryTotal(
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "estimated") val estimated: Boolean,
    @ColumnInfo(name = "total") val total: Int,
)

data class CategoryBreakdown(
    val category: BudgetCategory,
    val estimated: Int,
    val actual: Int,
)

enum class BudgetCategory(val label: String, val emoji: String) {
    RENT("Rent", "🏠"),
    FOOD("Food", "🍲"),
    TRANSPORT("Transport", "🚌"),
    UTILITIES("Utilities", "💡"),
    BOOKS("Books", "📚"),
    FUN("Fun", "🎉"),
    HEALTH("Health", "🩺"),
    OTHER("Other", "✨");

    companion object {
        fun fromLabel(label: String): BudgetCategory =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: OTHER
    }
}
