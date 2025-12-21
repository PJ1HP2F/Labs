using ASP.UI.HelperClasses;
using ASP.UI.Models;
using ASP.UI.Services.Authenfication;
using ASP.UI.Services.FileServices;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;
using System.Text.Json;
using System.Text;

namespace ASP.UI.Controllers
{
    public class AccountController(IHttpContextAccessor contextAccessor, HttpClient httpClient, ITokenAccessor tokenAccessor,
        IOptions<KeycloakData> options, IFileService fileService) : Controller
    {

        public IActionResult Register()
        {
            return View(new RegisterUserViewModel());
        }

        [HttpPost]
        [AutoValidateAntiforgeryToken]
        public async Task<IActionResult> Register(RegisterUserViewModel user)
        {
            if (ModelState.IsValid)
            {
                if (user == null)
                {
                    return BadRequest();
                }

                try
                {
                    await tokenAccessor.SetAuthorizationHeaderAsync(httpClient, true);
                }
                catch (Exception ex)
                {
                    return Unauthorized();
                }

                var avatarUrl = "/images/default-profile-picture.png";
                if (user.Avatar != null)
                {
                    avatarUrl = await fileService.SaveFileAsync(user.Avatar);
                }

                var newUser = new CreateUserModel();
                newUser.Attributes.Add("avatar", avatarUrl);
                newUser.Email = user.Email;
                newUser.Username = user.Email;
                newUser.Credentials.Add(new UserCredentials { Value = user.Password });

                var requestUri = $"{options.Value.Host}/admin/realms/{options.Value.Realm}/users";

                var serializerOptions = new JsonSerializerOptions
                {
                    PropertyNamingPolicy = JsonNamingPolicy.CamelCase
                };
                var userData = JsonSerializer.Serialize(newUser, serializerOptions);
                HttpContent content = new StringContent(userData, Encoding.UTF8, "application/json");

                var response = await httpClient.PostAsync(requestUri, content);
                if (response.IsSuccessStatusCode)
                {
                    return Redirect(Url.Action("Index", "Home"));
                }
                else
                {
                    return BadRequest(response.StatusCode);
                }
            }

            return View(user);
        }

        public async Task Login()
        {
            await HttpContext.ChallengeAsync("keycloak",
            new AuthenticationProperties
            {
                RedirectUri = Url.Action("Index", "Home")
            });
        }

        [HttpPost]
        public async Task Logout()
        {
            await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
            await HttpContext.SignOutAsync("keycloak",
            new AuthenticationProperties
            {
                RedirectUri = Url.Action("Index", "Home")
            });
        }
    }

    class CreateUserModel
    {
        public Dictionary<string, string> Attributes { get; set; } = [];
        public string Username { get; set; }
        public string Email { get; set; }
        public bool Enabled { get; set; } = true;
        public bool EmailVerified { get; set; } = true;
        public List<UserCredentials> Credentials { get; set; } = [];
    }

    class UserCredentials
    {
        public string Type { get; set; } = "password";
        public bool Temporary { get; set; } = false;
        public string Value { get; set; }
    }
}
