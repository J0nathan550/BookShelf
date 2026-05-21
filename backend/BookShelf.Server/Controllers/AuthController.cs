using BookShelf.Application.Services;
using BookShelf.Domain.Entities;
using BookShelf.Server.DTOs;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.WebUtilities;
using System.Text;

namespace BookShelf.Server.Controllers;

[Route("api/[controller]")]
[ApiController]
public class AuthController : ControllerBase
{
    private readonly UserManager<ApplicationUser> _userManager;
    private readonly IEmailService _emailService;
    private readonly ITokenService _tokenService;
    private readonly IConfiguration _configuration;
    private readonly INotificationService _notificationService;

    public AuthController(
        UserManager<ApplicationUser> userManager,
        IEmailService emailService,
        ITokenService tokenService,
        IConfiguration configuration,
        INotificationService notificationService)
    {
        _userManager = userManager;
        _emailService = emailService;
        _tokenService = tokenService;
        _configuration = configuration;
        _notificationService = notificationService;
    }

    [HttpPost("register")]
    [ProducesResponseType(typeof(AuthResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> Register([FromBody] RegisterDto dto)
    {
        var existingUser = await _userManager.FindByEmailAsync(dto.Email);
        if (existingUser != null)
        {
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "User with this email already exists"
            });
        }

        var user = new ApplicationUser
        {
            UserName = dto.Email,
            Email = dto.Email,
            FullName = dto.FullName,
            RegistrationDate = DateTime.UtcNow,
            IsActive = false
        };

        var result = await _userManager.CreateAsync(user, dto.Password);

        if (!result.Succeeded)
        {
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = string.Join(", ", result.Errors.Select(e => e.Description))
            });
        }

        var code = new Random().Next(100000, 999999).ToString();
        user.EmailVerificationCode = code;
        user.EmailVerificationCodeExpiry = DateTime.UtcNow.AddMinutes(30);
        await _userManager.UpdateAsync(user);

        try
        {
            await _emailService.SendEmailVerificationCodeAsync(user.Email!, user.FullName, code);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Failed to send verification email: {ex.Message}");
        }

