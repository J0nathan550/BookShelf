using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace BookShelf.Server.Controllers;

public class BaseController : ControllerBase
{
    protected string GetUserId()
    {
        return User.FindFirstValue(ClaimTypes.NameIdentifier) ?? string.Empty;
    }

    protected bool IsAdmin()
    {
        return User.IsInRole("Admin");
    }
}