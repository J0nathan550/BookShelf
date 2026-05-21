namespace BookShelf.Application.DTOs;

public class UserListDto
{
    public string Id { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public DateTime RegistrationDate { get; set; }
    public int BookCount { get; set; }
    public bool IsActive { get; set; }
}