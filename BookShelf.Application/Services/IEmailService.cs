namespace BookShelf.Application.Services;

public interface IEmailService
{
    Task SendEmailVerificationAsync(string email, string fullName, string verificationLink);
    Task SendPasswordResetEmailAsync(string email, string fullName, string resetLink);
    Task SendWelcomeEmailAsync(string email, string fullName);
}