namespace BookShelf.Application.Services;

public interface IEmailService
{
    Task SendEmailVerificationCodeAsync(string email, string fullName, string code);
    Task SendPasswordResetEmailAsync(string email, string fullName, string resetCode);
    Task SendWelcomeEmailAsync(string email, string fullName);
}
