package com.j0nathan550.bookshelf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.j0nathan550.bookshelf.data.remote.dto.BookNoteDto

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val author: String,
    val genreId: Int?,
    val genreName: String?,
    val formatId: Int?,
    val formatName: String?,
    val pages: Int?,
    val coverImageUrl: String?,
    val dateAdded: String,
    // ReadingStatus fields inlined
    val readingStatus: String?,
    val rating: Int?,
    val completionDate: String?,
    // LendingRecord fields inlined
    val isLent: Boolean,
    val lendingRecordId: Int?,
    val borrowerName: String?,
    val lendingDate: String?,
    val returnDate: String?,
    val isReturned: Boolean,
    // Notes serialized as JSON
    val notesJson: List<BookNoteDto>,
)
