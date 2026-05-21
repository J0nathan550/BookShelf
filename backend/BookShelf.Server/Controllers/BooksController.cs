using BookShelf.Application.DTOs;
using BookShelf.Application.Results;
using BookShelf.Application.Services;
using BookShelf.Domain.Entities;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;

namespace BookShelf.Server.Controllers;

[Route("api/[controller]")]
[ApiController]
[Authorize]
public class BooksController : BaseController
{
    private readonly IBookService _bookService;
    private readonly UserManager<ApplicationUser> _userManager;

    public BooksController(IBookService bookService, UserManager<ApplicationUser> userManager)
    {
        _bookService = bookService;
        _userManager = userManager;
    }

    [HttpPost]
    [ProducesResponseType(typeof(BookDto), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> CreateBook([FromBody] CreateBookDto dto)
    {
        var genreExists = await _bookService.GenreExistsAsync(dto.GenreId);
        var formatExists = await _bookService.FormatExistsAsync(dto.FormatId);

        if (!genreExists)
            return BadRequest(new[] { new Error("Invalid GenreId.") });

        if (!formatExists)
            return BadRequest(new[] { new Error("Invalid FormatId.") });

        var result = await _bookService.CreateBookAsync(dto, GetUserId());

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return CreatedAtAction(nameof(GetBook), new { bookId = result.Value!.Id }, result.Value);
    }

    [HttpGet("{bookId}")]
    [ProducesResponseType(typeof(BookDto), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetBook(int bookId)
    {
        var isAdmin = User.IsInRole("Admin");
        var result = await _bookService.GetBookAsync(bookId, GetUserId(), isAdmin);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return Ok(result.Value);
    }

    [HttpGet]
    [ProducesResponseType(typeof(IEnumerable<BookDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetUserBooks()
    {
        var result = await _bookService.GetUserBooksAsync(GetUserId());
        return Ok(result.Value);
    }

    [HttpPut("{bookId}")]
    [ProducesResponseType(typeof(BookDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> UpdateBook(int bookId, [FromBody] UpdateBookDto dto)
    {
        var result = await _bookService.UpdateBookAsync(bookId, dto, GetUserId());

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return Ok(result.Value);
    }

    [HttpDelete("{bookId}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> DeleteBook(int bookId)
    {
        var result = await _bookService.DeleteBookAsync(bookId, GetUserId());

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpGet("isbn/{isbn}")]
    [ProducesResponseType(typeof(IsbnLookupDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> LookupIsbn(string isbn)
    {
        var result = await _bookService.LookupIsbnAsync(isbn);
        if (!result.IsSuccess)
            return NotFound(new { message = result.Errors.FirstOrDefault()?.Message });
        return Ok(result.Value);
    }

    [HttpGet("search")]
    [ProducesResponseType(typeof(IEnumerable<BookDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> SearchBooks([FromQuery] string searchTerm)
    {
        var result = await _bookService.SearchBooksAsync(GetUserId(), searchTerm);
        return Ok(result.Value);
    }

    [HttpPut("{bookId}/reading-status")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> SetReadingStatus(int bookId, [FromBody] UpdateReadingStatusDto dto)
    {
        var result = await _bookService.SetReadingStatusAsync(bookId, GetUserId(), dto);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpPost("{bookId}/lend")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> LendBook(int bookId)
    {
        var user = await _userManager.FindByIdAsync(GetUserId());
        var borrowerName = user?.FullName ?? user?.Email ?? GetUserId();

        var result = await _bookService.LendBookAsync(bookId, GetUserId(), borrowerName);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpPut("{bookId}/return")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> ReturnBook(int bookId)
    {
        var isAdmin = User.IsInRole("Admin");
        var result = await _bookService.ReturnBookAsync(bookId, GetUserId(), isAdmin);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpGet("lent")]
    [ProducesResponseType(typeof(IEnumerable<BookDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetLentBooks()
    {
        var result = await _bookService.GetLentBooksAsync(GetUserId());
        return Ok(result.Value);
    }

    [HttpPost("{bookId}/notes")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> AddNote(int bookId, [FromBody] NoteTextDto dto)
    {
        var result = await _bookService.AddNoteAsync(bookId, GetUserId(), dto.NoteText);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpPut("notes/{noteId}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> UpdateNote(int noteId, [FromBody] NoteTextDto dto)
    {
        var result = await _bookService.UpdateNoteAsync(noteId, GetUserId(), dto.NoteText);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpDelete("notes/{noteId}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> DeleteNote(int noteId)
    {
        var result = await _bookService.DeleteNoteAsync(noteId, GetUserId());

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }
}
