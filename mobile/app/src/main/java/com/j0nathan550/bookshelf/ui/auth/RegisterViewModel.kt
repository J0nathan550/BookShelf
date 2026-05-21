package com.j0nathan550.bookshelf.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j0nathan550.bookshelf.data.repository.AuthRepository
import com.j0nathan550.bookshelf.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pendingVerificationUserId: String? = null,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.value.let { _uiState.value = it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.value.let { _uiState.value = it.copy(password = value, errorMessage = null) }
    fun onFullNameChange(value: String) = _uiState.value.let { _uiState.value = it.copy(fullName = value, errorMessage = null) }

    fun register() {
        val state = _uiState.value
        if (state.fullName.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "All fields are required")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.register(state.email.trim(), state.password, state.fullName.trim())) {
                is Resource.Success -> {
                    val body = result.data!!
                    if (body.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            pendingVerificationUserId = body.userId,
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = body.message)
                    }
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() = run { _uiState.value = _uiState.value.copy(errorMessage = null) }

    fun clearPendingNavigation() = run { _uiState.value = _uiState.value.copy(pendingVerificationUserId = null) }
}
