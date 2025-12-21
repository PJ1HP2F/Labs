using Microsoft.EntityFrameworkCore;
using ASP.UI.Models;

namespace ASP.UI
{
    public class TempDbContext : DbContext
    {
        public DbSet<Movie> Movies { get; set; }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            base.OnConfiguring(optionsBuilder);
            optionsBuilder.UseSqlite("");
        }
        public DbSet<ASP.UI.Models.RegisterUserViewModel> RegisterUserViewModel { get; set; } = default!;
    }
}
