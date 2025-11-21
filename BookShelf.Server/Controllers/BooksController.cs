using BookShelf.Application.DTOs;
using BookShelf.Application.Results;
using BookShelf.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookShelf.Server.Controllers;

[Route("api/[controller]")]
[ApiController]
[Authorize]
public class BooksController : BaseController
{
    private readonly IBookService _bookService;

    public BooksController(IBookService bookService)
    {
        _bookService = bookService;
    }

    [HttpPost]
    [ProducesResponseType(typeof(BookDto), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> CreateBook([FromBody] CreateBookDto dto)
    {
        // Validate foreign keys
        var genreExists = await _bookService.GenreExistsAsync(dto.GenreId);
        var formatExists = await _bookService.FormatExistsAsync(dto.FormatId);

        if (!genreExists)
            return BadRequest(new[] { new Error("Invalid GenreId.") });

        if (!formatExists)
            return BadRequest(new[] { new Error("Invalid FormatId.") });

        var result = await _bookService.CreateBookAsync(dto, GetUserId());

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return CreatedAtAction(nameof(GetBook), new { bookId = result.Value.Id }, result.Value);
    }

    [HttpGet("{bookId}")]
    [ProducesResponseType(typeof(BookDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> GetBook(int bookId)
    {
        var result = await _bookService.GetBookAsync(bookId, GetUserId());

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
    public async Task<IActionResult> SetReadingStatus(
        int bookId,
        [FromQuery] string status,
        [FromQuery] DateTime? completionDate = null)
    {
        var result = await _bookService.SetReadingStatusAsync(bookId, GetUserId(), status, completionDate);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpPost("{bookId}/lend")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> LendBook(
        int bookId,
        [FromQuery] DateTime lendingDate)
    {
        var result = await _bookService.LendBookAsync(bookId, GetUserId(), lendingDate);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpPut("{bookId}/return")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> ReturnBook(int bookId, [FromQuery] DateTime returnDate)
    {
        var result = await _bookService.ReturnBookAsync(bookId, GetUserId(), returnDate);

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
    public async Task<IActionResult> AddNote(int bookId, [FromBody] string noteText)
    {
        var result = await _bookService.AddNoteAsync(bookId, GetUserId(), noteText);

        if (!result.IsSuccess)
            return BadRequest(result.Errors);

        return NoContent();
    }

    [HttpPut("notes/{noteId}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(IEnumerable<IError>), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> UpdateNote(int noteId, [FromBody] string noteText)
    {
        var result = await _bookService.UpdateNoteAsync(noteId, GetUserId(), noteText);

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