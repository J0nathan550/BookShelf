package com.j0nathan550.bookshelf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis(),
)

object OperationType {
    const val UPDATE_BOOK = "UPDATE_BOOK"
    const val DELETE_BOOK = "DELETE_BOOK"
    const val UPDATE_READING_STATUS = "UPDATE_READING_STATUS"
    const val ADD_NOTE = "ADD_NOTE"
    const val UPDATE_NOTE = "UPDATE_NOTE"
    const val DELETE_NOTE = "DELETE_NOTE"
}
