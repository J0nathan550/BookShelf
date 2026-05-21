using BookShelf.Domain.Entities;

namespace BookShelf.Application.Services;

public interface ITokenService
{
    string GenerateToken(ApplicationUser user, IList<string> roles);
}