        return Ok(new AuthResponseDto
        {
            Success = true,
            Message = "Registration successful. A 6-digit verification code has been sent to your email.",
            UserId = user.Id
        });
    }

    [HttpPost("verify-email-code")]
    [ProducesResponseType(typeof(AuthResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> VerifyEmailCode([FromBody] VerifyEmailCodeDto dto)
    {
        var user = await _userManager.FindByIdAsync(dto.UserId);
        if (user == null)
        {
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "User not found"
            });
        }

        if (user.EmailVerificationCode == null || user.EmailVerificationCodeExpiry < DateTime.UtcNow)
        {
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "Verification code has expired. Please request a new one."
            });
        }

        if (user.EmailVerificationCode != dto.Code)
        {
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "Invalid verification code"
            });
        }

        user.EmailConfirmed = true;
        user.IsActive = true;
        user.EmailVerificationCode = null;
        user.EmailVerificationCodeExpiry = null;
        await _userManager.UpdateAsync(user);

        try
        {
            await _emailService.SendWelcomeEmailAsync(user.Email!, user.FullName);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Failed to send welcome email: {ex.Message}");
        }

        await _notificationService.NotifyAdminsNewUserAsync(user.FullName);

        return Ok(new AuthResponseDto
        {
            Success = true,
            Message = "Email verified successfully. You can now log in."
        });
    }

    [HttpPost("resend-verification-code")]
    [ProducesResponseType(typeof(AuthResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> ResendVerificationCode([FromBody] ResendVerificationCodeDto dto)
    {
        var user = await _userManager.FindByIdAsync(dto.UserId);
        if (user == null || user.IsActive)
        {
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "Unable to resend code"
            });
        }

        var code = new Random().Next(100000, 999999).ToString();
        user.EmailVerificationCode = code;
        user.EmailVerificationCodeExpiry = DateTime.UtcNow.AddMinutes(30);
        await _userManager.UpdateAsync(user);

        try
        {
            await _emailService.SendEmailVerificationCodeAsync(user.Email!, user.FullName, code);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Failed to resend verification email: {ex.Message}");
            return StatusCode(500, new AuthResponseDto { Success = false, Message = "Failed to send email" });
        }

        return Ok(new AuthResponseDto
        {
            Success = true,
            Message = "A new verification code has been sent to your email."
        });
    }

    [HttpPost("login")]
    [ProducesResponseType(typeof(AuthResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> Login([FromBody] LoginDto dto)
    {
        var user = await _userManager.FindByEmailAsync(dto.Email);
        if (user == null)
        {
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "Invalid email or password"
            });
        }

        if (!user.IsActive)
        {
            if (!user.EmailConfirmed)
            {
                return BadRequest(new AuthResponseDto
                {
                    Success = false,
                    Message = "Account is not active. Please verify your email.",
                    UserId = user.Id
                });
            }

            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "Your account has been deactivated by an administrator. Please contact support."
            });
        }

        var isPasswordValid = await _userManager.CheckPasswordAsync(user, dto.Password);

        if (!isPasswordValid)
        {
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "Invalid email or password"
            });
        }

        var roles = await _userManager.GetRolesAsync(user);
        var token = _tokenService.GenerateToken(user, roles);

        return Ok(new AuthResponseDto
        {
            Success = true,
            Message = "Login successful",
            UserId = user.Id,
            Email = user.Email,
            FullName = user.FullName,
            Roles = [.. roles],
            Token = token
        });
    }

    [HttpPost("logout")]
    [Authorize]
    [ProducesResponseType(StatusCodes.Status200OK)]
    public IActionResult Logout()
    {
        return Ok(new AuthResponseDto
        {
            Success = true,
            Message = "Logged out successfully"
        });
    }

    [HttpPost("forgot-password")]
    [ProducesResponseType(typeof(AuthResponseDto), StatusCodes.Status200OK)]
    public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordDto dto)
    {
        var user = await _userManager.FindByEmailAsync(dto.Email);
        if (user == null || !user.EmailConfirmed)
        {
            return Ok(new AuthResponseDto { Success = true, Message = "If an account with that email exists, a reset code has been sent." });
        }

        var code = new Random().Next(100000, 999999).ToString();
        user.PasswordResetCode = code;
        user.PasswordResetCodeExpiry = DateTime.UtcNow.AddMinutes(30);
        await _userManager.UpdateAsync(user);

        try
        {
            await _emailService.SendPasswordResetEmailAsync(user.Email!, user.FullName, code);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Failed to send password reset email: {ex.Message}");
        }

        return Ok(new AuthResponseDto { Success = true, Message = "If an account with that email exists, a reset code has been sent." });
    }

    [HttpPost("reset-password-code")]
    [ProducesResponseType(typeof(AuthResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> ResetPasswordWithCode([FromBody] ResetPasswordCodeDto dto)
    {
        var user = await _userManager.FindByEmailAsync(dto.Email);
        if (user == null)
            return BadRequest(new AuthResponseDto { Success = false, Message = "Invalid reset request" });

        if (user.PasswordResetCode == null || user.PasswordResetCodeExpiry < DateTime.UtcNow)
            return BadRequest(new AuthResponseDto { Success = false, Message = "Reset code has expired. Please request a new one." });

        if (user.PasswordResetCode != dto.Code)
            return BadRequest(new AuthResponseDto { Success = false, Message = "Invalid reset code" });

        var token = await _userManager.GeneratePasswordResetTokenAsync(user);
        var result = await _userManager.ResetPasswordAsync(user, token, dto.NewPassword);

        if (!result.Succeeded)
            return BadRequest(new AuthResponseDto { Success = false, Message = string.Join(", ", result.Errors.Select(e => e.Description)) });

        user.PasswordResetCode = null;
        user.PasswordResetCodeExpiry = null;
        await _userManager.UpdateAsync(user);

        return Ok(new AuthResponseDto { Success = true, Message = "Password reset successfully. You can now log in." });
    }

    [HttpPost("reset-password")]
    [ProducesResponseType(typeof(AuthResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordDto dto)
    {
        var user = await _userManager.FindByEmailAsync(dto.Email);
        if (user == null)
            return BadRequest(new AuthResponseDto { Success = false, Message = "Invalid password reset request" });

        var decodedToken = Encoding.UTF8.GetString(WebEncoders.Base64UrlDecode(dto.Token));
        var result = await _userManager.ResetPasswordAsync(user, decodedToken, dto.NewPassword);

        if (!result.Succeeded)
            return BadRequest(new AuthResponseDto { Success = false, Message = "Password reset failed. The link may have expired." });

        return Ok(new AuthResponseDto { Success = true, Message = "Password reset successfully." });
    }

    [HttpGet("current-user")]
    [Authorize]
    [ProducesResponseType(typeof(AuthResponseDto), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetCurrentUser()
    {
        var user = await _userManager.GetUserAsync(User);
        if (user == null) return Unauthorized();

        var roles = await _userManager.GetRolesAsync(user);

        return Ok(new AuthResponseDto
        {
            Success = true,
            UserId = user.Id,
            Email = user.Email,
            FullName = user.FullName,
            Roles = [.. roles]
        });
    }
}
