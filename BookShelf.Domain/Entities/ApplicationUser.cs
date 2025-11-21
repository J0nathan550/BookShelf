using Microsoft.AspNetCore.Identity;

namespace BookShelf.Domain.Entities;

public class ApplicationUser : IdentityUser
{
    public string FullName { get; set; } = string.Empty;
    public DateTime RegistrationDate { get; set; } = DateTime.UtcNow;
    public bool IsActive { get; set; } = true;
    public ICollection<LendingRecord> LendingRecords { get; set; } = [];
    public ICollection<BookNote> BookNotes { get; set; } = [];
}