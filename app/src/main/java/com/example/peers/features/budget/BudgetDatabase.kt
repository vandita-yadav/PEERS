package com.example.peers.features.budget

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BudgetMonthEntity::class, ExpenseEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var instance: BudgetDatabase? = null

        fun get(context: Context): BudgetDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "peers-budget.db",
                ).build().also { instance = it }
            }
    }
}
