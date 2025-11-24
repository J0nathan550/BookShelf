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

    public IotController(IBookService bookService)
    {
        _bookService = bookService;
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
            result = await _bookService.LendBookAsync(dto.BookId, dto.UserId, DateTime.UtcNow);
        }
        else if (dto.Action.Equals("return", StringComparison.CurrentCultureIgnoreCase))
        {
            result = await _bookService.ReturnBookAsync(dto.BookId, dto.UserId, DateTime.UtcNow);
        }
        else
        {
            return BadRequest("Unknown Action");
        }

        if (result.IsSuccess)
        {
            return Ok(new { status = "success", message = "Operation completed", bookId = dto.BookId });
        }
        else
        {
            return BadRequest(new { status = "error", errors = result.Errors });
        }
    }
}