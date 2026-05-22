package com.j0nathan550.bookshelf.data.local.mapper

import com.j0nathan550.bookshelf.data.local.entity.BookEntity
import com.j0nathan550.bookshelf.data.remote.dto.BookDto
import com.j0nathan550.bookshelf.data.remote.dto.FormatDto
import com.j0nathan550.bookshelf.data.remote.dto.GenreDto
import com.j0nathan550.bookshelf.data.remote.dto.LendingRecordDto
import com.j0nathan550.bookshelf.data.remote.dto.ReadingStatusDto

fun BookDto.toEntity() = BookEntity(
    id = id,
    title = title,
    author = author,
    genreId = genre?.id,
    genreName = genre?.name,
    formatId = format?.id,
    formatName = format?.name,
    pages = pages,
    coverImageUrl = coverImageUrl,
    dateAdded = dateAdded,
    readingStatus = readingStatus?.status,
    rating = readingStatus?.rating,
    completionDate = readingStatus?.completionDate,
    isLent = lendingRecord != null && !lendingRecord.isReturned,
    lendingRecordId = lendingRecord?.id,
    borrowerName = lendingRecord?.borrowerName,
    lendingDate = lendingRecord?.lendingDate,
    returnDate = lendingRecord?.returnDate,
    isReturned = lendingRecord?.isReturned ?: false,
    notesJson = notes,
)

fun BookEntity.toBookDto() = BookDto(
    id = id,
    title = title,
    author = author,
    genre = if (genreId != null && genreName != null) GenreDto(genreId, genreName) else null,
    format = if (formatId != null && formatName != null) FormatDto(formatId, formatName) else null,
    pages = pages,
    coverImageUrl = coverImageUrl,
    dateAdded = dateAdded,
    readingStatus = if (readingStatus != null) ReadingStatusDto(readingStatus, rating, completionDate) else null,
    lendingRecord = if (lendingRecordId != null) LendingRecordDto(
        id = lendingRecordId,
        borrowerName = borrowerName ?: "",
        lendingDate = lendingDate ?: "",
        returnDate = returnDate,
        isReturned = isReturned,
    ) else null,
    notes = notesJson,
)
