namespace ASP.UI.Services.Authenfication
{
    public interface ITokenAccessor
    {
        Task SetAuthorizationHeaderAsync(HttpClient httpClient, bool isClient);
    }
}
