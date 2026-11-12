package com.example.peers.features.checklist

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    @Query("SELECT * FROM student_checklist ORDER BY isCompleted ASC, createdAt DESC")
    fun observeAll(): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM student_checklist WHERE category = :category ORDER BY isCompleted ASC, createdAt DESC")
    fun observeByCategory(category: String): Flow<List<ChecklistEntity>>

    @Query("SELECT COUNT(*) FROM student_checklist")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ChecklistEntity>)

    @Update
    suspend fun update(item: ChecklistEntity)

    @Query("UPDATE student_checklist SET isCompleted = :completed WHERE id = :id")
    suspend fun toggleCompleted(id: String, completed: Boolean)

    @Delete
    suspend fun delete(item: ChecklistEntity)

    @Query("DELETE FROM student_checklist WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM student_checklist")
    suspend fun clearAll()
}
