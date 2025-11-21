using BookShelf.Application.DTOs;
using BookShelf.Application.Results;

namespace BookShelf.Application.Services;

public interface IBookService
{
    Task<Result<BookDto>> CreateBookAsync(CreateBookDto dto, string userId);
    Task<Result<BookDto>> GetBookAsync(int bookId, string userId);
    Task<Result<IEnumerable<BookDto>>> GetUserBooksAsync(string userId);
    Task<Result<BookDto>> UpdateBookAsync(int bookId, UpdateBookDto dto, string userId);
    Task<Result> DeleteBookAsync(int bookId, string userId);
    Task<Result<IEnumerable<BookDto>>> SearchBooksAsync(string userId, string searchTerm);
    Task<Result> SetReadingStatusAsync(int bookId, string userId, string status, DateTime? completionDate = null);
    Task<Result> LendBookAsync(int bookId, string userId, DateTime lendingDate);
    Task<Result> ReturnBookAsync(int bookId, string userId, DateTime returnDate);
    Task<Result<IEnumerable<BookDto>>> GetLentBooksAsync(string userId);
    Task<Result> AddNoteAsync(int bookId, string userId, string noteText);
    Task<Result> UpdateNoteAsync(int noteId, string userId, string noteText);
    Task<Result> DeleteNoteAsync(int noteId, string userId);
    Task<bool> GenreExistsAsync(int? genreId);
    Task<bool> FormatExistsAsync(int? formatId);
}