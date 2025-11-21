namespace BookShelf.Application.DTOs;

public class UpdateBookDto
{
    public string Title { get; set; } = string.Empty;
    public string Author { get; set; } = string.Empty;
    public int? GenreId { get; set; }
    public int? FormatId { get; set; }
    public int? Pages { get; set; }
    public string? CoverImageUrl { get; set; }
}