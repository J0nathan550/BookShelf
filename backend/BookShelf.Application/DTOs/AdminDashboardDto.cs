namespace BookShelf.Application.DTOs;

public class AdminDashboardDto
{
    public int TotalUsers { get; set; }
    public int TotalBooks { get; set; }
    public int PendingBooksCount { get; set; }
    public List<RecentUserDto> RecentRegistrations { get; set; } = [];
}