using ASP.UI.HelperClasses;
using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Options;
using System.Net.Http.Headers;
using System.Text.Json;

namespace ASP.UI.Services.Authenfication
{
    public class KeycloakTokenAccessor(IHttpContextAccessor contextAccessor, IOptions<KeycloakData> options, HttpClient httpClient) : ITokenAccessor
    {
        private readonly HttpContext _httpContext = contextAccessor.HttpContext!;
        private readonly KeycloakData _keycloakData = options.Value;

        public async Task SetAuthorizationHeaderAsync(HttpClient httpClient, bool isClient)
        {
            var token = await GetClientToken();
            httpClient.DefaultRequestHeaders.Authorization =
                new AuthenticationHeaderValue("Bearer", token);

        }

        async Task<string> GetUserToken()
        {
            var authSession = await _httpContext!.AuthenticateAsync("keycloak");

            if (authSession?.Principal == null)
            {
                throw new AuthenticationFailureException("User unathorized");
            }
            return (await _httpContext!.GetTokenAsync("keycloak", "access_token"))!;
        }

        async Task<string> GetClientToken()
        {
            var requestUri = $"{_keycloakData.Host}/realms/{_keycloakData.Realm}/protocol/openid-connect/token";

            HttpContent content = new FormUrlEncodedContent(
                [
                    new KeyValuePair<string,string>("client_id", _keycloakData.ClientId),
                    new KeyValuePair<string,string>("grant_type", "client_credentials"),
                    new KeyValuePair<string,string>("client_secret", _keycloakData.ClientSecret)
                ]);

            var response = await httpClient.PostAsync(requestUri, content);

            if (!response.IsSuccessStatusCode)
            {
                throw new HttpRequestException(response.StatusCode.ToString());
            }

            var json = await response.Content.ReadAsStringAsync();
            using var doc = JsonDocument.Parse(json);
            var token = doc.RootElement.GetProperty("access_token").GetString();

            return token!;
        }
    }
}
