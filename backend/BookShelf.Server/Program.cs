using BookShelf.Application.Services;
using BookShelf.Domain.Entities;
using BookShelf.Infrastructure.Data;
using BookShelf.Infrastructure.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using System.Text;

namespace BookShelf.Server;

public class Program
{
    public static async Task Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);

        // Add services to the container
        builder.Services.AddControllers();
        builder.Services.AddEndpointsApiExplorer();

        builder.Services.AddSwaggerGen(c =>
        {
            c.SwaggerDoc("v1", new OpenApiInfo
            {
                Title = "BookShelf API",
                Version = "v1",
                Description = "API for managing personal book collections"
            });

            c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
            {
                Description = "JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"",
                Name = "Authorization",
                In = ParameterLocation.Header,
                Type = SecuritySchemeType.ApiKey,
                Scheme = "Bearer"
            });

            c.AddSecurityRequirement(new OpenApiSecurityRequirement
            {
                {
                    new OpenApiSecurityScheme
                    {
                        Reference = new OpenApiReference
                        {
                            Type = ReferenceType.SecurityScheme,
                            Id = "Bearer"
                        },
                        Scheme = "oauth2",
                        Name = "Bearer",
                        In = ParameterLocation.Header
                    },
                    new List<string>()
                }
            });
        });

        // Database configuration
        builder.Services.AddDbContext<ApplicationDbContext>(options =>
            options.UseMySql(
                builder.Configuration.GetConnectionString("DefaultConnection"),
                ServerVersion.AutoDetect(builder.Configuration.GetConnectionString("DefaultConnection")),
                b => b.MigrationsAssembly("BookShelf.Infrastructure")
            ));

        // Identity configuration
        builder.Services.AddIdentity<ApplicationUser, IdentityRole>(options =>
        {
            options.Password.RequireDigit = true;
            options.Password.RequireLowercase = true;
            options.Password.RequireUppercase = true;
            options.Password.RequireNonAlphanumeric = false;
            options.Password.RequiredLength = 6;
            options.User.RequireUniqueEmail = true;
            options.SignIn.RequireConfirmedEmail = true;
        })
        .AddEntityFrameworkStores<ApplicationDbContext>()
        .AddDefaultTokenProviders();

        builder.Services.AddScoped<ITokenService, TokenService>();

        var jwtSettings = builder.Configuration.GetSection("Jwt");
        var key = Encoding.UTF8.GetBytes(jwtSettings["Key"]!);

        builder.Services.AddAuthentication(options =>
        {
            options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
            options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
        })
        .AddJwtBearer(options =>
        {
            options.RequireHttpsMetadata = false;
            options.SaveToken = true;
            options.TokenValidationParameters = new TokenValidationParameters
            {
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(key),

                ValidateIssuer = true,
                ValidateAudience = true,

                ValidIssuer = jwtSettings["Issuer"],
                ValidAudience = jwtSettings["Audience"],
                ClockSkew = TimeSpan.Zero
            };
        });

        builder.Services.AddHttpClient();
        builder.Services.AddScoped<IBookService, BookService>();
        builder.Services.AddScoped<IStatisticsService, StatisticsService>();
        builder.Services.AddScoped<IAdminService, AdminService>();
        builder.Services.AddScoped<IEmailService, EmailService>();
        builder.Services.AddScoped<INotificationService, NotificationService>();

        // CORS configuration
        builder.Services.AddCors(options =>
        {
            options.AddPolicy("AllowAnyOriginCors", policy =>
            {
                policy.AllowAnyOrigin()
                      .AllowAnyMethod()
                      .AllowAnyHeader();
            });
        });

        var app = builder.Build();

        // Seed admin user
        using (var scope = app.Services.CreateScope())
        {
            var services = scope.ServiceProvider;
            try
            {
                var userManager = services.GetRequiredService<UserManager<ApplicationUser>>();
                var roleManager = services.GetRequiredService<RoleManager<IdentityRole>>();
                var dbContext = services.GetRequiredService<ApplicationDbContext>();
                await dbContext.Database.MigrateAsync();
                await SeedDatabaseBaseData(userManager, roleManager, dbContext, app.Configuration);
            }
            catch (Exception ex)
            {
                var logger = services.GetRequiredService<ILogger<Program>>();
                logger.LogError(ex, "An error occurred while seeding the database.");
            }
        }

        // Configure the HTTP request pipeline
        // if (app.Environment.IsDevelopment())
        // {
            app.UseSwagger();
            app.UseSwaggerUI(c =>
            {
                c.SwaggerEndpoint("/swagger/v1/swagger.json", "BookShelf API v1");
            });
        // }

        // app.UseHttpsRedirection(); for local test is okay, but for esp32 it causes redirection and it fails to do any API requests...
        app.UseStaticFiles();
        app.UseCors("AllowAnyOriginCors");

        app.UseAuthentication();
        app.UseAuthorization();

        app.MapControllers();

        app.Run();
    }

    private static async Task SeedDatabaseBaseData(
        UserManager<ApplicationUser> userManager,
        RoleManager<IdentityRole> roleManager,
        ApplicationDbContext dbContext,
        IConfiguration configuration)
    {
        if (!await roleManager.RoleExistsAsync("Admin")) await roleManager.CreateAsync(new IdentityRole("Admin"));
        if (!await roleManager.RoleExistsAsync("User")) await roleManager.CreateAsync(new IdentityRole("User"));

        var adminEmail = configuration["AdminUser:Email"] ?? "admin@bookshelf.com";
        var adminPassword = configuration["AdminUser:Password"] ?? "Admin123!";
        var adminUser = await userManager.FindByEmailAsync(adminEmail);
        if (adminUser == null)
        {
            adminUser = new ApplicationUser
            {
                UserName = adminEmail,
                Email = adminEmail,
                FullName = configuration["AdminUser:FullName"] ?? "System Administrator",
                EmailConfirmed = true,
                IsActive = true,
                RegistrationDate = DateTime.UtcNow
            };
            var result = await userManager.CreateAsync(adminUser, adminPassword);
            if (result.Succeeded) await userManager.AddToRoleAsync(adminUser, "Admin");
        }

        var genres = new[] { "Fantasy", "Science Fiction", "Mystery", "Thriller", "Romance", "Historical", "Non-Fiction", "Biography" };
        foreach (var genre in genres)
        {
            if (!await dbContext.Genres.AnyAsync(g => g.Name == genre))
                dbContext.Genres.Add(new Genre { Name = genre });
        }

        var formats = new[] { "Hardcover", "Paperback", "E-Book", "Audiobook" };
        foreach (var format in formats)
        {
            if (!await dbContext.BookFormats.AnyAsync(f => f.Name == format))
                dbContext.BookFormats.Add(new BookFormat { Name = format });
        }

        await dbContext.SaveChangesAsync();

        if (!await dbContext.Books.AnyAsync())
        {
            var seedBooks = new List<Book>
            {
                new() { Title = "The Great Gatsby", Author = "F. Scott Fitzgerald", Pages = 180, IsApproved = true, ApplicationUserId = adminUser!.Id },
                new() { Title = "The Hobbit", Author = "J.R.R. Tolkien", Pages = 310, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "The Catcher in the Rye", Author = "J.D. Salinger", Pages = 277, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "The Lord of the Rings", Author = "J.R.R. Tolkien", Pages = 1178, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "The Hitchhiker's Guide to the Galaxy", Author = "Douglas Adams", Pages = 193, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "The Da Vinci Code", Author = "Dan Brown", Pages = 454, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "The Alchemist", Author = "Paulo Coelho", Pages = 197, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "The Name of the Wind", Author = "Patrick Rothfuss", Pages = 662, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "The Martian", Author = "Andy Weir", Pages = 369, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "1984", Author = "George Orwell", Pages = 328, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "Brave New World", Author = "Aldous Huxley", Pages = 311, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "To Kill a Mockingbird", Author = "Harper Lee", Pages = 281, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "Dune", Author = "Frank Herbert", Pages = 412, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "Foundation", Author = "Isaac Asimov", Pages = 244, IsApproved = true, ApplicationUserId = adminUser.Id },
                new() { Title = "Harry Potter and the Philosopher's Stone", Author = "J.K. Rowling", Pages = 309, IsApproved = true, ApplicationUserId = adminUser.Id },
            };
            dbContext.Books.AddRange(seedBooks);
            await dbContext.SaveChangesAsync();
        }
    }
}