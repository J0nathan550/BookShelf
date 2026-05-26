using Microsoft.AspNetCore.Identity;

namespace BookShelf.Domain.Entities;

public class ApplicationUser : IdentityUser
{
    public string FullName { get; set; } = string.Empty;
    public DateTime RegistrationDate { get; set; } = DateTime.UtcNow;
    public bool IsActive { get; set; } = true;
    public string? EmailVerificationCode { get; set; }
    public DateTime? EmailVerificationCodeExpiry { get; set; }
    public string? PasswordResetCode { get; set; }
    public DateTime? PasswordResetCodeExpiry { get; set; }
    public string? FcmToken { get; set; }
    public ICollection<LendingRecord> LendingRecords { get; set; } = [];
    public ICollection<BookNote> BookNotes { get; set; } = [];
    public ICollection<Book> Books { get; set; } = [];
    public ICollection<ReadingStatus> ReadingStatuses { get; set; } = [];
}