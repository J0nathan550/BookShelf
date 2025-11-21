namespace BookShelf.Domain.Entities;

public class Book
{
    public int Id { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Author { get; set; } = string.Empty;
    public int? GenreId { get; set; }
    public Genre? Genre { get; set; }
    public int? FormatId { get; set; }
    public BookFormat? Format { get; set; }
    public int? Pages { get; set; }
    public string? CoverImageUrl { get; set; }
    public DateTime DateAdded { get; set; } = DateTime.UtcNow;
    public string Status { get; set; } = "Available";
    public DateTime? CompletionDate { get; set; }
    public ICollection<LendingRecord> LendingRecords { get; set; } = [];
    public ICollection<BookNote> Notes { get; set; } = [];
}