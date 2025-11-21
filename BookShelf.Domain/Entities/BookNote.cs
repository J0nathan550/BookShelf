namespace BookShelf.Domain.Entities;

public class BookNote
{
    public int Id { get; set; }
    public int BookId { get; set; }
    public Book Book { get; set; } = default!;
    public string ApplicationUserId { get; set; } = string.Empty;
    public ApplicationUser ApplicationUser { get; set; } = default!;
    public string NoteText { get; set; } = string.Empty;
    public DateTime CreatedDate { get; set; } = DateTime.UtcNow;
    public DateTime? ModifiedDate { get; set; }
}