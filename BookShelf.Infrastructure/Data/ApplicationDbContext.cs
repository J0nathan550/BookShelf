using BookShelf.Domain.Entities;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;

namespace BookShelf.Infrastructure.Data;

public class ApplicationDbContext : IdentityDbContext<ApplicationUser>
{
    public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
        : base(options)
    {
    }

    public DbSet<Book> Books { get; set; }
    public DbSet<Genre> Genres { get; set; }
    public DbSet<BookFormat> BookFormats { get; set; }
    public DbSet<LendingRecord> LendingRecords { get; set; }
    public DbSet<BookNote> BookNotes { get; set; }

    protected override void OnModelCreating(ModelBuilder builder)
    {
        base.OnModelCreating(builder);

        builder.Entity<Book>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Title).IsRequired().HasMaxLength(200);
            entity.Property(e => e.Author).IsRequired().HasMaxLength(200);
            entity.Property(e => e.CoverImageUrl).HasMaxLength(500);

            entity.Property(e => e.Status).HasMaxLength(50).HasDefaultValue("Available");

            entity.HasOne(e => e.Genre)
                .WithMany(g => g.Books)
                .HasForeignKey(e => e.GenreId)
                .OnDelete(DeleteBehavior.SetNull);

            entity.HasOne(e => e.Format)
                .WithMany(f => f.Books)
                .HasForeignKey(e => e.FormatId)
                .OnDelete(DeleteBehavior.SetNull);
        });


        builder.Entity<LendingRecord>(entity =>
        {
            entity.HasKey(e => e.Id);

            entity.HasOne(e => e.Book)
                .WithMany(b => b.LendingRecords) 
                .HasForeignKey(e => e.BookId)
                .OnDelete(DeleteBehavior.Cascade);

            entity.HasOne(e => e.ApplicationUser)
                .WithMany(u => u.LendingRecords)
                .HasForeignKey(e => e.ApplicationUserId)
                .OnDelete(DeleteBehavior.Cascade);
        });

        builder.Entity<BookNote>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.NoteText).IsRequired().HasMaxLength(2000);

            entity.HasOne(e => e.Book)
                .WithMany(b => b.Notes)
                .HasForeignKey(e => e.BookId)
                .OnDelete(DeleteBehavior.Cascade);

            entity.HasOne(e => e.ApplicationUser)
                .WithMany(u => u.BookNotes)
                .HasForeignKey(e => e.ApplicationUserId)
                .OnDelete(DeleteBehavior.Cascade);
        });

        builder.Entity<Genre>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Name).IsRequired().HasMaxLength(100);
        });

        builder.Entity<BookFormat>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Name).IsRequired().HasMaxLength(50);
        });
    }
}