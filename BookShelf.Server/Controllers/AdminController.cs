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

    public AdminController(IAdminService adminService)
    {
        _adminService = adminService;
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
}