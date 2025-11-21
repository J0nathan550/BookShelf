namespace BookShelf.Application.DTOs;

public class BookNoteDto
{
    public int Id { get; set; }
    public string NoteText { get; set; } = string.Empty;
    public DateTime CreatedDate { get; set; }
    public DateTime? ModifiedDate { get; set; }
}