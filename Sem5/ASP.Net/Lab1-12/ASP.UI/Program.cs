using API.UI.Services.CategoryService;
using ASP.UI.Extensions;
using ASP.UI.HelperClasses;
using ASP.UI.Middleware;
using ASP.UI.Services.CartService;
using ASP.UI.Services.MovieService;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.IdentityModel.Protocols.OpenIdConnect;
using Serilog;

var builder = WebApplication.CreateBuilder(args);
var api = builder.Configuration.GetValue<string>("UriData:ApiUri");

var keycloakData = builder.Configuration.GetSection("Keycloak").Get<KeycloakData>();
if (keycloakData == null)
{
    throw new InvalidOperationException("Keycloak configuration is not set.");
}

// Add services to the container.
builder.Services.AddControllersWithViews();
builder.RegisterCustomServices();

builder.Services.AddHttpClient<ICategoryService, ApiCategoryService>(opt => opt.BaseAddress = new Uri(api + "categories"));
builder.Services.AddHttpClient<IMovieService, ApiMovieService>(opt => opt.BaseAddress = new Uri(api + "movies"));
builder.Services.AddHttpContextAccessor();

builder.Services.AddRazorPages();

builder.Services
                .AddAuthentication(options =>
                {
                    options.DefaultScheme = CookieAuthenticationDefaults.AuthenticationScheme;
                    options.DefaultChallengeScheme = "keycloak";
                })
                .AddCookie()    
                .AddOpenIdConnect("keycloak", options =>
                {
                    options.Authority = $"{keycloakData.Host}/auth/realms/{keycloakData.Realm}";
                    options.ClientId = keycloakData.ClientId;
                    options.ClientSecret = keycloakData.ClientSecret;
                    options.ResponseType = OpenIdConnectResponseType.Code;
                    options.Scope.Add("openid");
                    options.SaveTokens = true;
                    options.RequireHttpsMetadata = false;
                    options.MetadataAddress = $"{keycloakData.Host}/realms/{keycloakData.Realm}/.well-known/openid-configuration";
                });

builder.Services.AddSession();
builder.Services.AddScoped<Cart>(SessionCart.GetCart);

builder.Services.AddAuthorization(options => options.AddPolicy("admin", p => p.RequireRole("POWER-USER")));

/*var logger = new LoggerConfiguration()
    .ReadFrom.Configuration(builder.Configuration)
    .CreateLogger();

builder.Logging.ClearProviders();
builder.Logging.AddConsole();
builder.Logging.AddSerilog();*/

builder.Host.UseSerilog((context, services, configuration) =>
    configuration
        .ReadFrom.Configuration(context.Configuration)
        .ReadFrom.Services(services)
        .Enrich.FromLogContext());

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

app.UseAuthorization();

app.MapStaticAssets();

app.MapControllerRoute(
    name: "areas",
    pattern: "{area:exists}/{controller=Home}/{action=Index}/{id?}");

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}")
    .WithStaticAssets();

app.MapRazorPages().RequireAuthorization("admin");

app.UseSession();

app.UseMiddleware<SerilogLoggerMiddleware>();

app.Run();
