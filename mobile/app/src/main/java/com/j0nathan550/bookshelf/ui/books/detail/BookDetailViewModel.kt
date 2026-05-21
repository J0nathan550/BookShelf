package com.j0nathan550.bookshelf.ui.books.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j0nathan550.bookshelf.data.remote.dto.BookDto
import com.j0nathan550.bookshelf.data.remote.dto.UpdateReadingStatusRequest
import com.j0nathan550.bookshelf.data.repository.AuthRepository
import com.j0nathan550.bookshelf.data.repository.BookRepository
import com.j0nathan550.bookshelf.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookDetailUiState(
    val book: BookDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDeleted: Boolean = false,
    val showLendDialog: Boolean = false,
    val showReturnDialog: Boolean = false,
    val showNoteDialog: Boolean = false,
    val editingNoteId: Int? = null,
    val noteText: String = "",
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val isAdmin: Boolean get() = authRepository.isAdmin()
    val currentUserId: String get() = authRepository.getUserId() ?: ""

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    fun loadBook(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = bookRepository.getBook(id)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(book = result.data, isLoading = false)
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateStatus(status: String, rating: Int?, completionDate: String?) {
        val bookId = _uiState.value.book?.id ?: return
        viewModelScope.launch {
            when (bookRepository.updateReadingStatus(bookId, UpdateReadingStatusRequest(status, rating, completionDate))) {
                is Resource.Success -> loadBook(bookId)
                is Resource.Error -> _uiState.value = _uiState.value.copy(errorMessage = "Failed to update status")
                is Resource.Loading -> Unit
            }
        }
    }

    fun deleteBook(onDeleted: () -> Unit) {
        val bookId = _uiState.value.book?.id ?: return
        viewModelScope.launch {
            when (bookRepository.deleteBook(bookId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isDeleted = true)
                    onDeleted()
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete book")
                is Resource.Loading -> Unit
            }
        }
    }

    fun lendBook() {
        val bookId = _uiState.value.book?.id ?: return
        viewModelScope.launch {
            when (bookRepository.lendBook(bookId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(showLendDialog = false)
                    loadBook(bookId)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(errorMessage = "Failed to lend book")
                is Resource.Loading -> Unit
            }
        }
    }

    fun returnBook() {
        val bookId = _uiState.value.book?.id ?: return
        viewModelScope.launch {
            when (bookRepository.returnBook(bookId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(showReturnDialog = false)
                    loadBook(bookId)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(errorMessage = "Failed to return book")
                is Resource.Loading -> Unit
            }
        }
    }

    fun saveNote() {
        val bookId = _uiState.value.book?.id ?: return
        val state = _uiState.value
        if (state.noteText.isBlank()) return
        viewModelScope.launch {
            val result = if (state.editingNoteId == null) {
                bookRepository.addNote(bookId, state.noteText)
            } else {
                bookRepository.updateNote(state.editingNoteId, state.noteText)
            }
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(showNoteDialog = false, noteText = "", editingNoteId = null)
                    loadBook(bookId)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(errorMessage = "Failed to save note")
                is Resource.Loading -> Unit
            }
        }
    }

    fun deleteNote(noteId: Int) {
        val bookId = _uiState.value.book?.id ?: return
        viewModelScope.launch {
            bookRepository.deleteNote(noteId)
            loadBook(bookId)
        }
    }

    fun showLendDialog() { _uiState.value = _uiState.value.copy(showLendDialog = true) }
    fun hideLendDialog() { _uiState.value = _uiState.value.copy(showLendDialog = false) }
    fun showReturnDialog() { _uiState.value = _uiState.value.copy(showReturnDialog = true) }
    fun hideReturnDialog() { _uiState.value = _uiState.value.copy(showReturnDialog = false) }
    fun showAddNoteDialog() { _uiState.value = _uiState.value.copy(showNoteDialog = true, noteText = "", editingNoteId = null) }
    fun showEditNoteDialog(id: Int, text: String) { _uiState.value = _uiState.value.copy(showNoteDialog = true, noteText = text, editingNoteId = id) }
    fun hideNoteDialog() { _uiState.value = _uiState.value.copy(showNoteDialog = false) }
    fun onNoteTextChange(v: String) { _uiState.value = _uiState.value.copy(noteText = v) }
    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}
