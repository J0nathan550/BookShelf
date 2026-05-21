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

data class EmailVerificationUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null,
    val isVerified: Boolean = false,
    val resendMessage: String? = null,
)

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) {
        if (value.length <= 6 && value.all(Char::isDigit)) {
            _uiState.value = _uiState.value.copy(code = value, errorMessage = null)
        }
    }

    fun verify(userId: String) {
        val state = _uiState.value
        if (state.code.length < 6) {
            _uiState.value = state.copy(errorMessage = "Enter the complete 6-digit code")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.verifyEmailCode(userId, state.code)) {
                is Resource.Success -> {
                    val body = result.data!!
                    if (body.success) {
                        _uiState.value = _uiState.value.copy(isLoading = false, isVerified = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = body.message)
                    }
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    fun resend(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResending = true, resendMessage = null, errorMessage = null)
            when (val result = authRepository.resendVerificationCode(userId)) {
                is Resource.Success -> {
                    val body = result.data!!
                    _uiState.value = _uiState.value.copy(
                        isResending = false,
                        code = "",
                        resendMessage = if (body.success) body.message else null,
                        errorMessage = if (!body.success) body.message else null,
                    )
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isResending = false, errorMessage = result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
    fun clearResendMessage() { _uiState.value = _uiState.value.copy(resendMessage = null) }
}
