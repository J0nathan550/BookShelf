package com.j0nathan550.bookshelf.ui.account

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

data class AccountUiState(
    val email: String = "",
    val fullName: String = "",
    val biometricEnabled: Boolean = false,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AccountUiState(
            email = authRepository.getUserEmail() ?: "",
            fullName = authRepository.getFullName() ?: "",
            biometricEnabled = authRepository.isBiometricEnabled(),
        ),
    )
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    val isAdmin: Boolean get() = authRepository.isAdmin()

    init {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Resource.Success -> {
                    val user = result.data!!
                    _uiState.value = _uiState.value.copy(
                        email = user.email ?: authRepository.getUserEmail() ?: "",
                        fullName = user.fullName ?: authRepository.getFullName() ?: "",
                    )
                }
                else -> Unit
            }
        }
    }

    fun toggleBiometric() {
        val newValue = !_uiState.value.biometricEnabled
        authRepository.setBiometricEnabled(newValue)
        _uiState.value = _uiState.value.copy(biometricEnabled = newValue)
    }

    fun logout() = authRepository.logout()
}
