import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminDashboardDto, AdminUserDto } from '../models/admin.model';
import { BookDto } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly baseUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<AdminDashboardDto> {
    return this.http.get<AdminDashboardDto>(`${this.baseUrl}/dashboard`);
  }

  getUsers(): Observable<AdminUserDto[]> {
    return this.http.get<AdminUserDto[]>(`${this.baseUrl}/users`);
  }

  disableUser(userId: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/users/${userId}/disable`, {});
  }

  enableUser(userId: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/users/${userId}/enable`, {});
  }

  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/users/${userId}`);
  }

  getPendingBooks(): Observable<BookDto[]> {
    return this.http.get<BookDto[]>(`${this.baseUrl}/books/pending`);
  }

  getAllBooks(): Observable<BookDto[]> {
    return this.http.get<BookDto[]>(`${this.baseUrl}/books/all`);
  }

  approveBook(bookId: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/books/${bookId}/approve`, {});
  }

  rejectBook(bookId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/books/${bookId}`);
  }

  importData(data: { books: any[] }): Observable<{ booksImported: number; notesImported: number }> {
    return this.http.post<{ booksImported: number; notesImported: number }>(`${this.baseUrl}/import`, data);
  }
}
