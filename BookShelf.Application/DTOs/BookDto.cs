namespace BookShelf.Application.DTOs;

public class BookDto
{
    public int Id { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Author { get; set; } = string.Empty;
    public string? Genre { get; set; }
    public string? Format { get; set; }
    public int? Pages { get; set; }
    public string? CoverImageUrl { get; set; }
    public DateTime DateAdded { get; set; }
    public ReadingStatusDto? ReadingStatus { get; set; }
    public LendingRecordDto? LendingRecord { get; set; }
    public List<BookNoteDto> Notes { get; set; } = [];
}