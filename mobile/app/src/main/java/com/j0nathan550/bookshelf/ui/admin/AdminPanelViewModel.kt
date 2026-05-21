package com.j0nathan550.bookshelf.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j0nathan550.bookshelf.data.remote.dto.AdminDashboardDto
import com.j0nathan550.bookshelf.data.remote.dto.AdminUserDto
import com.j0nathan550.bookshelf.data.remote.dto.BookDto
import com.j0nathan550.bookshelf.data.repository.AdminRepository
import com.j0nathan550.bookshelf.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminPanelUiState(
    val dashboard: AdminDashboardDto? = null,
    val pendingBooks: List<BookDto> = emptyList(),
    val users: List<AdminUserDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPanelUiState())
    val uiState: StateFlow<AdminPanelUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val dashboardResult = adminRepository.getDashboard()
            val pendingResult = adminRepository.getPendingBooks()
            val usersResult = adminRepository.getUsers()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                dashboard = (dashboardResult as? Resource.Success)?.data,
                pendingBooks = (pendingResult as? Resource.Success)?.data ?: emptyList(),
                users = (usersResult as? Resource.Success)?.data ?: emptyList(),
                errorMessage = (dashboardResult as? Resource.Error)?.message,
            )
        }
    }

    fun approveBook(bookId: Int) {
        viewModelScope.launch {
            when (adminRepository.approveBook(bookId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        pendingBooks = _uiState.value.pendingBooks.filter { it.id != bookId },
                    )
                    refreshDashboard()
                }
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
            }
        }
    }

    fun rejectBook(bookId: Int) {
        viewModelScope.launch {
            when (adminRepository.rejectBook(bookId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        pendingBooks = _uiState.value.pendingBooks.filter { it.id != bookId },
                    )
                    refreshDashboard()
                }
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
            }
        }
    }

    fun disableUser(userId: String) {
        viewModelScope.launch {
            when (adminRepository.disableUser(userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        users = _uiState.value.users.map {
                            if (it.id == userId) it.copy(isActive = false) else it
                        },
                    )
                }
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
            }
        }
    }

    fun enableUser(userId: String) {
        viewModelScope.launch {
            when (adminRepository.enableUser(userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        users = _uiState.value.users.map {
                            if (it.id == userId) it.copy(isActive = true) else it
                        },
                    )
                }
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun refreshDashboard() {
        val result = adminRepository.getDashboard()
        if (result is Resource.Success) {
            _uiState.value = _uiState.value.copy(dashboard = result.data)
        }
    }
}
