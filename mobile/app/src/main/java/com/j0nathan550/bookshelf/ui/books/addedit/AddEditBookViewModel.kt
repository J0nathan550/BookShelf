package com.j0nathan550.bookshelf.ui.books.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j0nathan550.bookshelf.data.remote.dto.CreateBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.FormatDto
import com.j0nathan550.bookshelf.data.remote.dto.GenreDto
import com.j0nathan550.bookshelf.data.remote.dto.UpdateBookRequest
import com.j0nathan550.bookshelf.data.repository.BookRepository
import com.j0nathan550.bookshelf.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditBookUiState(
    val title: String = "",
    val author: String = "",
    val selectedGenreId: Int? = null,
    val selectedFormatId: Int? = null,
    val pages: String = "",
    val coverImageUrl: String = "",
    val capturedPhotoPath: String? = null,
    val genres: List<GenreDto> = emptyList(),
    val formats: List<FormatDto> = emptyList(),
    val isLoading: Boolean = false,
    val isLookingUpIsbn: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AddEditBookViewModel @Inject constructor(private val bookRepository: BookRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditBookUiState())
    val uiState: StateFlow<AddEditBookUiState> = _uiState.asStateFlow()

    fun init(bookId: Int?) {
        viewModelScope.launch {
            // Load reference data
            val genresResult = bookRepository.getGenres()
            val formatsResult = bookRepository.getFormats()
            _uiState.value = _uiState.value.copy(
                genres = (genresResult as? Resource.Success)?.data ?: emptyList(),
                formats = (formatsResult as? Resource.Success)?.data ?: emptyList(),
            )
            // If editing, pre-fill
            if (bookId != null) {
                when (val result = bookRepository.getBook(bookId)) {
                    is Resource.Success -> {
                        val book = result.data!!
                        _uiState.value = _uiState.value.copy(
                            title = book.title,
                            author = book.author,
                            selectedGenreId = book.genre?.id,
                            selectedFormatId = book.format?.id,
                            pages = book.pages?.toString() ?: "",
                            coverImageUrl = book.coverImageUrl ?: "",
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    fun onTitleChange(v: String) = run { _uiState.value = _uiState.value.copy(title = v) }
    fun onAuthorChange(v: String) = run { _uiState.value = _uiState.value.copy(author = v) }
    fun onGenreSelected(id: Int) = run { _uiState.value = _uiState.value.copy(selectedGenreId = id) }
    fun onFormatSelected(id: Int) = run { _uiState.value = _uiState.value.copy(selectedFormatId = id) }
    fun onPagesChange(v: String) = run { _uiState.value = _uiState.value.copy(pages = v) }
    fun onCoverUrlChange(v: String) = run { _uiState.value = _uiState.value.copy(coverImageUrl = v, capturedPhotoPath = null) }
    fun onPhotoTaken(path: String) = run { _uiState.value = _uiState.value.copy(capturedPhotoPath = path, coverImageUrl = "") }
    fun clearCapturedPhoto() = run { _uiState.value = _uiState.value.copy(capturedPhotoPath = null) }
    fun clearError() = run { _uiState.value = _uiState.value.copy(errorMessage = null) }

    fun lookupIsbn(isbn: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLookingUpIsbn = true, errorMessage = null)
            when (val result = bookRepository.lookupIsbn(isbn)) {
                is Resource.Success -> {
                    val data = result.data!!
                    _uiState.value = _uiState.value.copy(
                        isLookingUpIsbn = false,
                        title = data.title.ifBlank { _uiState.value.title },
                        author = data.author.ifBlank { _uiState.value.author },
                        pages = data.pages?.toInt()?.toString() ?: _uiState.value.pages,
                        coverImageUrl = data.coverImageUrl ?: _uiState.value.coverImageUrl,
                    )
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLookingUpIsbn = false,
                    errorMessage = result.message ?: "ISBN lookup failed",
                )
                is Resource.Loading -> Unit
            }
        }
    }

    fun save(bookId: Int?) {
        val state = _uiState.value
        if (state.title.isBlank() || state.author.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Title and author are required")
            return
        }
        val genreId = state.selectedGenreId ?: run {
            _uiState.value = state.copy(errorMessage = "Please select a genre")
            return
        }
        val formatId = state.selectedFormatId ?: run {
            _uiState.value = state.copy(errorMessage = "Please select a format")
            return
        }
        val pages = state.pages.toIntOrNull() ?: run {
            _uiState.value = state.copy(errorMessage = "Please enter a valid page count")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val capturedPath = state.capturedPhotoPath

            // For edit with a captured photo: upload first to get the URL for the update call.
            var coverUrl = state.coverImageUrl.takeIf { it.isNotBlank() }
            if (bookId != null && capturedPath != null) {
                val uploadResult = bookRepository.uploadCover(bookId, capturedPath)
                if (uploadResult is Resource.Success) coverUrl = uploadResult.data
            }

            val result = if (bookId == null) {
                bookRepository.createBook(CreateBookRequest(state.title, state.author, genreId, formatId, pages, coverUrl))
            } else {
                bookRepository.updateBook(bookId, UpdateBookRequest(state.title, state.author, genreId, formatId, pages, coverUrl))
            }
            when (result) {
                is Resource.Success -> {
                    // For a new book with captured photo: upload now that we have the ID.
                    if (bookId == null && capturedPath != null) {
                        val newId = result.data!!.id
                        bookRepository.uploadCover(newId, capturedPath)
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> Unit
            }
        }
    }
}
