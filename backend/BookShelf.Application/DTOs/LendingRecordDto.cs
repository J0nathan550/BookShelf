namespace BookShelf.Application.DTOs;

public class LendingRecordDto
{
    public int Id { get; set; }
    public string LenderUserId { get; set; } = string.Empty;
    public string? LenderName { get; set; }
    public string BorrowerName { get; set; } = string.Empty;
    public DateTime LendingDate { get; set; }
    public DateTime? ReturnDate { get; set; }
    public bool IsReturned { get; set; }
}