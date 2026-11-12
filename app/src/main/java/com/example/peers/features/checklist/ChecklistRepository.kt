package com.example.peers.features.checklist

import android.content.Context
import com.example.peers.features.dashboard.DashboardUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChecklistRepository(context: Context) {

    private val dao = ChecklistDatabase.get(context).checklistDao()

    fun observeItems(category: ChecklistCategory): Flow<List<ChecklistItem>> {
        return if (category == ChecklistCategory.ALL) {
            dao.observeAll().map { list -> list.map { it.toModel() } }
        } else {
            dao.observeByCategory(category.name).map { list -> list.map { it.toModel() } }
        }
    }

    suspend fun ensureInitialized(user: DashboardUser?) {
        val count = dao.getCount()
        if (count == 0) {
            val defaults = ChecklistGenerator.generateDefaults(user)
            dao.insertAll(defaults.map { ChecklistEntity.fromModel(it) })
        }
    }

    suspend fun toggleCompleted(item: ChecklistItem) {
        dao.toggleCompleted(item.id, !item.isCompleted)
    }

    suspend fun addItem(item: ChecklistItem) {
        dao.insert(ChecklistEntity.fromModel(item))
    }

    suspend fun deleteItem(item: ChecklistItem) {
        dao.deleteById(item.id)
    }

    suspend fun regenerate(user: DashboardUser?) {
        dao.clearAll()
        val defaults = ChecklistGenerator.generateDefaults(user)
        dao.insertAll(defaults.map { ChecklistEntity.fromModel(it) })
    }
}
