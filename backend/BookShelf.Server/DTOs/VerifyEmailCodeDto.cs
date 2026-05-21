using System.ComponentModel.DataAnnotations;

namespace BookShelf.Server.DTOs;

public class VerifyEmailCodeDto
{
    [Required]
    public string UserId { get; set; } = string.Empty;

    [Required]
    [StringLength(6, MinimumLength = 6)]
    public string Code { get; set; } = string.Empty;
}
