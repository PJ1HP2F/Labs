using ASP.UI.Services.Authenfication;
using System.Text;
using System.Text.Json;

namespace ASP.UI.Services.MovieService
{
    public class ApiMovieService(HttpClient httpClient, IConfiguration configuration, ILogger<ApiMovieService> logger, ITokenAccessor tokenAccessor) : IMovieService
    {
        private readonly HttpClient _client = httpClient;
        private readonly IConfiguration _configuration = configuration;
        private readonly ILogger _logger = logger;
        private readonly ITokenAccessor _tokenAccessor = tokenAccessor;
        private readonly JsonSerializerOptions _serializerOptions = new() { PropertyNamingPolicy = JsonNamingPolicy.CamelCase };

        public async Task<ResponseData<Movie>> CreateProductAsync(Movie movie, IFormFile? formFile)
        {
            movie.Image = "Images/default.jpg";
            var request = new HttpRequestMessage { Method = HttpMethod.Post, RequestUri = _client.BaseAddress };
            var content = new MultipartFormDataContent();


            if (formFile != null)
            {
                var streamContent = new StreamContent(formFile.OpenReadStream());
                content.Add(streamContent, "file", formFile.FileName);
            }

            var data = new StringContent(JsonSerializer.Serialize(movie));
            content.Add(data, "movie");

            request.Content = content;

            try
            {
                await _tokenAccessor.SetAuthorizationHeaderAsync(_client, false);
            }
            catch (Exception e)
            {
                return ResponseData<Movie>.Error($"Object was not created. Error: {e.Message}");
            }

            var response = await _client.SendAsync(request, CancellationToken.None);

            if (response.IsSuccessStatusCode)
            {
                return (await response.Content.ReadFromJsonAsync<ResponseData<Movie>>(_serializerOptions))!;
            }
            _logger.LogError($"Error while creating object. Error:{response.StatusCode}");

            return ResponseData<Movie>.Error($"Error while creating object. Error:{response.StatusCode}");
        }

        public async Task DeleteProductAsync(int id)
        {
            try
            {
                await _tokenAccessor.SetAuthorizationHeaderAsync(_client, false);
            }
            catch (Exception e)
            {
                ResponseData<Movie>.Error($"Object was not deleted. Error: {e.Message}");
                return;
            }

            var response = await _client.DeleteAsync($"{_client.BaseAddress!.AbsoluteUri}/{id}");
            response.EnsureSuccessStatusCode();
        }

        public async Task<ResponseData<Movie>> GetProductByIdAsync(int id)
        {
            var urlString = new StringBuilder($"{_client.BaseAddress!.AbsoluteUri}");
            urlString.Append($"/{id}");

            try
            {
                await _tokenAccessor.SetAuthorizationHeaderAsync(_client, false);
            }
            catch (Exception e)
            {
                return ResponseData<Movie>.Error($"Object was not get. Error: {e.Message}");
            }

            var response = await _client.GetAsync(new Uri(urlString.ToString()));
            if (response.IsSuccessStatusCode)
            {
                try
                {
                    return (await response.Content.ReadFromJsonAsync<ResponseData<Movie>>(_serializerOptions))!;
                }
                catch (JsonException ex)
                {
                    _logger.LogError($"Error: {ex.Message}");
                }
            }
            _logger.LogError($"Error while receiving data. Error:{response.StatusCode}");

            return ResponseData<Movie>.Error($"Error while receiving data. Error:{response.StatusCode}");
        }

        public async Task<ResponseData<ListModel<Movie>>> GetProductListAsync(string? categoryNormalizedName, int pageNumber = 1)
        {
            var urlString = new StringBuilder($"{_client.BaseAddress!.AbsoluteUri}");

            List<KeyValuePair<string, string?>> parameters = [];
            if (categoryNormalizedName != null)
            {
                parameters.Add(new KeyValuePair<string, string?>("category", categoryNormalizedName));
            }
            if (pageNumber > 1)
            {
                parameters.Add(new KeyValuePair<string, string?>("pageNumber", pageNumber.ToString()));
            }

            var _pageSize = _configuration.GetValue<string>("PageSettings:ItemsPerPage");
            if (_pageSize!.Equals("3"))
            {
                parameters.Add(new KeyValuePair<string, string?>("pageSize", _pageSize));
            }
            urlString.Append(QueryString.Create(parameters));

            var response = await _client.GetAsync(new Uri(urlString.ToString()));
            if (response.IsSuccessStatusCode)
            {
                try
                {
                    return (await response.Content.ReadFromJsonAsync<ResponseData<ListModel<Movie>>>(_serializerOptions))!;
                }
                catch (JsonException ex)
                {
                    _logger.LogError($"Error: {ex.Message}");
                }
            }
            _logger.LogError($"Error while receiving data. Error:{response.StatusCode}");

            return ResponseData<ListModel<Movie>>.Error($"Error while receiving data. Error:{response.StatusCode}");
        }

        public async Task UpdateProductAsync(int id, Movie movie, IFormFile? formFile)
        {
            var request = new HttpRequestMessage { Method = HttpMethod.Put, RequestUri = new Uri($"{_client.BaseAddress}/{id}") };
            var content = new MultipartFormDataContent();

            if (formFile != null)
            {
                var streamContent = new StreamContent(formFile.OpenReadStream());
                content.Add(streamContent, "file", formFile.FileName);
            }

            var data = new StringContent(JsonSerializer.Serialize(movie));
            content.Add(data, "movie");

            request.Content = content;
            var response = await _client.SendAsync(request, CancellationToken.None);
            if (response.IsSuccessStatusCode)
            {
                return;
            }
            _logger.LogError($"Error while creating object. Error:{response.StatusCode}");

            throw new HttpRequestException($"Error while updating item: {response.ReasonPhrase}");
        }
    }
}