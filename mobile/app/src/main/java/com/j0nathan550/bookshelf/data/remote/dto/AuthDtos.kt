package com.j0nathan550.bookshelf.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false,
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
)

data class VerifyEmailCodeRequest(
    val userId: String,
    val code: String,
)

data class ResendVerificationCodeRequest(
    val userId: String,
)

data class ForgotPasswordRequest(
    val email: String,
)

data class ResetPasswordCodeRequest(
    val email: String,
    val code: String,
    val newPassword: String,
)

data class RegisterFcmTokenRequest(val token: String)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val userId: String?,
    val email: String?,
    val fullName: String?,
    val roles: List<String> = emptyList(),
)
