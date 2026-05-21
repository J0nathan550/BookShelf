package com.j0nathan550.bookshelf.data.repository

import com.j0nathan550.bookshelf.data.local.dao.BookDao
import com.j0nathan550.bookshelf.data.local.entity.BookEntity
import com.j0nathan550.bookshelf.data.remote.ApiService
import com.j0nathan550.bookshelf.data.remote.dto.BookDto
import com.j0nathan550.bookshelf.data.remote.dto.CreateBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.CreateNoteRequest
import com.j0nathan550.bookshelf.data.remote.dto.FormatDto
import com.j0nathan550.bookshelf.data.remote.dto.GenreDto
import com.j0nathan550.bookshelf.data.remote.dto.IsbnLookupDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import com.j0nathan550.bookshelf.data.remote.dto.LendBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.ReturnBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.StatisticsDto
import com.j0nathan550.bookshelf.data.remote.dto.UpdateBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.UpdateNoteRequest
import com.j0nathan550.bookshelf.data.remote.dto.UpdateReadingStatusRequest
import com.j0nathan550.bookshelf.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val api: ApiService,
    private val bookDao: BookDao,
) {
    val cachedBooks: Flow<List<BookEntity>> = bookDao.getAllBooks()
    val cachedLentBooks: Flow<List<BookEntity>> = bookDao.getLentBooks()

    fun searchCached(query: String): Flow<List<BookEntity>> = bookDao.searchBooks(query)

    suspend fun fetchBooks(): Resource<List<BookDto>> = try {
        val response = api.getBooks()
        if (response.isSuccessful) {
            val books = response.body()!!
            bookDao.upsertBooks(books.map { it.toEntity() })
            Resource.Success(books)
        } else {
            Resource.Error(response.message())
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getBook(id: Int): Resource<BookDto> = try {
        val response = api.getBook(id)
        if (response.isSuccessful) {
            val book = response.body()!!
            bookDao.upsertBook(book.toEntity())
            Resource.Success(book)
        } else {
            Resource.Error(response.message())
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun createBook(request: CreateBookRequest): Resource<BookDto> = try {
        val response = api.createBook(request)
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun updateBook(id: Int, request: UpdateBookRequest): Resource<BookDto> = try {
        val response = api.updateBook(id, request)
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun deleteBook(id: Int): Resource<Unit> = try {
        val response = api.deleteBook(id)
        if (response.isSuccessful) {
            bookDao.deleteBook(id)
            Resource.Success(Unit)
        } else {
            Resource.Error(response.message())
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun searchBooks(term: String): Resource<List<BookDto>> = try {
        val response = api.searchBooks(term)
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun updateReadingStatus(id: Int, request: UpdateReadingStatusRequest): Resource<Unit> = try {
        val response = api.updateReadingStatus(id, request)
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun lendBook(id: Int): Resource<Unit> = try {
        val response = api.lendBook(id)
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun returnBook(id: Int): Resource<Unit> = try {
        val response = api.returnBook(id)
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun addNote(bookId: Int, text: String): Resource<Unit> = try {
        val response = api.addNote(bookId, CreateNoteRequest(text))
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun updateNote(noteId: Int, text: String): Resource<Unit> = try {
        val response = api.updateNote(noteId, UpdateNoteRequest(text))
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun deleteNote(noteId: Int): Resource<Unit> = try {
        val response = api.deleteNote(noteId)
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun uploadCover(bookId: Int, filePath: String): Resource<String> = try {
        val file = File(filePath)
        val requestBody = file.asRequestBody("image/jpeg".toMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val response = api.uploadCover(bookId, part)
        if (response.isSuccessful) Resource.Success(response.body()!!.coverImageUrl)
        else Resource.Error("Cover upload failed")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Cover upload failed")
    }

    suspend fun lookupIsbn(isbn: String): Resource<IsbnLookupDto> = try {
        val response = api.lookupIsbn(isbn)
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error("No book found for that ISBN")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getGenres(): Resource<List<GenreDto>> = try {
        val response = api.getGenres()
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getFormats(): Resource<List<FormatDto>> = try {
        val response = api.getFormats()
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getStatistics(): Resource<StatisticsDto> = try {
        val response = api.getStatistics()
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }
}

private fun BookDto.toEntity() = BookEntity(
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
