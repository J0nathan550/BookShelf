using BookShelf.Application.DTOs;
using BookShelf.Application.Results;

namespace BookShelf.Application.Services;

public interface IStatisticsService
{
    Task<Result<StatisticsDto>> GetUserStatisticsAsync(string userId);
}