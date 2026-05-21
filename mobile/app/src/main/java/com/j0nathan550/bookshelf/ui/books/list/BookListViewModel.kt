package com.j0nathan550.bookshelf.ui.books.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j0nathan550.bookshelf.data.local.entity.BookEntity
import com.j0nathan550.bookshelf.data.remote.dto.BookDto
import com.j0nathan550.bookshelf.data.repository.AuthRepository
import com.j0nathan550.bookshelf.data.repository.BookRepository
import com.j0nathan550.bookshelf.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookListUiState(
    val books: List<BookDto> = emptyList(),
    val cachedBooks: List<BookEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val statusFilter: String = "All",
    val userFullName: String = "",
    val isAdmin: Boolean = false,
    val selectedTab: Int = 0,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class BookListViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookListUiState())
    val uiState: StateFlow<BookListUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        _uiState.value = _uiState.value.copy(
            userFullName = authRepository.getFullName() ?: "",
            isAdmin = authRepository.isAdmin(),
        )
        loadBooks()
        observeCache()
    }

    private fun observeCache() {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) bookRepository.cachedBooks
                    else bookRepository.searchCached(query)
                }
                .collect { entities ->
                    _uiState.value = _uiState.value.copy(cachedBooks = entities)
                }
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = bookRepository.fetchBooks()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    books = result.data!!,
                    isLoading = false,
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
                is Resource.Loading -> Unit
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchQuery.value = query
    }

    fun onStatusFilterChange(filter: String) {
        _uiState.value = _uiState.value.copy(statusFilter = filter)
    }

    fun onTabSelected(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun clearError() = run { _uiState.value = _uiState.value.copy(errorMessage = null) }

    fun logout() = authRepository.logout()

    fun filteredBooks(): List<BookDto> {
        val state = _uiState.value
        var list = if (state.selectedTab == 0) {
            state.books.filter { it.isApproved }
        } else {
            state.books.filter { !it.isApproved }
        }
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.lowercase()
            list = list.filter { it.title.lowercase().contains(q) || it.author.lowercase().contains(q) }
        }
        if (state.selectedTab == 0 && state.statusFilter != "All") {
            list = when (state.statusFilter) {
                "Lent Out" -> list.filter { it.lendingRecord != null && !it.lendingRecord.isReturned }
                "Available" -> list.filter { it.lendingRecord == null || it.lendingRecord.isReturned }
                else -> list.filter { it.readingStatus?.status == state.statusFilter }
            }
        }
        return list
    }
}
