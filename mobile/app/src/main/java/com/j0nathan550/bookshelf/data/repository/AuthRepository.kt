package com.j0nathan550.bookshelf.data.repository

import com.google.gson.Gson
import com.j0nathan550.bookshelf.data.remote.ApiService
import com.j0nathan550.bookshelf.data.remote.dto.AuthResponse
import com.j0nathan550.bookshelf.data.remote.dto.LoginRequest
import com.j0nathan550.bookshelf.data.remote.dto.RegisterRequest
import com.j0nathan550.bookshelf.data.remote.dto.ForgotPasswordRequest
import com.j0nathan550.bookshelf.data.remote.dto.ResendVerificationCodeRequest
import com.j0nathan550.bookshelf.data.remote.dto.ResetPasswordCodeRequest
import com.j0nathan550.bookshelf.data.remote.dto.VerifyEmailCodeRequest
import com.j0nathan550.bookshelf.util.Resource
import com.j0nathan550.bookshelf.util.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager,
) {
    private val gson = Gson()

    suspend fun login(email: String, password: String): Resource<AuthResponse> = try {
        val response = api.login(LoginRequest(email, password))
        if (response.isSuccessful) {
            val body = response.body()!!
            if (body.success && body.token != null) {
                tokenManager.saveToken(body.token)
                body.userId?.let { tokenManager.saveUserId(it) }
                body.email?.let { tokenManager.saveUserEmail(it) }
                body.fullName?.let { tokenManager.saveFullName(it) }
                tokenManager.saveRoles(body.roles)
            }
            Resource.Success(body)
        } else {
            Resource.Error(parseErrorMessage(response.errorBody()?.string(), response.message()))
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun register(email: String, password: String, fullName: String): Resource<AuthResponse> = try {
        val response = api.register(RegisterRequest(email, password, fullName))
        if (response.isSuccessful) {
            Resource.Success(response.body()!!)
        } else {
            Resource.Error(parseErrorMessage(response.errorBody()?.string(), response.message()))
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun verifyEmailCode(userId: String, code: String): Resource<AuthResponse> = try {
        val response = api.verifyEmailCode(VerifyEmailCodeRequest(userId, code))
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(parseErrorMessage(response.errorBody()?.string(), response.message()))
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun resendVerificationCode(userId: String): Resource<AuthResponse> = try {
        val response = api.resendVerificationCode(ResendVerificationCodeRequest(userId))
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(parseErrorMessage(response.errorBody()?.string(), response.message()))
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun forgotPassword(email: String): Resource<AuthResponse> = try {
        val response = api.forgotPassword(ForgotPasswordRequest(email))
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(parseErrorMessage(response.errorBody()?.string(), response.message()))
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun resetPasswordWithCode(email: String, code: String, newPassword: String): Resource<AuthResponse> = try {
        val response = api.resetPasswordWithCode(ResetPasswordCodeRequest(email, code, newPassword))
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(parseErrorMessage(response.errorBody()?.string(), response.message()))
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getCurrentUser(): Resource<AuthResponse> = try {
        val response = api.getCurrentUser()
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(parseErrorMessage(response.errorBody()?.string(), response.message()))
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    fun logout() = tokenManager.clearAll()

    fun isLoggedIn() = tokenManager.getToken() != null

    fun isAdmin() = tokenManager.isAdmin()

    fun getUserId() = tokenManager.getUserId()

    fun getUserEmail() = tokenManager.getUserEmail()

    fun getFullName() = tokenManager.getFullName()

    // Try to extract a human-readable message from the response body.
    // The backend returns AuthResponse JSON on auth errors; ASP.NET Core model
    // validation returns a ProblemDetails object with an "errors" map.
    private fun parseErrorMessage(body: String?, fallback: String): String {
        if (body.isNullOrBlank()) return fallback
        return try {
            val auth = gson.fromJson(body, AuthResponse::class.java)
            auth.message.takeIf { it.isNotBlank() } ?: fallback
        } catch (_: Exception) {
            try {
                val problem = gson.fromJson(body, Map::class.java)
                @Suppress("UNCHECKED_CAST")
                val errors = problem["errors"] as? Map<String, List<String>>
                errors?.values?.flatten()?.firstOrNull() ?: fallback
            } catch (_: Exception) {
                fallback
            }
        }
    }
}
