using ASP.UI.Services.Authenfication;

namespace ASP.UI.Services.FileServices
{
    public class LocalFileService(HttpClient httpClient, ITokenAccessor tokenAccessor) : IFileService
    {
        private readonly HttpClient _httpClient = httpClient;
        private readonly ITokenAccessor _tokenAccessor = tokenAccessor;

        public async Task<string> SaveFileAsync(IFormFile formFile)
        {
            if (formFile == null)
            {
                throw new ArgumentNullException(nameof(formFile));
            }

            await _tokenAccessor.SetAuthorizationHeaderAsync(_httpClient, true);

            var request = new HttpRequestMessage(HttpMethod.Post, "files");

            var extension = Path.GetExtension(formFile.FileName);
            var newName = Path.ChangeExtension(Path.GetRandomFileName(), extension);

            var content = new MultipartFormDataContent();
            var streamContent = new StreamContent(formFile.OpenReadStream());
            content.Add(streamContent, "file", newName);

            request.Content = content;

            var response = await _httpClient.SendAsync(request);

            if (response.IsSuccessStatusCode)
            {
                var responseContent = await response.Content.ReadAsStringAsync();
                return responseContent;
            }
            else
            {
                throw new InvalidOperationException($"Error while uploading file: {response.StatusCode}");
            }
        }
    }
}
