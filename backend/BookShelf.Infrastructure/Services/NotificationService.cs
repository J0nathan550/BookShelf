using BookShelf.Application.Services;
using BookShelf.Infrastructure.Data;
using FirebaseAdmin;
using FirebaseAdmin.Messaging;
using Google.Apis.Auth.OAuth2;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace BookShelf.Infrastructure.Services;

public class NotificationService : INotificationService
{
    private readonly ApplicationDbContext _db;
    private readonly ILogger<NotificationService> _logger;

    private static bool _firebaseReady;
    private static bool _initAttempted;
    private static readonly object _initLock = new();

    public NotificationService(
        ApplicationDbContext db,
        IConfiguration config,
        ILogger<NotificationService> logger)
    {
        _db = db;
        _logger = logger;
        EnsureFirebaseInitialized(config["Firebase:ServiceAccountPath"]);
    }

    // ── Token registration ────────────────────────────────────────────────────

    public async Task RegisterTokenAsync(string userId, string fcmToken)
    {
        var user = await _db.Users.FindAsync(userId);
        if (user == null) return;
        user.FcmToken = fcmToken;
        await _db.SaveChangesAsync();
    }

    // ── Admin notifications ───────────────────────────────────────────────────

    public async Task NotifyAdminsBookLentAsync(int bookId, string borrowerName)
    {
        if (!_firebaseReady) return;
        var book = await _db.Books.FindAsync(bookId);
        if (book == null) return;
        await SendToAdminsAsync(
            "Book Borrowed",
            $"\"{book.Title}\" was borrowed by {borrowerName}.",
            new() { ["screen"] = "book_detail", ["bookId"] = bookId.ToString() });
    }

    public async Task NotifyAdminsBookReturnedAsync(int bookId)
    {
        if (!_firebaseReady) return;
        var book = await _db.Books.FindAsync(bookId);
        if (book == null) return;
        await SendToAdminsAsync(
            "Book Returned",
            $"\"{book.Title}\" has been returned.",
            new() { ["screen"] = "book_detail", ["bookId"] = bookId.ToString() });
    }

    public async Task NotifyAdminsNewSubmissionAsync(int bookId, string submitterName)
    {
        if (!_firebaseReady) return;
        var book = await _db.Books.FindAsync(bookId);
        if (book == null) return;
        await SendToAdminsAsync(
            "New Book Submission",
            $"{submitterName} submitted \"{book.Title}\" for approval.",
            new() { ["screen"] = "admin_panel" });
    }

    public async Task NotifyAdminsNoteAddedAsync(int bookId, string authorName)
    {
        if (!_firebaseReady) return;
        var book = await _db.Books.FindAsync(bookId);
        if (book == null) return;
        await SendToAdminsAsync(
            "Note Added",
            $"{authorName} added a note on \"{book.Title}\".",
            new() { ["screen"] = "book_detail", ["bookId"] = bookId.ToString() });
    }

    public async Task NotifyAdminsNoteUpdatedAsync(int noteId, string authorName)
    {
        if (!_firebaseReady) return;
        var note = await _db.BookNotes.FindAsync(noteId);
        if (note == null) return;
        var book = await _db.Books.FindAsync(note.BookId);
        if (book == null) return;
        await SendToAdminsAsync(
            "Note Updated",
            $"{authorName} edited a note on \"{book.Title}\".",
            new() { ["screen"] = "book_detail", ["bookId"] = book.Id.ToString() });
    }

    public async Task NotifyAdminsNewUserAsync(string userFullName)
    {
        if (!_firebaseReady) return;
        await SendToAdminsAsync(
            "New User Registered",
            $"{userFullName} just verified their account.",
            new() { ["screen"] = "admin_panel" });
    }

    // ── User notifications ────────────────────────────────────────────────────

    public async Task NotifyUserSubmissionApprovedAsync(int bookId)
    {
        if (!_firebaseReady) return;
        var book = await _db.Books
            .Include(b => b.ApplicationUser)
            .FirstOrDefaultAsync(b => b.Id == bookId);
        var token = book?.ApplicationUser?.FcmToken;
        if (token == null) return;
        await TrySendAsync(token,
            "Submission Approved",
            $"Your book \"{book!.Title}\" was approved and is now in the library.",
            new() { ["screen"] = "book_detail", ["bookId"] = bookId.ToString() });
    }

    public async Task NotifyUserSubmissionRejectedAsync(int bookId)
    {
        if (!_firebaseReady) return;
        var book = await _db.Books
            .Include(b => b.ApplicationUser)
            .FirstOrDefaultAsync(b => b.Id == bookId);
        var token = book?.ApplicationUser?.FcmToken;
        if (token == null) return;
        await TrySendAsync(token,
            "Submission Rejected",
            $"Your submission \"{book!.Title}\" was not approved by an admin.",
            new() { ["screen"] = "book_list" });
    }

    public async Task NotifyUserAccountDisabledAsync(string userId)
    {
        if (!_firebaseReady) return;
        var user = await _db.Users.FindAsync(userId);
        if (user?.FcmToken == null) return;
        await TrySendAsync(user.FcmToken,
            "Account Suspended",
            "Your BookShelf account has been disabled by an administrator.",
            new() { ["screen"] = "none" });
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private async Task SendToAdminsAsync(string title, string body, Dictionary<string, string> data)
    {
        var tokens = await GetAdminFcmTokensAsync();
        if (tokens.Count == 0) return;
        try
        {
            var multicast = new MulticastMessage
            {
                Notification = new Notification { Title = title, Body = body },
                Data = data,
                Tokens = tokens,
            };
            await FirebaseMessaging.DefaultInstance.SendEachForMulticastAsync(multicast);
        }
        catch (Exception ex)
        {
            _logger.LogWarning("FCM multicast failed: {Message}", ex.Message);
        }
    }

    private async Task TrySendAsync(string token, string title, string body, Dictionary<string, string> data)
    {
        try
        {
            var message = new Message
            {
                Notification = new Notification { Title = title, Body = body },
                Data = data,
                Token = token,
            };
            await FirebaseMessaging.DefaultInstance.SendAsync(message);
        }
        catch (Exception ex)
        {
            _logger.LogWarning("FCM send failed: {Message}", ex.Message);
        }
    }

    private async Task<List<string>> GetAdminFcmTokensAsync()
    {
        var adminRole = await _db.Roles.FirstOrDefaultAsync(r => r.Name == "Admin");
        if (adminRole == null) return [];
        var adminUserIds = await _db.UserRoles
            .Where(ur => ur.RoleId == adminRole.Id)
            .Select(ur => ur.UserId)
            .ToListAsync();
        return await _db.Users
            .Where(u => adminUserIds.Contains(u.Id) && u.FcmToken != null)
            .Select(u => u.FcmToken!)
            .ToListAsync();
    }

    private void EnsureFirebaseInitialized(string? serviceAccountPath)
    {
        if (_firebaseReady || _initAttempted) return;
        lock (_initLock)
        {
            if (_firebaseReady || _initAttempted) return;
            _initAttempted = true;
            if (string.IsNullOrWhiteSpace(serviceAccountPath) || !File.Exists(serviceAccountPath))
            {
                _logger.LogWarning("Firebase service account not configured — push notifications disabled.");
                return;
            }
            try
            {
                if (FirebaseApp.DefaultInstance == null)
                    FirebaseApp.Create(new AppOptions { Credential = GoogleCredential.FromFile(serviceAccountPath) });
                _firebaseReady = true;
                _logger.LogInformation("Firebase Admin SDK initialized.");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to initialize Firebase Admin SDK.");
            }
        }
    }
}
