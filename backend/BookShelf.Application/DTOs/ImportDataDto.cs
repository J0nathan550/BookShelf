namespace BookShelf.Application.DTOs;

public class ImportDataDto
{
    public List<BookDto> Books { get; set; } = [];
}

public class ImportResultDto
{
    public int BooksImported { get; set; }
    public int NotesImported { get; set; }
}
