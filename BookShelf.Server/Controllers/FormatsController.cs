using BookShelf.Infrastructure.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BookShelf.Server.Controllers;

[Route("api/[controller]")]
[ApiController]
[Authorize]
public class FormatsController : ControllerBase
{
    private readonly ApplicationDbContext _dbContext;

    public FormatsController(ApplicationDbContext dbContext)
    {
        _dbContext = dbContext;
    }

    [HttpGet]
    public async Task<IActionResult> GetFormats()
    {
        var formats = await _dbContext.BookFormats
            .Select(f => new { f.Id, f.Name })
            .ToListAsync();

        return Ok(formats);
    }
}