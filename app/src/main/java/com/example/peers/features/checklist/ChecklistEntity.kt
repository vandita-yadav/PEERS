package com.example.peers.features.checklist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_checklist")
data class ChecklistEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val isCompleted: Boolean,
    val isCustom: Boolean,
    val createdAt: Long,
) {
    fun toModel(): ChecklistItem = ChecklistItem(
        id = id,
        title = title,
        description = description,
        category = runCatching { ChecklistCategory.valueOf(category) }.getOrDefault(ChecklistCategory.CUSTOM),
        priority = runCatching { ChecklistPriority.valueOf(priority) }.getOrDefault(ChecklistPriority.MEDIUM),
        isCompleted = isCompleted,
        isCustom = isCustom,
        createdAt = createdAt,
    )

    companion object {
        fun fromModel(model: ChecklistItem): ChecklistEntity = ChecklistEntity(
            id = model.id,
            title = model.title,
            description = model.description,
            category = model.category.name,
            priority = model.priority.name,
            isCompleted = model.isCompleted,
            isCustom = model.isCustom,
            createdAt = model.createdAt,
        )
    }
}
