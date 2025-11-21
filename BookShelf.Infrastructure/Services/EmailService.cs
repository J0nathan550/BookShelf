using BookShelf.Application.Services;
using Microsoft.Extensions.Configuration;
using System.Net;
using System.Net.Mail;

namespace BookShelf.Infrastructure.Services;

public class EmailService : IEmailService
{
    private readonly IConfiguration _configuration;
    private readonly string _smtpServer;
    private readonly int _smtpPort;
    private readonly string _smtpUsername;
    private readonly string _smtpPassword;
    private readonly string _fromEmail;
    private readonly string _fromName;

    public EmailService(IConfiguration configuration)
    {
        _configuration = configuration;
        _smtpServer = _configuration["Email:SmtpServer"] ?? "smtp.gmail.com";
        _smtpPort = int.Parse(_configuration["Email:SmtpPort"] ?? "587");
        _smtpUsername = _configuration["Email:Username"] ?? "";
        _smtpPassword = _configuration["Email:Password"] ?? "";
        _fromEmail = _configuration["Email:FromEmail"] ?? "";
        _fromName = _configuration["Email:FromName"] ?? "BookShelf";
    }

    public async Task SendEmailVerificationAsync(string email, string fullName, string verificationLink)
    {
        var subject = "Verify Your BookShelf Account";
        var body = $@"
                <html>
                <body style='font-family: Arial, sans-serif;'>
                    <h2>Welcome to BookShelf, {fullName}!</h2>
                    <p>Thank you for registering. Please verify your email address by clicking the link below:</p>
                    <p>
                        <a href='{verificationLink}' 
                           style='background-color: #4CAF50; color: white; padding: 10px 20px; 
                                  text-decoration: none; border-radius: 5px; display: inline-block;'>
                            Verify Email
                        </a>
                    </p>
                    <p>If the button doesn't work, copy and paste this link into your browser:</p>
                    <p>{verificationLink}</p>
                    <p>This link will expire in 24 hours.</p>
                    <br/>
                    <p>Best regards,<br/>The BookShelf Team</p>
                </body>
                </html>
            ";

        await SendEmailAsync(email, subject, body);
    }

    public async Task SendPasswordResetEmailAsync(string email, string fullName, string resetLink)
    {
        var subject = "Reset Your BookShelf Password";
        var body = $@"
                <html>
                <body style='font-family: Arial, sans-serif;'>
                    <h2>Password Reset Request</h2>
                    <p>Hello {fullName},</p>
                    <p>We received a request to reset your password. Click the link below to reset it:</p>
                    <p>
                        <a href='{resetLink}' 
                           style='background-color: #2196F3; color: white; padding: 10px 20px; 
                                  text-decoration: none; border-radius: 5px; display: inline-block;'>
                            Reset Password
                        </a>
                    </p>
                    <p>If the button doesn't work, copy and paste this link into your browser:</p>
                    <p>{resetLink}</p>
                    <p>This link will expire in 1 hour.</p>
                    <p>If you didn't request a password reset, please ignore this email.</p>
                    <br/>
                    <p>Best regards,<br/>The BookShelf Team</p>
                </body>
                </html>
            ";

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
                </html>
            ";

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
