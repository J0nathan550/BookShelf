using BookShelf.Application.DTOs;
using BookShelf.Application.Results;
using BookShelf.Application.Services;
using BookShelf.Domain.Entities;
using BookShelf.Infrastructure.Data;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;

namespace BookShelf.Infrastructure.Services;

public class AdminService : IAdminService
{
    private readonly ApplicationDbContext _dbContext;
    private readonly UserManager<ApplicationUser> _userManager;

    public AdminService(ApplicationDbContext dbContext, UserManager<ApplicationUser> userManager)
    {
        _dbContext = dbContext;
        _userManager = userManager;
    }

    public async Task<Result<AdminDashboardDto>> GetDashboardAsync()
    {
        var totalUsers = await _dbContext.Users.CountAsync();
        var totalBooks = await _dbContext.Books.CountAsync(b => b.IsApproved);
        var pendingBooksCount = await _dbContext.Books.CountAsync(b => !b.IsApproved);

        var recentUsers = await _dbContext.Users
            .OrderByDescending(u => u.RegistrationDate)
            .Take(10)
            .Select(u => new RecentUserDto
            {
                Id = u.Id.ToString(),
                Email = u.Email ?? "",
                FullName = u.FullName,
                RegistrationDate = u.RegistrationDate
            })
            .ToListAsync();

        var dashboard = new AdminDashboardDto
        {
            TotalUsers = totalUsers,
            TotalBooks = totalBooks,
            PendingBooksCount = pendingBooksCount,
            RecentRegistrations = recentUsers
        };

        return Result<AdminDashboardDto>.Ok(dashboard);
    }

    public async Task<Result<IEnumerable<UserListDto>>> GetAllUsersAsync()
    {
        var users = await _dbContext.Users
            .Select(u => new UserListDto
            {
                Id = u.Id.ToString(),
                Email = u.Email ?? "",
                FullName = u.FullName,
                RegistrationDate = u.RegistrationDate,
                BookCount = u.LendingRecords.Count(lr => !lr.IsReturned),
                IsActive = u.IsActive
            })
            .ToListAsync();

        return Result<IEnumerable<UserListDto>>.Ok(users);
    }

    public async Task<Result> DisableUserAsync(string userId)
    {
        var user = await _userManager.FindByIdAsync(userId);

        if (user == null)
            return Result.Fail("User not found");

        user.IsActive = false;
        var result = await _userManager.UpdateAsync(user);

        if (!result.Succeeded)
            return Result.Fail(string.Join(", ", result.Errors.Select(e => e.Description)));

        return Result.Ok();
    }

    public async Task<Result> EnableUserAsync(string userId)
    {
        var user = await _userManager.FindByIdAsync(userId);

        if (user == null)
            return Result.Fail("User not found");

        user.IsActive = true;
        var result = await _userManager.UpdateAsync(user);

        if (!result.Succeeded)
            return Result.Fail(string.Join(", ", result.Errors.Select(e => e.Description)));

        return Result.Ok();
    }

    public async Task<Result> DeleteUserAsync(string userId)
    {
        var user = await _userManager.FindByIdAsync(userId);

        if (user == null)
            return Result.Fail("User not found");

        var result = await _userManager.DeleteAsync(user);

        if (!result.Succeeded)
            return Result.Fail(string.Join(", ", result.Errors.Select(e => e.Description)));

        return Result.Ok();
    }

    public async Task<Result<IEnumerable<BookDto>>> GetPendingBooksAsync()
    {
        var books = await _dbContext.Books
            .Where(b => !b.IsApproved)
            .Include(b => b.Genre)
            .Include(b => b.Format)
            .Include(b => b.LendingRecords)
            .Include(b => b.Notes)
            .OrderByDescending(b => b.DateAdded)
            .ToListAsync();

        return Result<IEnumerable<BookDto>>.Ok(books.Select(MapBookToDto));
    }

    public async Task<Result> ApproveBookAsync(int bookId)
    {
        var book = await _dbContext.Books.FindAsync(bookId);

        if (book == null)
            return Result.Fail("Book not found");

        book.IsApproved = true;
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result> RejectBookAsync(int bookId)
    {
        var book = await _dbContext.Books.FindAsync(bookId);

        if (book == null)
            return Result.Fail("Book not found");

        _dbContext.Books.Remove(book);
        await _dbContext.SaveChangesAsync();

        return Result.Ok();
    }

    public async Task<Result<IEnumerable<BookDto>>> GetAllBooksAsync()
    {
        var books = await _dbContext.Books
            .Include(b => b.Genre)
            .Include(b => b.Format)
            .Include(b => b.LendingRecords)
            .Include(b => b.Notes)
            .OrderByDescending(b => b.DateAdded)
            .ToListAsync();

        return Result<IEnumerable<BookDto>>.Ok(books.Select(MapBookToDto));
    }

    private static BookDto MapBookToDto(Book book)
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
            ReadingStatus = new ReadingStatusDto { Status = book.Status, CompletionDate = book.CompletionDate, Rating = null },
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
                ? [.. book.Notes.Select(n => new BookNoteDto { Id = n.Id, NoteText = n.NoteText, CreatedDate = n.CreatedDate, ModifiedDate = n.ModifiedDate })]
                : []
        };
    }
}