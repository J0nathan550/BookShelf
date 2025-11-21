using BookShelf.Application.DTOs;
using BookShelf.Application.Results;
using BookShelf.Application.Services;
using BookShelf.Domain.Entities;
using BookShelf.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace BookShelf.Infrastructure.Services;

public class BookService : IBookService
{
    private readonly ApplicationDbContext _dbContext;

    public BookService(ApplicationDbContext dbContext)
    {
        _dbContext = dbContext;
    }

    public async Task<Result<BookDto>> CreateBookAsync(CreateBookDto dto, string userId)
    {
        if (!await GenreExistsAsync(dto.GenreId))
            return Result<BookDto>.Fail("Invalid GenreId.");

        if (!await FormatExistsAsync(dto.FormatId))
            return Result<BookDto>.Fail("Invalid FormatId.");

        var book = new Book
        {
            Title = dto.Title,
            Author = dto.Author,
            GenreId = dto.GenreId,
            FormatId = dto.FormatId,
            Pages = dto.Pages,
            CoverImageUrl = dto.CoverImageUrl,
            DateAdded = DateTime.UtcNow,
            Status = "Available"
        };

        _dbContext.Books.Add(book);
        await _dbContext.SaveChangesAsync();

        return Result<BookDto>.Ok(MapToDto(book));
    }

    public async Task<Result<BookDto>> GetBookAsync(int bookId, string userId)
    {
        var book = await GetBookWithDetailsAsync(bookId);

        if (book == null)
            return Result<BookDto>.Fail("Book not found");

        return Result<BookDto>.Ok(MapToDto(book));
    }

    public async Task<Result<IEnumerable<BookDto>>> GetUserBooksAsync(string userId)
    {
        var books = await GetBooksBorrowedByUserAsync(userId);
        return Result<IEnumerable<BookDto>>.Ok(books.Select(MapToDto));
    }

    public async Task<Result<BookDto>> UpdateBookAsync(int bookId, UpdateBookDto dto, string userId)
    {
        var book = await GetBookWithDetailsAsync(bookId);

        if (book == null)
            return Result<BookDto>.Fail("Book not found");

        book.Title = dto.Title;
        book.Author = dto.Author;
        book.GenreId = dto.GenreId;
        book.FormatId = dto.FormatId;
        book.Pages = dto.Pages;
        book.CoverImageUrl = dto.CoverImageUrl;

        _dbContext.Books.Update(book);
        await _dbContext.SaveChangesAsync();

        return Result<BookDto>.Ok(MapToDto(book));
    }

