namespace BookShelf.Application.Services;

public interface INotificationService
{
    // Token registration
    Task RegisterTokenAsync(string userId, string fcmToken);

    // Admin notifications — all active admins receive these
    Task NotifyAdminsBookLentAsync(int bookId, string borrowerName);
    Task NotifyAdminsBookReturnedAsync(int bookId);
    Task NotifyAdminsNewSubmissionAsync(int bookId, string submitterName);
    Task NotifyAdminsNoteAddedAsync(int bookId, string authorName);
    Task NotifyAdminsNoteUpdatedAsync(int noteId, string authorName);
    Task NotifyAdminsNewUserAsync(string userFullName);

    // User notifications — only the affected user receives these
    Task NotifyUserSubmissionApprovedAsync(int bookId);
    Task NotifyUserSubmissionRejectedAsync(int bookId);   // call BEFORE the book is deleted
    Task NotifyUserAccountDisabledAsync(string userId);
}
