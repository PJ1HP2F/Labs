using ASP.NET_Lab1.UI.Services.CategoryService;
using ASP.NET_Lab1.UI.Services.MovieService;

namespace ASP.NET_Lab1.UI.Extensions
{
    public static class HostingExtensions
    {
        public static void RegisterCustomServices(this WebApplicationBuilder builder)
        {
            builder.Services
                .AddScoped<ICategoryService, MemoryCategoryService>()
                .AddScoped<IMovieService, MemoryMovieService>();
        }
    }
}
