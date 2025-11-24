namespace BookShelf.Application.DTOs;

public class IotScanDto
{
    public int BookId { get; set; }
    public string UserId { get; set; } = "";
    public string Action { get; set; } = "";
}