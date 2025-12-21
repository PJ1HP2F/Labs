using ASP.Domain.Entities;
using ASP.Domain.Models;
using Microsoft.AspNetCore.Components.WebAssembly.Authentication;
using Microsoft.AspNetCore.Http;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;

namespace ASP.BlazorWASM.Services
{
    public class DataService(HttpClient httpClient, IConfiguration configuration, IAccessTokenProvider accessTokenProvider) : IDataService
    {
        private readonly HttpClient _httpClient = httpClient;
        private readonly IAccessTokenProvider _accessTokenProvider = accessTokenProvider;

        private readonly string _apiUrl = configuration["ApiSettings:BaseUrl"]!;
        private readonly string _defaultPageSize = configuration["ApiSettings:PageSize"]!;


        public List<Category> Categories { get; set; } = [];
        public ListModel<Movie> Items { get; set; } = new();
        public bool IsSuccessful { get; set; }
        public string ErrorMessage { get; set; } = string.Empty;
        public int TotalPages { get; set; }
        public int CurrentPage { get; set; } = 1;
        public Category? SelectedCategory { get; set; } = null;

        public event Action DataLoaded = delegate { };

        private async Task<string> GetJwtTokenAsync()
        {
            var tokenRequest = await _accessTokenProvider.RequestAccessToken();
            if (tokenRequest.TryGetToken(out var token))
            {
                return token.Value;
            }
            throw new FieldAccessException("Unable to get a token");
        }

        public async Task GetCategoryListAsync()
        {
            try
            {
                var token = await GetJwtTokenAsync();

                var request = new HttpRequestMessage(HttpMethod.Get, $"{_apiUrl}categories/");
                request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);

                var response = await _httpClient.SendAsync(request);

                if (response.IsSuccessStatusCode)
                {
                    var categories = await response.Content.ReadFromJsonAsync<List<Category>>();

                    IsSuccessful = true;
                    Categories = categories;
                    DataLoaded.Invoke();
                }
                else
                {
                    IsSuccessful = false;
                    ErrorMessage = $"Error while getting category list: {response.StatusCode} {response.ReasonPhrase}";
                }
            }
            catch (Exception e)
            {
                IsSuccessful = false;
                ErrorMessage = e.Message;
            }
        }

        public async Task GetProductListAsync(int pageNumber = 1)
        {
            try
            {
                var token = await GetJwtTokenAsync();

                var route = new StringBuilder("movies/");
                List<KeyValuePair<string, string>> queryData = [];

                if (SelectedCategory != null)
                {
                    queryData.Add(KeyValuePair.Create("category", SelectedCategory.NormalizedName));
                }

                if (pageNumber > 1)
                {
                    queryData.Add(KeyValuePair.Create("pageNumber", $"{CurrentPage}"));
                }

                if (!_defaultPageSize.Equals("3"))
                {
                    queryData.Add(KeyValuePair.Create("pageSize", $"{_defaultPageSize}"));
                }

                if (queryData.Count > 0)
                {
                    route.Append(QueryString.Create(queryData));
                }

                var request = new HttpRequestMessage(HttpMethod.Get, $"{_apiUrl}{route}");
                request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);

                var response = await _httpClient.SendAsync(request);

                if (response.IsSuccessStatusCode)
                {
                    var items = await response.Content.ReadFromJsonAsync<ResponseData<ListModel<Movie>>>();

                    if ((items == null || !items.IsSuccessfull || items.Data == null) && items.ErrorMessage != "No such page")
                    {
                        IsSuccessful = false;
                        ErrorMessage = items?.ErrorMessage ?? "Unknown error";
                    }
                    else if (items.ErrorMessage == "No such page")
                    {
                        IsSuccessful = true;
                        Items.CurrentPage = 1;
                        Items.TotalPages = 1;
                        Items.Items = [];
                        DataLoaded.Invoke();
                    }
                    else
                    {
                        IsSuccessful = true;
                        Items = items.Data;
                        CurrentPage = Items.CurrentPage;
                        TotalPages = Items.TotalPages;
                        DataLoaded.Invoke();
                    }
                }
                else
                {
                    IsSuccessful = false;
                    ErrorMessage = $"Error while getting item list: {response.StatusCode} {response.ReasonPhrase}";
                }
            }
            catch (Exception e)
            {
                IsSuccessful = false;
                ErrorMessage = e.Message;
            }
        }
    }
}
