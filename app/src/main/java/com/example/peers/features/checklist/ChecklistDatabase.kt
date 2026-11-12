package com.example.peers.features.checklist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChecklistEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ChecklistDatabase : RoomDatabase() {
    abstract fun checklistDao(): ChecklistDao

    companion object {
        @Volatile
        private var instance: ChecklistDatabase? = null

        fun get(context: Context): ChecklistDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChecklistDatabase::class.java,
                    "peers-checklist.db",
                ).build().also { instance = it }
            }
    }
}
