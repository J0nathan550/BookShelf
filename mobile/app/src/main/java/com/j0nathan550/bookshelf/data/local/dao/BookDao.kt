package com.j0nathan550.bookshelf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.j0nathan550.bookshelf.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBook(id: Int): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: Int)

    @Query("DELETE FROM books")
    suspend fun clearAll()

    @Query("SELECT * FROM books WHERE isLent = 1")
    fun getLentBooks(): Flow<List<BookEntity>>

    @Query(
        "SELECT * FROM books WHERE " +
            "lower(title) LIKE '%' || lower(:query) || '%' OR " +
            "lower(author) LIKE '%' || lower(:query) || '%'",
    )
    fun searchBooks(query: String): Flow<List<BookEntity>>
}
