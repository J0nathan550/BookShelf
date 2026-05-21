using BookShelf.Application.DTOs;
using BookShelf.Application.Results;
using BookShelf.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookShelf.Server.Controllers;

[Route("api/[controller]")]
[ApiController]
[Authorize(Roles = "Admin")]
public class AdminController : BaseController
{
    private readonly IAdminService _adminService;
    private readonly INotificationService _notificationService;

    public AdminController(IAdminService adminService, INotificationService notificationService)
    {
        _adminService = adminService;
        _notificationService = notificationService;
    }

    [HttpGet("dashboard")]
    [ProducesResponseType(typeof(AdminDashboardDto), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetDashboard()
    {
        var result = await _adminService.GetDashboardAsync();
        return Ok(result.Value);
    }

    [HttpGet("users")]
    [ProducesResponseType(typeof(IEnumerable<UserListDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetAllUsers()
    {
        var result = await _adminService.GetAllUsersAsync();
        return Ok(result.Value);
    }

    [HttpPut("users/{userId}/disable")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> DisableUser(string userId)
    {
        var result = await _adminService.DisableUserAsync(userId);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        await _notificationService.NotifyUserAccountDisabledAsync(userId);
        return NoContent();
    }

    [HttpPut("users/{userId}/enable")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> EnableUser(string userId)
    {
        var result = await _adminService.EnableUserAsync(userId);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpDelete("users/{userId}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> DeleteUser(string userId)
    {
        var result = await _adminService.DeleteUserAsync(userId);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpGet("books/pending")]
    [ProducesResponseType(typeof(IEnumerable<BookDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetPendingBooks()
    {
        var result = await _adminService.GetPendingBooksAsync();
        return Ok(result.Value);
    }

    [HttpGet("books/all")]
    [ProducesResponseType(typeof(IEnumerable<BookDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetAllBooks()
    {
        var result = await _adminService.GetAllBooksAsync();
        return Ok(result.Value);
    }

    [HttpPut("books/{bookId}/approve")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> ApproveBook(int bookId)
    {
        var result = await _adminService.ApproveBookAsync(bookId);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        await _notificationService.NotifyUserSubmissionApprovedAsync(bookId);
        return NoContent();
    }

    [HttpDelete("books/{bookId}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> RejectBook(int bookId)
    {
        // Notify before deletion so the service can still look up the book and its owner.
        await _notificationService.NotifyUserSubmissionRejectedAsync(bookId);

        var result = await _adminService.RejectBookAsync(bookId);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }
}