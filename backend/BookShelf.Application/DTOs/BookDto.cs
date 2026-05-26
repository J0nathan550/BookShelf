namespace BookShelf.Application.DTOs;

public class BookDto
{
    public int Id { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Author { get; set; } = string.Empty;
    public BookGenreDto? Genre { get; set; }
    public BookFormatDto? Format { get; set; }
    public int? Pages { get; set; }
    public string? CoverImageUrl { get; set; }
    public DateTime DateAdded { get; set; }
    public bool IsApproved { get; set; }
    public ReadingStatusDto? ReadingStatus { get; set; }
    public double? AverageRating { get; set; }
    public LendingRecordDto? LendingRecord { get; set; }
    public List<BookNoteDto> Notes { get; set; } = [];
    public string? SubmittedByName { get; set; }
    public List<LendingRecordDto> LendingHistory { get; set; } = [];
}

public class BookGenreDto
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
}

public class BookFormatDto
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
}