    public async Task<Result> DeleteBookAsync(int bookId, string userId)
    {
        var book = await GetBookWithDetailsAsync(bookId);

        if (book == null)
            return Result.Fail("Book not found");

        _dbContext.Books.Remove(book);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result<IEnumerable<BookDto>>> SearchBooksAsync(string userId, string searchTerm)
    {
        var books = await _dbContext.Books
            .Where(b => b.Title.Contains(searchTerm) || b.Author.Contains(searchTerm))
            .Include(b => b.Genre)
            .Include(b => b.Format)
            .ToListAsync();

        return Result<IEnumerable<BookDto>>.Ok(books.Select(MapToDto));
    }

    public async Task<Result> SetReadingStatusAsync(int bookId, string userId, string status, DateTime? completionDate = null)
    {
        var book = await GetBookWithDetailsAsync(bookId);

        if (book == null)
            return Result.Fail("Book not found");

        book.Status = status;
        book.CompletionDate = completionDate;

        _dbContext.Books.Update(book);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result> LendBookAsync(int bookId, string userId, DateTime lendingDate)
    {
        var book = await GetBookWithDetailsAsync(bookId);

        if (book == null)
            return Result.Fail("Book not found");

        if (book.LendingRecords.Any(lr => !lr.IsReturned))
            return Result.Fail("Book is already lent out");

        var lendingRecord = new LendingRecord
        {
            BookId = bookId,
            ApplicationUserId = userId,
            LendingDate = lendingDate,
            IsReturned = false
        };

        _dbContext.LendingRecords.Add(lendingRecord);
        book.Status = "Lent Out";
        _dbContext.Books.Update(book);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result> ReturnBookAsync(int bookId, string userId, DateTime returnDate)
    {
        var book = await GetBookWithDetailsAsync(bookId);

        if (book == null)
            return Result.Fail("Book not found");

        var activeLending = book.LendingRecords?.FirstOrDefault(lr => !lr.IsReturned);

        if (activeLending == null)
            return Result.Fail("Book is not currently lent out");

        activeLending.ReturnDate = returnDate;
        activeLending.IsReturned = true;
        book.Status = "Available";

        _dbContext.LendingRecords.Update(activeLending);
        _dbContext.Books.Update(book);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result<IEnumerable<BookDto>>> GetLentBooksAsync(string userId)
    {
        var books = await GetBooksBorrowedByUserAsync(userId);
        return Result<IEnumerable<BookDto>>.Ok(books.Select(MapToDto));
    }

    public async Task<Result> AddNoteAsync(int bookId, string userId, string noteText)
    {
        var book = await GetBookWithDetailsAsync(bookId);

        if (book == null)
            return Result.Fail("Book not found");

        var note = new BookNote
        {
            BookId = bookId,
            ApplicationUserId = userId,
            NoteText = noteText,
            CreatedDate = DateTime.UtcNow
        };

        _dbContext.BookNotes.Add(note);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result> UpdateNoteAsync(int noteId, string userId, string noteText)
    {
        var note = await _dbContext.BookNotes.FindAsync(noteId);

        if (note == null)
            return Result.Fail("Note not found");

        if (note.ApplicationUserId != userId)
            return Result.Fail("You can only edit your own notes.");

        note.NoteText = noteText;
        note.ModifiedDate = DateTime.UtcNow;

        _dbContext.BookNotes.Update(note);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result> DeleteNoteAsync(int noteId, string userId)
    {
        var note = await _dbContext.BookNotes.FindAsync(noteId);

        if (note == null)
            return Result.Fail("Note not found");

        if (note.ApplicationUserId != userId)
            return Result.Fail("You can only delete your own notes.");

        _dbContext.BookNotes.Remove(note);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<bool> GenreExistsAsync(int? genreId)
    {
        if (genreId == null) return true;
        return await _dbContext.Genres.AnyAsync(g => g.Id == genreId);
    }

    public async Task<bool> FormatExistsAsync(int? formatId)
    {
        if (formatId == null) return true;
        return await _dbContext.BookFormats.AnyAsync(f => f.Id == formatId);
    }

    private async Task<IEnumerable<Book>> GetBooksBorrowedByUserAsync(string userId)
    {
        return await _dbContext.Books
            .Where(b => b.LendingRecords.Any(lr => lr.ApplicationUserId == userId && !lr.IsReturned))
            .Include(b => b.Genre)
            .Include(b => b.Format)
            .Include(b => b.LendingRecords)
            .OrderByDescending(b => b.DateAdded)
            .ToListAsync();
    }

    private async Task<Book?> GetBookWithDetailsAsync(int bookId)
    {
        return await _dbContext.Books
            .Where(b => b.Id == bookId)
            .Include(b => b.Genre)
            .Include(b => b.Format)
            .Include(b => b.LendingRecords)
            .Include(b => b.Notes)
            .FirstOrDefaultAsync();
    }

    private static BookDto MapToDto(Book book)
    {
        var activeLending = book.LendingRecords?.FirstOrDefault(x => !x.IsReturned);

        return new BookDto
        {
            Id = book.Id,
            Title = book.Title,
            Author = book.Author,
            Genre = book.Genre?.Name,
            Format = book.Format?.Name,
            Pages = book.Pages,
            CoverImageUrl = book.CoverImageUrl,
            DateAdded = book.DateAdded,
            ReadingStatus = new ReadingStatusDto
            {
                Status = book.Status,
                CompletionDate = book.CompletionDate,
                Rating = null
            },
            LendingRecord = activeLending != null ? new LendingRecordDto
            {
                Id = activeLending.Id,
                BorrowerName = activeLending.ApplicationUserId,
                LendingDate = activeLending.LendingDate,
                ReturnDate = activeLending.ReturnDate,
                IsReturned = activeLending.IsReturned
            } : null,
            Notes = book.Notes != null
                ? [.. book.Notes.Select(n => new BookNoteDto
                {
                    Id = n.Id,
                    NoteText = n.NoteText,
                    CreatedDate = n.CreatedDate,
                    ModifiedDate = n.ModifiedDate
                })]
                : []
        };
    }
}