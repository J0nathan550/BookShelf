namespace BookShelf.Application.DTOs;

public class IsbnLookupDto
{
    public string Title { get; set; } = string.Empty;
    public string Author { get; set; } = string.Empty;
    public int? Pages { get; set; }
    public string? CoverImageUrl { get; set; }
}
