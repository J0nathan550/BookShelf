namespace BookShelf.Application.DTOs;

public class StatisticsDto
{
    public int TotalBooks { get; set; }
    public int WantToRead { get; set; }
    public int CurrentlyReading { get; set; }
    public int Finished { get; set; }
    public int BooksReadThisYear { get; set; }
    public Dictionary<string, int> GenreDistribution { get; set; } = [];
}