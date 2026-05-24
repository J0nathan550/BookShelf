import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  BookDto, CreateBookDto, UpdateBookDto, UpdateReadingStatusDto,
  NoteTextDto, BookNoteDto, GenreDto, FormatDto, IsbnLookupDto
} from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly baseUrl = `${environment.apiUrl}/books`;

  constructor(private http: HttpClient) {}

  getBooks(): Observable<BookDto[]> {
    return this.http.get<BookDto[]>(this.baseUrl);
  }

  getBook(id: number): Observable<BookDto> {
    return this.http.get<BookDto>(`${this.baseUrl}/${id}`);
  }

  createBook(dto: CreateBookDto): Observable<BookDto> {
    return this.http.post<BookDto>(this.baseUrl, dto);
  }

  updateBook(id: number, dto: UpdateBookDto): Observable<BookDto> {
    return this.http.put<BookDto>(`${this.baseUrl}/${id}`, dto);
  }

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  searchBooks(term: string): Observable<BookDto[]> {
    return this.http.get<BookDto[]>(`${this.baseUrl}/search`, { params: { searchTerm: term } });
  }

  setReadingStatus(id: number, dto: UpdateReadingStatusDto): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}/reading-status`, dto);
  }

  getLentBooks(): Observable<BookDto[]> {
    return this.http.get<BookDto[]>(`${this.baseUrl}/lent`);
  }

  lendBook(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/lend`, {});
  }

  returnBook(id: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}/return`, {});
  }

  uploadCover(id: number, file: File): Observable<{ coverImageUrl: string }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ coverImageUrl: string }>(`${this.baseUrl}/${id}/cover`, form);
  }

  addNote(bookId: number, dto: NoteTextDto): Observable<BookNoteDto> {
    return this.http.post<BookNoteDto>(`${this.baseUrl}/${bookId}/notes`, dto);
  }

  updateNote(noteId: number, dto: NoteTextDto): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/notes/${noteId}`, dto);
  }

  deleteNote(noteId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/notes/${noteId}`);
  }

  lookupIsbn(isbn: string): Observable<IsbnLookupDto> {
    return this.http.get<IsbnLookupDto>(`${this.baseUrl}/isbn/${isbn}`);
  }

  getGenres(): Observable<GenreDto[]> {
    return this.http.get<GenreDto[]>(`${environment.apiUrl}/genres`);
  }

  getFormats(): Observable<FormatDto[]> {
    return this.http.get<FormatDto[]>(`${environment.apiUrl}/formats`);
  }
}
