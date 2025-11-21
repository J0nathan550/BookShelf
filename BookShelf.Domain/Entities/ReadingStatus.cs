namespace BookShelf.Domain.Entities;

public class ReadingStatus
{
    public int Id { get; set; }
    public int BookId { get; set; }
    public Book Book { get; set; } = default!;
    public string Status { get; set; } = "Want to Read";
    public int? Rating { get; set; }
    public DateTime? CompletionDate { get; set; }
    public DateTime LastUpdated { get; set; } = DateTime.UtcNow;
}