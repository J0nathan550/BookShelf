using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;

namespace BookShelf.Infrastructure.Data
{
    public class ApplicationDbContextFactory
    {
        public static ApplicationDbContext CreateDbContext(string[] args)
        {
            var basePath = Path.Combine(Directory.GetCurrentDirectory(), "../BookShelf.Server");

            IConfigurationRoot configuration = new ConfigurationBuilder()
                .SetBasePath(basePath)
                .AddJsonFile("appsettings.json")
                .Build();

            var connectionString = configuration.GetConnectionString("DefaultConnection");

            var builder = new DbContextOptionsBuilder<ApplicationDbContext>();
            builder.UseMySql(
                connectionString,
                ServerVersion.AutoDetect(connectionString),
                b => b.MigrationsAssembly("BookShelf.Infrastructure")
            );

            return new ApplicationDbContext(builder.Options);
        }
    }
}