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

    public AuthController(
        UserManager<ApplicationUser> userManager,
        IEmailService emailService,
        ITokenService tokenService,
        IConfiguration configuration)
    {
        _userManager = userManager;
        _emailService = emailService;
        _tokenService = tokenService;
        _configuration = configuration;
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

        var token = await _userManager.GenerateEmailConfirmationTokenAsync(user);
        var encodedToken = WebEncoders.Base64UrlEncode(Encoding.UTF8.GetBytes(token));

        var frontendUrl = _configuration["FrontendUrl"] ?? "http://localhost:4200";
        var verificationLink = $"{frontendUrl}/verify-email?userId={user.Id}&token={encodedToken}";

        try
        {
            await _emailService.SendEmailVerificationAsync(user.Email!, user.FullName, verificationLink);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Failed to send verification email: {ex.Message}");
        }

        return Ok(new AuthResponseDto
        {
            Success = true,
            Message = "Registration successful. Please check your email to verify your account.",
            UserId = user.Id
        });
    }

    [HttpPost("verify-email")]
    [ProducesResponseType(typeof(AuthResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> VerifyEmail([FromQuery] string userId, [FromQuery] string token)
    {
        var user = await _userManager.FindByIdAsync(userId);
        if (user == null)
        {
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "Invalid verification link"
            });
        }

        var decodedToken = Encoding.UTF8.GetString(WebEncoders.Base64UrlDecode(token));
        var result = await _userManager.ConfirmEmailAsync(user, decodedToken);

        if (!result.Succeeded)
        {
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "Email verification failed. The link may have expired."
            });
        }

        user.IsActive = true;
        await _userManager.UpdateAsync(user);

        try
        {
            await _emailService.SendWelcomeEmailAsync(user.Email!, user.FullName);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Failed to send welcome email: {ex.Message}");
        }

        return Ok(new AuthResponseDto
        {
            Success = true,
            Message = "Email verified successfully. You can now log in."
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
            return BadRequest(new AuthResponseDto
            {
                Success = false,
                Message = "Account is not active. Please verify your email or contact support."
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
            return Ok(new AuthResponseDto { Success = true, Message = "If an account with that email exists, a password reset link has been sent." });
        }

        var token = await _userManager.GeneratePasswordResetTokenAsync(user);
        var encodedToken = WebEncoders.Base64UrlEncode(Encoding.UTF8.GetBytes(token));

        var frontendUrl = _configuration["FrontendUrl"] ?? "http://localhost:4200";
        var resetLink = $"{frontendUrl}/reset-password?email={dto.Email}&token={encodedToken}";

        try
        {
            await _emailService.SendPasswordResetEmailAsync(user.Email!, user.FullName, resetLink);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Failed to send password reset email: {ex.Message}");
        }

        return Ok(new AuthResponseDto { Success = true, Message = "If an account with that email exists, a password reset link has been sent." });
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