using BookShelf.Application.DTOs;
using BookShelf.Application.Results;

namespace BookShelf.Application.Services;

public interface IAdminService
{
    Task<Result<AdminDashboardDto>> GetDashboardAsync();
    Task<Result<IEnumerable<UserListDto>>> GetAllUsersAsync();
    Task<Result> DisableUserAsync(string userId);
    Task<Result> EnableUserAsync(string userId);
    Task<Result> DeleteUserAsync(string userId);
}