package com.j0nathan550.bookshelf.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.j0nathan550.bookshelf.data.local.dao.PendingOperationDao
import com.j0nathan550.bookshelf.data.local.entity.OperationType
import com.j0nathan550.bookshelf.data.remote.ApiService
import com.j0nathan550.bookshelf.data.remote.dto.CreateNoteRequest
import com.j0nathan550.bookshelf.data.remote.dto.UpdateNoteRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OfflineSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: ApiService,
    private val pendingOps: PendingOperationDao,
    private val gson: Gson,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pending = pendingOps.getAllPending()
        for (op in pending) {
            try {
                val success = when (op.operationType) {
                    OperationType.UPDATE_BOOK -> {
                        val p = gson.fromJson(op.payload, UpdateBookPayload::class.java)
                        api.updateBook(p.bookId, p.request).isSuccessful
                    }
                    OperationType.DELETE_BOOK -> {
                        val p = gson.fromJson(op.payload, DeleteBookPayload::class.java)
                        val res = api.deleteBook(p.bookId)
                        res.isSuccessful || res.code() == 404
                    }
                    OperationType.UPDATE_READING_STATUS -> {
                        val p = gson.fromJson(op.payload, UpdateReadingStatusPayload::class.java)
                        api.updateReadingStatus(p.bookId, p.request).isSuccessful
                    }
                    OperationType.ADD_NOTE -> {
                        val p = gson.fromJson(op.payload, AddNotePayload::class.java)
                        api.addNote(p.bookId, CreateNoteRequest(p.text)).isSuccessful
                    }
                    OperationType.UPDATE_NOTE -> {
                        val p = gson.fromJson(op.payload, UpdateNotePayload::class.java)
                        api.updateNote(p.noteId, UpdateNoteRequest(p.text)).isSuccessful
                    }
                    OperationType.DELETE_NOTE -> {
                        val p = gson.fromJson(op.payload, DeleteNotePayload::class.java)
                        val res = api.deleteNote(p.noteId)
                        res.isSuccessful || res.code() == 404
                    }
                    else -> true
                }
                if (success) pendingOps.remove(op) else return Result.retry()
            } catch (_: Exception) {
                return Result.retry()
            }
        }
        return Result.success()
    }
}
