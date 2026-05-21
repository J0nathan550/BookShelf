using BookShelf.Application.DTOs;
using BookShelf.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookShelf.Server.Controllers;

[ApiController]
[Route("api/notifications")]
[Authorize]
public class NotificationsController : BaseController
{
    private readonly INotificationService _notificationService;

    public NotificationsController(INotificationService notificationService)
    {
        _notificationService = notificationService;
    }

    [HttpPost("register-token")]
    public async Task<IActionResult> RegisterToken([FromBody] RegisterFcmTokenDto dto)
    {
        if (string.IsNullOrWhiteSpace(dto.Token))
            return BadRequest("Token cannot be empty.");

        await _notificationService.RegisterTokenAsync(GetUserId(), dto.Token);
        return Ok();
    }
}
