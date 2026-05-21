package com.j0nathan550.bookshelf.ui.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.j0nathan550.bookshelf.data.repository.AuthRepository
import com.j0nathan550.bookshelf.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    // biometric gate
    val showBiometricButton: Boolean = false,
    val showPasswordMode: Boolean = false,
    val storedEmail: String = "",
    val storedName: String = "",
    // enrollment dialog (shown once after first successful password login)
    val showEnableBiometricDialog: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        val showBiometric = authRepository.isLoggedIn() &&
            authRepository.isBiometricEnabled() &&
            isBiometricHardwareAvailable()
        _uiState.value = _uiState.value.copy(
            showBiometricButton = showBiometric,
            storedEmail = authRepository.getUserEmail() ?: "",
            storedName = authRepository.getFullName() ?: "",
        )
    }

    private fun isBiometricHardwareAvailable(): Boolean =
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        ) == BiometricManager.BIOMETRIC_SUCCESS

    // ── password-mode login ─────────────────────────────────────────────────

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email and password are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.login(state.email.trim(), state.password)) {
                is Resource.Success -> {
                    val body = result.data!!
                    if (body.success) {
                        if (isBiometricHardwareAvailable() && !authRepository.isBiometricEnabled()) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                showEnableBiometricDialog = true,
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = body.message)
                    }
                }
                is Resource.Error -> _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    // ── biometric-mode actions ──────────────────────────────────────────────

    fun onBiometricLoginSuccess() {
        _uiState.value = _uiState.value.copy(isLoggedIn = true)
    }

    fun onBiometricError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    /** User chose "Use password instead" on the biometric screen. */
    fun switchToPasswordMode() {
        _uiState.value = _uiState.value.copy(
            showPasswordMode = true,
            email = _uiState.value.storedEmail,
        )
    }

    /** User chose "Sign in with a different account". Clears session + biometric. */
    fun switchToDifferentAccount() {
        authRepository.setBiometricEnabled(false)
        authRepository.logout()
        _uiState.value = LoginUiState()
    }

    // ── enrollment dialog ───────────────────────────────────────────────────

    fun onEnableBiometricChoice(enable: Boolean) {
        if (enable) authRepository.setBiometricEnabled(true)
        _uiState.value = _uiState.value.copy(
            showEnableBiometricDialog = false,
            isLoggedIn = true,
        )
    }

    // ── misc ────────────────────────────────────────────────────────────────

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
