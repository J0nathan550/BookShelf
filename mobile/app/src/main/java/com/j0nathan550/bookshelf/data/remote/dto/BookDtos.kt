package com.j0nathan550.bookshelf.data.remote.dto

data class BookDto(
    val id: Int,
    val title: String,
    val author: String,
    val genre: GenreDto?,
    val format: FormatDto?,
    val pages: Int?,
    val coverImageUrl: String?,
    val dateAdded: String,
    val isApproved: Boolean = true,
    val readingStatus: ReadingStatusDto?,
    val averageRating: Double? = null,
    val lendingRecord: LendingRecordDto?,
    val notes: List<BookNoteDto> = emptyList(),
    val submittedByName: String? = null,
    val lendingHistory: List<LendingRecordDto> = emptyList(),
)

data class CreateBookRequest(
    val title: String,
    val author: String,
    val genreId: Int,
    val formatId: Int,
    val pages: Int?,
    val coverImageUrl: String?,
)

data class UpdateBookRequest(
    val title: String,
    val author: String,
    val genreId: Int?,
    val formatId: Int?,
    val pages: Int?,
    val coverImageUrl: String?,
)

data class ReadingStatusDto(
    val status: String,
    val rating: Int?,
    val completionDate: String?,
)

data class UpdateReadingStatusRequest(
    val status: String,
    val rating: Int?,
    val completionDate: String?,
)

data class LendingRecordDto(
    val id: Int,
    val lenderUserId: String = "",
    val lenderName: String? = null,
    val borrowerName: String,
    val lendingDate: String,
    val returnDate: String?,
    val isReturned: Boolean,
)

data class LendBookRequest(
    val borrowerName: String,
    val lendingDate: String,
)

data class ReturnBookRequest(
    val returnDate: String,
)

data class BookNoteDto(
    val id: Int,
    val noteText: String,
    val createdDate: String,
    val modifiedDate: String?,
    val authorName: String? = null,
)

data class CreateNoteRequest(val noteText: String)

data class UpdateNoteRequest(val noteText: String)

data class IsbnLookupDto(
    val title: String,
    val author: String,
    val pages: Int?,
    val coverImageUrl: String?,
)

data class CoverUploadResponse(val coverImageUrl: String)

data class AdminDashboardDto(
    val totalUsers: Int,
    val totalBooks: Int,
    val pendingBooksCount: Int,
    val recentRegistrations: List<AdminUserDto> = emptyList(),
)

data class AdminUserDto(
    val id: String,
    val email: String,
    val fullName: String,
    val registrationDate: String,
    val bookCount: Int,
    val isActive: Boolean,
)
