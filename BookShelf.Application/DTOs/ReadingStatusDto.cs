namespace BookShelf.Application.DTOs;

public class ReadingStatusDto
{
    public string Status { get; set; } = string.Empty;
    public int? Rating { get; set; }
    public DateTime? CompletionDate { get; set; }
}