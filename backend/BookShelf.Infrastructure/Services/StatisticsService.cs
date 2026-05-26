using BookShelf.Application.DTOs;
using BookShelf.Application.Results;
using BookShelf.Application.Services;
using BookShelf.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace BookShelf.Infrastructure.Services;

public class StatisticsService : IStatisticsService
{
    private readonly ApplicationDbContext _dbContext;

    public StatisticsService(ApplicationDbContext dbContext)
    {
        _dbContext = dbContext;
    }

    public async Task<Result<StatisticsDto>> GetUserStatisticsAsync(string userId)
    {
        var totalBooks = await _dbContext.Books
            .CountAsync(b => b.IsApproved || b.ApplicationUserId == userId);

        var readingStatuses = await _dbContext.ReadingStatuses
            .Where(rs => rs.ApplicationUserId == userId)
            .Include(rs => rs.Book)
            .ThenInclude(b => b.Genre)
            .ToListAsync();

        var currentYear = DateTime.UtcNow.Year;

        var statistics = new StatisticsDto
        {
            TotalBooks = totalBooks,
            WantToRead = readingStatuses.Count(rs => rs.Status == "Want to Read"),
            CurrentlyReading = readingStatuses.Count(rs => rs.Status == "Currently Reading"),
            Finished = readingStatuses.Count(rs => rs.Status == "Finished"),
            BooksReadThisYear = readingStatuses.Count(rs =>
                rs.Status == "Finished" &&
                rs.CompletionDate.HasValue &&
                rs.CompletionDate.Value.Year == currentYear),
            GenreDistribution = readingStatuses
                .Where(rs => rs.Book?.Genre != null)
                .GroupBy(rs => rs.Book!.Genre!.Name)
                .ToDictionary(g => g.Key, g => g.Count())
        };

        return Result<StatisticsDto>.Ok(statistics);
    }
}