using ASP.UI.HelperClasses;
using ASP.UI.Services.Authenfication;
using ASP.UI.Services.CategoryService;
using ASP.UI.Services.FileServices;
using ASP.UI.Services.MovieService;

namespace ASP.UI.Extensions
{
    public static class HostingExtensions
    {
        public static void RegisterCustomServices(this WebApplicationBuilder builder)
        {
            builder.Services
                .Configure<KeycloakData>(builder.Configuration.GetSection("Keycloak"))
                .AddHttpClient<ITokenAccessor, KeycloakTokenAccessor>();

            builder.Services.AddHttpClient<IFileService, LocalFileService>(opt =>
            {
                opt.BaseAddress = new Uri(builder.Configuration["UriData:ApiUri"] + "Files");
            });
        }
    }
}
