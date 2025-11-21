using System.ComponentModel.DataAnnotations;

namespace BookShelf.Server.DTOs;

public class ForgotPasswordDto
{
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;
}
