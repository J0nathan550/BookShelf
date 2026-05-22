package com.j0nathan550.bookshelf.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.j0nathan550.bookshelf.data.local.dao.BookDao
import com.j0nathan550.bookshelf.data.local.dao.PendingOperationDao
import com.j0nathan550.bookshelf.data.local.entity.BookEntity
import com.j0nathan550.bookshelf.data.local.entity.OperationType
import com.j0nathan550.bookshelf.data.local.entity.PendingOperationEntity
import com.j0nathan550.bookshelf.data.local.mapper.toBookDto
import com.j0nathan550.bookshelf.data.local.mapper.toEntity
import com.j0nathan550.bookshelf.data.remote.ApiService
import com.j0nathan550.bookshelf.data.remote.dto.BookDto
import com.j0nathan550.bookshelf.data.remote.dto.CreateBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.CreateNoteRequest
import com.j0nathan550.bookshelf.data.remote.dto.FormatDto
import com.j0nathan550.bookshelf.data.remote.dto.GenreDto
import com.j0nathan550.bookshelf.data.remote.dto.IsbnLookupDto
import com.j0nathan550.bookshelf.data.remote.dto.LendBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.ReturnBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.StatisticsDto
import com.j0nathan550.bookshelf.data.remote.dto.UpdateBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.UpdateNoteRequest
import com.j0nathan550.bookshelf.data.remote.dto.UpdateReadingStatusRequest
import com.j0nathan550.bookshelf.data.sync.AddNotePayload
import com.j0nathan550.bookshelf.data.sync.DeleteBookPayload
import com.j0nathan550.bookshelf.data.sync.DeleteNotePayload
import com.j0nathan550.bookshelf.data.sync.OfflineSyncWorker
import com.j0nathan550.bookshelf.data.sync.UpdateBookPayload
import com.j0nathan550.bookshelf.data.sync.UpdateNotePayload
import com.j0nathan550.bookshelf.data.sync.UpdateReadingStatusPayload
import com.j0nathan550.bookshelf.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService,
    private val bookDao: BookDao,
    private val pendingOps: PendingOperationDao,
    private val workManager: WorkManager,
    private val gson: Gson,
) {
    val cachedBooks: Flow<List<BookEntity>> = bookDao.getAllBooks()
    val cachedLentBooks: Flow<List<BookEntity>> = bookDao.getLentBooks()
    val pendingCount: Flow<Int> = pendingOps.pendingCount()

    fun searchCached(query: String): Flow<List<BookEntity>> = bookDao.searchBooks(query)

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun enqueueSyncWork() {
        val req = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        workManager.enqueueUniqueWork("offline_sync", ExistingWorkPolicy.KEEP, req)
    }

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

    suspend fun updateBook(id: Int, request: UpdateBookRequest): Resource<BookDto> {
        if (!isOnline()) {
            val existing = bookDao.getBook(id)
                ?: return Resource.Error("Book not found in local cache")
            val updated = existing.copy(
                title = request.title,
                author = request.author,
                genreId = request.genreId,
                genreName = if (request.genreId == existing.genreId) existing.genreName else null,
                formatId = request.formatId,
                formatName = if (request.formatId == existing.formatId) existing.formatName else null,
                pages = request.pages,
                coverImageUrl = request.coverImageUrl,
            )
            bookDao.upsertBook(updated)
            pendingOps.enqueue(
                PendingOperationEntity(
                    operationType = OperationType.UPDATE_BOOK,
                    payload = gson.toJson(UpdateBookPayload(id, request)),
                ),
            )
            enqueueSyncWork()
            return Resource.Success(updated.toBookDto())
        }
        return try {
            val response = api.updateBook(id, request)
            if (response.isSuccessful) Resource.Success(response.body()!!)
            else Resource.Error(response.message())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun deleteBook(id: Int): Resource<Unit> {
        if (!isOnline()) {
            bookDao.deleteBook(id)
            pendingOps.enqueue(
                PendingOperationEntity(
                    operationType = OperationType.DELETE_BOOK,
                    payload = gson.toJson(DeleteBookPayload(id)),
                ),
            )
            enqueueSyncWork()
            return Resource.Success(Unit)
        }
        return try {
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
    }

    suspend fun searchBooks(term: String): Resource<List<BookDto>> = try {
        val response = api.searchBooks(term)
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun updateReadingStatus(id: Int, request: UpdateReadingStatusRequest): Resource<Unit> {
        if (!isOnline()) {
            val existing = bookDao.getBook(id)
            if (existing != null) {
                bookDao.upsertBook(
                    existing.copy(
                        readingStatus = request.status,
                        rating = request.rating,
                        completionDate = request.completionDate,
                    ),
                )
            }
            pendingOps.enqueue(
                PendingOperationEntity(
                    operationType = OperationType.UPDATE_READING_STATUS,
                    payload = gson.toJson(UpdateReadingStatusPayload(id, request)),
                ),
            )
            enqueueSyncWork()
            return Resource.Success(Unit)
        }
        return try {
            val response = api.updateReadingStatus(id, request)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(response.message())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
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

    suspend fun addNote(bookId: Int, text: String): Resource<Unit> {
        if (!isOnline()) {
            pendingOps.enqueue(
                PendingOperationEntity(
                    operationType = OperationType.ADD_NOTE,
                    payload = gson.toJson(AddNotePayload(bookId, text)),
                ),
            )
            enqueueSyncWork()
            return Resource.Success(Unit)
        }
        return try {
            val response = api.addNote(bookId, CreateNoteRequest(text))
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(response.message())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun updateNote(noteId: Int, text: String): Resource<Unit> {
        if (!isOnline()) {
            pendingOps.enqueue(
                PendingOperationEntity(
                    operationType = OperationType.UPDATE_NOTE,
                    payload = gson.toJson(UpdateNotePayload(noteId, text)),
                ),
            )
            enqueueSyncWork()
            return Resource.Success(Unit)
        }
        return try {
            val response = api.updateNote(noteId, UpdateNoteRequest(text))
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(response.message())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun deleteNote(noteId: Int): Resource<Unit> {
        if (!isOnline()) {
            pendingOps.enqueue(
                PendingOperationEntity(
                    operationType = OperationType.DELETE_NOTE,
                    payload = gson.toJson(DeleteNotePayload(noteId)),
                ),
            )
            enqueueSyncWork()
            return Resource.Success(Unit)
        }
        return try {
            val response = api.deleteNote(noteId)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(response.message())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
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
