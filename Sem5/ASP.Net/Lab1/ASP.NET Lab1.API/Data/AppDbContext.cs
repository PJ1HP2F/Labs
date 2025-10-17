using ASP.NET_Lab1.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace ASP.NET_Lab1.API.Data
{
    public class AppDbContext(DbContextOptions options) : DbContext(options)
    {
        public DbSet<Movie> Movies { get; set; }
        public DbSet<Category> Categories { get; set; }
    }
}
