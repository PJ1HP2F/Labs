using ASP.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace ASP.API.Data
{
    public class DbInitializer
    {
        public static async Task SeedData(WebApplication app)
        {
            using var scope = app.Services.CreateScope();
            var context = scope.ServiceProvider.GetRequiredService<AppDbContext>();

            var baseUrl = app.Configuration.GetValue<string>("AppSettings:BaseUrl");

            await context.Database.MigrateAsync();

            if (context.Categories.Any() || context.Movies.Any())
            {
                return;
            }

            var categories = new List<Category>
            {
                new Category {Name="Триллеры", NormalizedName="thriller"},
                new Category {Name="Фантастика", NormalizedName="science-fiction"},
                new Category {Name="Детективы", NormalizedName="detective"},
                new Category {Name="Для детей", NormalizedName="for-children"},
                new Category {Name="Для взрослых", NormalizedName="for-adults"},
            };

            await context.Categories.AddRangeAsync(categories);
            await context.SaveChangesAsync();

            var movies = new List<Movie>
            {
                new Movie {Name = "Начало", Description = "Лучший фильм",
                           Price = 100, Image = $"{baseUrl}/Images/inception.jpg", Category = categories.Find(c => c.NormalizedName == "science-fiction"), 
                           NormalizedName = "Inception"},
                new Movie {Name = "Семь", Description = "Концовка огонь",
                           Price = 10, Image = $"{baseUrl}/Images/seven.jpg", Category = categories.Find(c => c.NormalizedName == "thriller"), 
                           NormalizedName = "Seven"},
                new Movie {Name = "Лунтик", Description = "Смешарики лучше",
                           Price = 2, Image = $"{baseUrl}/Images/luntik.jpg", Category = categories.Find(c => c.NormalizedName == "for-children"),
                           NormalizedName = "Luntik"},
                new Movie {Name = "Исчезнувшая", Description = "Триллер", Price = 12,
                           Image = $"{baseUrl}/Images/gone.jpg", Category = categories.Find(c => c.NormalizedName == "thriller"),
                           NormalizedName = "Gone"}
            };

            await context.Movies.AddRangeAsync(movies);
            await context.SaveChangesAsync();
        }
    }
}
