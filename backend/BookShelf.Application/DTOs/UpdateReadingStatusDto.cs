namespace BookShelf.Application.DTOs;

public class UpdateReadingStatusDto
{
    public string Status { get; set; } = string.Empty;
    public int? Rating { get; set; }
    public DateTime? CompletionDate { get; set; }
}
