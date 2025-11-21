using BookShelf.Infrastructure.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BookShelf.Server.Controllers;

[Route("api/[controller]")]
[ApiController]
[Authorize]
public class GenresController : ControllerBase
{
    private readonly ApplicationDbContext _dbContext;

    public GenresController(ApplicationDbContext dbContext)
    {
        _dbContext = dbContext;
    }

    [HttpGet]
    public async Task<IActionResult> GetGenres()
    {
        var genres = await _dbContext.Genres
            .Select(g => new { g.Id, g.Name })
            .ToListAsync();

        return Ok(genres);
    }
}