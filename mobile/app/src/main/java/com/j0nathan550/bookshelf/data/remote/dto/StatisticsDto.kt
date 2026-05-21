package com.j0nathan550.bookshelf.data.remote.dto

data class StatisticsDto(
    val totalBooks: Int,
    val wantToRead: Int,
    val currentlyReading: Int,
    val finished: Int,
    val booksReadThisYear: Int,
    val genreDistribution: Map<String, Int> = emptyMap(),
)
