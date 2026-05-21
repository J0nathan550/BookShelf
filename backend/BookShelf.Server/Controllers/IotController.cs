using BookShelf.Application.DTOs;
using BookShelf.Application.Results;
using BookShelf.Application.Services;
using Microsoft.AspNetCore.Mvc;

namespace BookShelf.Server.Controllers;

[ApiController]
[Route("api/iot")]
public class IotController : ControllerBase
{
    private readonly IBookService _bookService;
    private readonly INotificationService _notificationService;

    public IotController(IBookService bookService, INotificationService notificationService)
    {
        _bookService = bookService;
        _notificationService = notificationService;
    }

    [HttpPost("scan")]
    public async Task<IActionResult> ProcessScan([FromBody] IotScanDto dto)
    {
        if (dto.BookId <= 0 || string.IsNullOrEmpty(dto.UserId))
        {
            return BadRequest("Invalid Scan Data");
        }

        Result result;

        if (dto.Action.Equals("lend", StringComparison.CurrentCultureIgnoreCase))
        {
            result = await _bookService.LendBookAsync(dto.BookId, dto.UserId, "IoT Device");
        }
        else if (dto.Action.Equals("return", StringComparison.CurrentCultureIgnoreCase))
        {
            result = await _bookService.ReturnBookAsync(dto.BookId, dto.UserId);
        }
        else
        {
            return BadRequest("Unknown Action");
        }

        if (result.IsSuccess)
        {
            if (dto.Action.Equals("lend", StringComparison.OrdinalIgnoreCase))
                await _notificationService.NotifyAdminsBookLentAsync(dto.BookId, "IoT Device");
            else
                await _notificationService.NotifyAdminsBookReturnedAsync(dto.BookId);

            return Ok(new { status = "success", message = "Operation completed", bookId = dto.BookId });
        }
        else
        {
            return BadRequest(new { status = "error", errors = result.Errors });
        }
    }
}