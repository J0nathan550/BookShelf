using BookShelf.Application.Services;
using Microsoft.Extensions.Configuration;
using System.Net;
using System.Net.Mail;

namespace BookShelf.Infrastructure.Services;

public class EmailService : IEmailService
{
    private readonly string _smtpServer;
    private readonly int _smtpPort;
    private readonly string _smtpUsername;
    private readonly string _smtpPassword;
    private readonly string _fromEmail;
    private readonly string _fromName;

    public EmailService(IConfiguration configuration)
    {
        _smtpServer = configuration["Email:SmtpServer"] ?? "smtp.gmail.com";
        _smtpPort = int.Parse(configuration["Email:SmtpPort"] ?? "587");
        _smtpUsername = configuration["Email:Username"] ?? "";
        _smtpPassword = configuration["Email:Password"] ?? "";
        _fromEmail = configuration["Email:FromEmail"] ?? "";
        _fromName = configuration["Email:FromName"] ?? "BookShelf";
    }

    public async Task SendEmailVerificationCodeAsync(string email, string fullName, string code)
    {
        var subject = "Your BookShelf Verification Code";
        var body = $@"
            <html>
            <body style='font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto;'>
                <h2 style='color: #4CAF50;'>Welcome to BookShelf, {fullName}!</h2>
                <p>Use the verification code below in the app to activate your account:</p>
                <div style='text-align: center; margin: 32px 0;'>
                    <span style='font-size: 48px; font-family: monospace; letter-spacing: 12px;
                                 font-weight: bold; color: #212121; background: #F5F5F5;
                                 padding: 16px 24px; border-radius: 8px; display: inline-block;'>
                        {code}
                    </span>
                </div>
                <p style='color: #757575;'>This code expires in <strong>30 minutes</strong>.</p>
                <p style='color: #757575;'>If you did not create a BookShelf account, you can safely ignore this email.</p>
                <br/>
                <p>Best regards,<br/>The BookShelf Team</p>
            </body>
            </html>";

        await SendEmailAsync(email, subject, body);
    }

    public async Task SendPasswordResetEmailAsync(string email, string fullName, string resetCode)
    {
        var subject = "Reset Your BookShelf Password";
        var body = $@"
            <html>
            <body style='font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto;'>
                <h2 style='color: #2196F3;'>Password Reset Request</h2>
                <p>Hello {fullName},</p>
                <p>We received a request to reset your password. Enter the code below in the BookShelf app:</p>
                <div style='text-align: center; margin: 32px 0;'>
                    <span style='font-size: 48px; font-family: monospace; letter-spacing: 12px;
                                 font-weight: bold; color: #212121; background: #F5F5F5;
                                 padding: 16px 24px; border-radius: 8px; display: inline-block;'>
                        {resetCode}
                    </span>
                </div>
                <p style='color: #757575;'>This code expires in <strong>30 minutes</strong>.</p>
                <p style='color: #757575;'>If you didn't request a password reset, please ignore this email.</p>
                <br/>
                <p>Best regards,<br/>The BookShelf Team</p>
            </body>
            </html>";

        await SendEmailAsync(email, subject, body);
    }

    public async Task SendWelcomeEmailAsync(string email, string fullName)
    {
        var subject = "Welcome to BookShelf!";
        var body = $@"
            <html>
            <body style='font-family: Arial, sans-serif;'>
                <h2>Welcome to BookShelf, {fullName}!</h2>
                <p>Your email has been verified successfully. You can now start organizing your personal library!</p>
                <h3>Getting Started:</h3>
                <ul>
                    <li>Add your first book to your collection</li>
                    <li>Track your reading progress</li>
                    <li>Organize books by genre and format</li>
                    <li>Keep track of lent books</li>
                    <li>Add personal notes to your books</li>
                </ul>
                <p>We hope BookShelf helps you enjoy your reading journey even more!</p>
                <br/>
                <p>Happy reading,<br/>The BookShelf Team</p>
            </body>
            </html>";

        await SendEmailAsync(email, subject, body);
    }

    private async Task SendEmailAsync(string toEmail, string subject, string body)
    {
        try
        {
            using var smtpClient = new SmtpClient(_smtpServer, _smtpPort)
            {
                Credentials = new NetworkCredential(_smtpUsername, _smtpPassword),
                EnableSsl = true
            };

            var mailMessage = new MailMessage
            {
                From = new MailAddress(_fromEmail, _fromName),
                Subject = subject,
                Body = body,
                IsBodyHtml = true
            };

            mailMessage.To.Add(toEmail);
            await smtpClient.SendMailAsync(mailMessage);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Email sending failed: {ex.Message}");
            throw;
        }
    }
}
