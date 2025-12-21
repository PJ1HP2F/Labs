using ASP.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace ASP.API.Data
{
    public class AppDbContext(DbContextOptions options) : DbContext(options)
    {
        public DbSet<Movie> Movies { get; set; }
        public DbSet<Category> Categories { get; set; }
    }
}
