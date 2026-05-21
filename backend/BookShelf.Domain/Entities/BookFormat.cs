namespace BookShelf.Domain.Entities;

public class BookFormat
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public ICollection<Book> Books { get; set; } = [];
}