package com.j0nathan550.bookshelf.data.remote

import com.j0nathan550.bookshelf.data.remote.dto.AdminDashboardDto
import com.j0nathan550.bookshelf.data.remote.dto.AdminUserDto
import com.j0nathan550.bookshelf.data.remote.dto.AuthResponse
import com.j0nathan550.bookshelf.data.remote.dto.BookDto
import com.j0nathan550.bookshelf.data.remote.dto.BookNoteDto
import com.j0nathan550.bookshelf.data.remote.dto.CreateBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.CreateNoteRequest
import com.j0nathan550.bookshelf.data.remote.dto.FormatDto
import com.j0nathan550.bookshelf.data.remote.dto.GenreDto
import com.j0nathan550.bookshelf.data.remote.dto.CoverUploadResponse
import com.j0nathan550.bookshelf.data.remote.dto.IsbnLookupDto
import com.j0nathan550.bookshelf.data.remote.dto.LendBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.LoginRequest
import com.j0nathan550.bookshelf.data.remote.dto.RegisterFcmTokenRequest
import com.j0nathan550.bookshelf.data.remote.dto.RegisterRequest
import com.j0nathan550.bookshelf.data.remote.dto.ForgotPasswordRequest
import com.j0nathan550.bookshelf.data.remote.dto.ResendVerificationCodeRequest
import com.j0nathan550.bookshelf.data.remote.dto.ResetPasswordCodeRequest
import com.j0nathan550.bookshelf.data.remote.dto.VerifyEmailCodeRequest
import com.j0nathan550.bookshelf.data.remote.dto.ReturnBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.StatisticsDto
import com.j0nathan550.bookshelf.data.remote.dto.UpdateBookRequest
import com.j0nathan550.bookshelf.data.remote.dto.UpdateNoteRequest
import com.j0nathan550.bookshelf.data.remote.dto.UpdateReadingStatusRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/verify-email-code")
    suspend fun verifyEmailCode(@Body request: VerifyEmailCodeRequest): Response<AuthResponse>

    @POST("api/auth/resend-verification-code")
    suspend fun resendVerificationCode(@Body request: ResendVerificationCodeRequest): Response<AuthResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<AuthResponse>

    @POST("api/auth/reset-password-code")
    suspend fun resetPasswordWithCode(@Body request: ResetPasswordCodeRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/auth/current-user")
    suspend fun getCurrentUser(): Response<AuthResponse>

    @POST("api/notifications/register-token")
    suspend fun registerFcmToken(@Body request: RegisterFcmTokenRequest): Response<Unit>

    // Books
    @GET("api/books")
    suspend fun getBooks(): Response<List<BookDto>>

    @GET("api/books/{id}")
    suspend fun getBook(@Path("id") id: Int): Response<BookDto>

    @POST("api/books")
    suspend fun createBook(@Body request: CreateBookRequest): Response<BookDto>

    @PUT("api/books/{id}")
    suspend fun updateBook(@Path("id") id: Int, @Body request: UpdateBookRequest): Response<BookDto>

    @DELETE("api/books/{id}")
    suspend fun deleteBook(@Path("id") id: Int): Response<Unit>

    @Multipart
    @POST("api/books/{id}/cover")
    suspend fun uploadCover(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part,
    ): Response<CoverUploadResponse>

    @GET("api/books/isbn/{isbn}")
    suspend fun lookupIsbn(@Path("isbn") isbn: String): Response<IsbnLookupDto>

    @GET("api/books/search")
    suspend fun searchBooks(@Query("term") term: String): Response<List<BookDto>>

    @PUT("api/books/{id}/reading-status")
    suspend fun updateReadingStatus(
        @Path("id") id: Int,
        @Body request: UpdateReadingStatusRequest,
    ): Response<Unit>

    @POST("api/books/{id}/lend")
    suspend fun lendBook(@Path("id") id: Int): Response<Unit>

    @PUT("api/books/{id}/return")
    suspend fun returnBook(@Path("id") id: Int): Response<Unit>

    @GET("api/books/lent")
    suspend fun getLentBooks(): Response<List<BookDto>>

    @POST("api/books/{id}/notes")
    suspend fun addNote(@Path("id") id: Int, @Body request: CreateNoteRequest): Response<Unit>

    @PUT("api/books/notes/{noteId}")
    suspend fun updateNote(@Path("noteId") noteId: Int, @Body request: UpdateNoteRequest): Response<Unit>

    @DELETE("api/books/notes/{noteId}")
    suspend fun deleteNote(@Path("noteId") noteId: Int): Response<Unit>

    // Reference data
    @GET("api/genres")
    suspend fun getGenres(): Response<List<GenreDto>>

    @GET("api/formats")
    suspend fun getFormats(): Response<List<FormatDto>>

    // Statistics
    @GET("api/statistics")
    suspend fun getStatistics(): Response<StatisticsDto>

    // Admin
    @GET("api/admin/dashboard")
    suspend fun getAdminDashboard(): Response<AdminDashboardDto>

    @GET("api/admin/users")
    suspend fun getAdminUsers(): Response<List<AdminUserDto>>

    @PUT("api/admin/users/{userId}/disable")
    suspend fun disableUser(@Path("userId") userId: String): Response<Unit>

    @PUT("api/admin/users/{userId}/enable")
    suspend fun enableUser(@Path("userId") userId: String): Response<Unit>

    @DELETE("api/admin/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): Response<Unit>

    @GET("api/admin/books/pending")
    suspend fun getPendingBooks(): Response<List<BookDto>>

    @GET("api/admin/books/all")
    suspend fun getAllBooks(): Response<List<BookDto>>

    @PUT("api/admin/books/{bookId}/approve")
    suspend fun approveBook(@Path("bookId") bookId: Int): Response<Unit>

    @DELETE("api/admin/books/{bookId}")
    suspend fun rejectBook(@Path("bookId") bookId: Int): Response<Unit>
}
