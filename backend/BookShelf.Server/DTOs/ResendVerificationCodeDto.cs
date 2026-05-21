using System.ComponentModel.DataAnnotations;

namespace BookShelf.Server.DTOs;

public class ResendVerificationCodeDto
{
    [Required]
    public string UserId { get; set; } = string.Empty;
}
