package com.j0nathan550.bookshelf.data.sync

import com.j0nathan550.bookshelf.data.remote.dto.UpdateBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.UpdateReadingStatusRequest

data class UpdateBookPayload(val bookId: Int, val request: UpdateBookRequest)
data class DeleteBookPayload(val bookId: Int)
data class UpdateReadingStatusPayload(val bookId: Int, val request: UpdateReadingStatusRequest)
data class AddNotePayload(val bookId: Int, val text: String)
data class UpdateNotePayload(val noteId: Int, val text: String)
data class DeleteNotePayload(val noteId: Int)
