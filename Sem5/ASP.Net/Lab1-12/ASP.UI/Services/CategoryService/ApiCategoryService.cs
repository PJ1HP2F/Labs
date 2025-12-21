using System.Text;
using System.Text.Json;

namespace API.UI.Services.CategoryService
{
    public class ApiCategoryService(HttpClient httpClient, ILogger<ApiCategoryService> logger) : ICategoryService
    {
        private readonly HttpClient _client = httpClient;
        private readonly ILogger _logger = logger;
        private readonly JsonSerializerOptions _serializerOptions = new() { PropertyNamingPolicy = JsonNamingPolicy.CamelCase };

        public async Task<ResponseData<List<Category>>> GetCategoryListAsync()
        {
            var urlString = new StringBuilder($"{_client.BaseAddress!.AbsoluteUri}");

            var response = await _client.GetAsync(new Uri(urlString.ToString()));
            if (response.IsSuccessStatusCode)
            {
                try
                {
                    return ResponseData<List<Category>>.Success((await response.Content.ReadFromJsonAsync<List<Category>>(_serializerOptions))!);
                }
                catch (JsonException ex)
                {
                    _logger.LogError($"Error: {ex.Message}");
                }
            }
            _logger.LogError($"Error while receiving data. Error:{response.StatusCode}");

            return ResponseData<List<Category>>.Error($"Error while receiving data. Error:{response.StatusCode}");
        }
    }
}