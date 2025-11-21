namespace BookShelf.Domain.Entities;

public class LendingRecord
{
    public int Id { get; set; }

    public int BookId { get; set; }
    public Book Book { get; set; } = default!;
    public string ApplicationUserId { get; set; } = string.Empty;
    public ApplicationUser ApplicationUser { get; set; } = default!;
    public DateTime LendingDate { get; set; } = DateTime.UtcNow;
    public DateTime? ReturnDate { get; set; }
    public bool IsReturned { get; set; } = false;
}