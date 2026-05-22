package com.j0nathan550.bookshelf.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.j0nathan550.bookshelf.data.local.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingOperationEntity>

    @Insert
    suspend fun enqueue(op: PendingOperationEntity)

    @Delete
    suspend fun remove(op: PendingOperationEntity)

    @Query("SELECT COUNT(*) FROM pending_operations")
    fun pendingCount(): Flow<Int>
}
