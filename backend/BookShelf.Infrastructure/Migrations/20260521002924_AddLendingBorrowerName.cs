using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace BookShelf.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AddLendingBorrowerName : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "BorrowerName",
                table: "LendingRecords",
                type: "varchar(200)",
                maxLength: 200,
                nullable: false,
                defaultValue: "")
                .Annotation("MySql:CharSet", "utf8mb4");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "BorrowerName",
                table: "LendingRecords");
        }
    }
}
