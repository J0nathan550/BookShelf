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

    public async Task<Result<BookDto>> CreateBookAsync(CreateBookDto dto, string userId, bool autoApprove = false)
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
            Status = "Available",
            ApplicationUserId = userId,
            IsApproved = autoApprove
        };

        _dbContext.Books.Add(book);
        await _dbContext.SaveChangesAsync();

        return Result<BookDto>.Ok(MapToDto(book));
    }

    public async Task<Result<BookDto>> GetBookAsync(int bookId, string userId, bool isAdmin = false)
    {
        var book = await GetBookWithDetailsAsync(bookId);

        if (book == null)
            return Result<BookDto>.Fail("Book not found");

        // Non-admin: only see own notes
        if (!isAdmin && book.Notes != null)
            book.Notes = [.. book.Notes.Where(n => n.ApplicationUserId == userId)];

        var dto = MapToDto(book);

        if (isAdmin)
        {
            // Collect all user IDs that need name resolution in one query
            var userIds = new HashSet<string>();
            if (!string.IsNullOrEmpty(book.ApplicationUserId))
                userIds.Add(book.ApplicationUserId);
            foreach (var note in book.Notes ?? [])
                if (!string.IsNullOrEmpty(note.ApplicationUserId))
                    userIds.Add(note.ApplicationUserId);
            foreach (var lr in book.LendingRecords ?? [])
                if (!string.IsNullOrEmpty(lr.ApplicationUserId))
                    userIds.Add(lr.ApplicationUserId);

            var userNames = await _dbContext.Users
                .Where(u => userIds.Contains(u.Id))
                .ToDictionaryAsync(u => u.Id, u => u.FullName ?? u.Email ?? "Unknown");

            // Submitter name
            if (!string.IsNullOrEmpty(book.ApplicationUserId) &&
                userNames.TryGetValue(book.ApplicationUserId, out var submitterName))
                dto.SubmittedByName = submitterName;

            // Note author names
            foreach (var (noteDto, noteEntity) in dto.Notes.Zip(book.Notes!))
            {
                if (!string.IsNullOrEmpty(noteEntity.ApplicationUserId) &&
                    userNames.TryGetValue(noteEntity.ApplicationUserId, out var authorName))
                    noteDto.AuthorName = authorName;
            }

            // Full lending history with lender names
            dto.LendingHistory = book.LendingRecords?
                .OrderByDescending(lr => lr.LendingDate)
                .Select(lr => new LendingRecordDto
                {
                    Id = lr.Id,
                    LenderUserId = lr.ApplicationUserId,
                    LenderName = !string.IsNullOrEmpty(lr.ApplicationUserId) && userNames.TryGetValue(lr.ApplicationUserId, out var lenderName)
                        ? lenderName : null,
                    BorrowerName = lr.BorrowerName,
                    LendingDate = lr.LendingDate,
                    ReturnDate = lr.ReturnDate,
                    IsReturned = lr.IsReturned
                })
                .ToList() ?? [];
        }

        return Result<BookDto>.Ok(dto);
    }

    public async Task<Result<IEnumerable<BookDto>>> GetUserBooksAsync(string userId)
    {
        // Show all approved books (shared catalog) + own pending submissions
        var books = await _dbContext.Books
            .Where(b => b.IsApproved || b.ApplicationUserId == userId)
            .Include(b => b.Genre)
            .Include(b => b.Format)
            .Include(b => b.LendingRecords)
            .Include(b => b.Notes)
            .OrderByDescending(b => b.DateAdded)
            .ToListAsync();
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
            .Where(b => (b.IsApproved || b.ApplicationUserId == userId)
                && (b.Title.Contains(searchTerm) || b.Author.Contains(searchTerm)))
            .Include(b => b.Genre)
            .Include(b => b.Format)
            .ToListAsync();

        return Result<IEnumerable<BookDto>>.Ok(books.Select(MapToDto));
    }

    public async Task<Result> SetReadingStatusAsync(int bookId, string userId, UpdateReadingStatusDto dto)
    {
        var book = await GetBookWithDetailsAsync(bookId);

        if (book == null)
            return Result.Fail("Book not found");

        string[] validStatuses = ["Want to Read", "Currently Reading", "Finished"];
        if (!validStatuses.Contains(dto.Status))
            return Result.Fail("Invalid reading status.");

        book.Status = dto.Status;
        book.CompletionDate = dto.CompletionDate;

        _dbContext.Books.Update(book);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result> LendBookAsync(int bookId, string userId, string borrowerName)
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
            BorrowerName = borrowerName,
            LendingDate = DateTime.UtcNow,
            IsReturned = false
        };

        _dbContext.LendingRecords.Add(lendingRecord);
        book.Status = "Lent Out";
        _dbContext.Books.Update(book);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result> ReturnBookAsync(int bookId, string userId, bool isAdmin = false)
    {
        var book = await GetBookWithDetailsAsync(bookId);

        if (book == null)
            return Result.Fail("Book not found");

        var activeLending = book.LendingRecords?.FirstOrDefault(lr => !lr.IsReturned);

        if (activeLending == null)
            return Result.Fail("Book is not currently lent out");

        if (!isAdmin && activeLending.ApplicationUserId != userId)
            return Result.Fail("You can only return books you lent.");

        activeLending.ReturnDate = DateTime.UtcNow;
        activeLending.IsReturned = true;
        book.Status = "Available";

        _dbContext.LendingRecords.Update(activeLending);
        _dbContext.Books.Update(book);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result<IEnumerable<BookDto>>> GetLentBooksAsync(string userId)
    {
        // Approved books where this user has an active lending record
        var books = await _dbContext.Books
            .Where(b => b.IsApproved && b.LendingRecords.Any(lr => lr.ApplicationUserId == userId && !lr.IsReturned))
            .Include(b => b.Genre)
            .Include(b => b.Format)
            .Include(b => b.LendingRecords)
            .Include(b => b.Notes)
            .OrderByDescending(b => b.DateAdded)
            .ToListAsync();
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
            Genre = book.Genre != null ? new BookGenreDto { Id = book.Genre.Id, Name = book.Genre.Name } : null,
            Format = book.Format != null ? new BookFormatDto { Id = book.Format.Id, Name = book.Format.Name } : null,
            Pages = book.Pages,
            CoverImageUrl = book.CoverImageUrl,
            DateAdded = book.DateAdded,
            IsApproved = book.IsApproved,
            ReadingStatus = new ReadingStatusDto
            {
                Status = book.Status,
                CompletionDate = book.CompletionDate,
                Rating = null
            },
            LendingRecord = activeLending != null ? new LendingRecordDto
            {
                Id = activeLending.Id,
                LenderUserId = activeLending.ApplicationUserId,
                BorrowerName = activeLending.BorrowerName,
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