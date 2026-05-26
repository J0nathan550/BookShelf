namespace BookShelf.Application.DTOs;

public class ImportDataDto
{
    public List<UserListDto> Users { get; set; } = [];
    public List<BookDto> Books { get; set; } = [];
}

public class ImportResultDto
{
    public int UsersImported { get; set; }
    public int BooksImported { get; set; }
    public int NotesImported { get; set; }
}
