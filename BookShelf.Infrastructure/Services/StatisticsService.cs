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
        var lendingHistory = await _dbContext.LendingRecords
            .Where(lr => lr.ApplicationUserId == userId)
            .Include(lr => lr.Book)
            .ThenInclude(b => b.Genre)
            .ToListAsync();

        var currentYear = DateTime.UtcNow.Year;

        var statistics = new StatisticsDto
        {
            TotalBooks = lendingHistory.Count,
            WantToRead = 0,
            CurrentlyReading = lendingHistory.Count(lr => !lr.IsReturned),
            Finished = lendingHistory.Count(lr => lr.IsReturned),
            BooksReadThisYear = lendingHistory.Count(lr =>
                lr.IsReturned &&
                lr.ReturnDate.HasValue &&
                lr.ReturnDate.Value.Year == currentYear),
            GenreDistribution = lendingHistory
                .Select(lr => lr.Book)
                .Where(b => b.Genre != null)
                .GroupBy(b => b.Genre!.Name)
                .ToDictionary(g => g.Key, g => g.Count())
        };

        return Result<StatisticsDto>.Ok(statistics);
    }
}