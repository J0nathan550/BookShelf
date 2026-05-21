package com.j0nathan550.bookshelf.data.repository

import com.j0nathan550.bookshelf.data.remote.ApiService
import com.j0nathan550.bookshelf.data.remote.dto.AdminDashboardDto
import com.j0nathan550.bookshelf.data.remote.dto.AdminUserDto
import com.j0nathan550.bookshelf.data.remote.dto.BookDto
import com.j0nathan550.bookshelf.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(private val api: ApiService) {

    suspend fun getDashboard(): Resource<AdminDashboardDto> = try {
        val response = api.getAdminDashboard()
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getUsers(): Resource<List<AdminUserDto>> = try {
        val response = api.getAdminUsers()
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun disableUser(userId: String): Resource<Unit> = try {
        val response = api.disableUser(userId)
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun enableUser(userId: String): Resource<Unit> = try {
        val response = api.enableUser(userId)
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun deleteUser(userId: String): Resource<Unit> = try {
        val response = api.deleteUser(userId)
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getPendingBooks(): Resource<List<BookDto>> = try {
        val response = api.getPendingBooks()
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun approveBook(bookId: Int): Resource<Unit> = try {
        val response = api.approveBook(bookId)
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun rejectBook(bookId: Int): Resource<Unit> = try {
        val response = api.rejectBook(bookId)
        if (response.isSuccessful) Resource.Success(Unit)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getAllBooks(): Resource<List<BookDto>> = try {
        val response = api.getAllBooks()
        if (response.isSuccessful) Resource.Success(response.body()!!)
        else Resource.Error(response.message())
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }
}
