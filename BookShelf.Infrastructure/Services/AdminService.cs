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
        var totalBooks = await _dbContext.Books.CountAsync();

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
}