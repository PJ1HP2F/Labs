using ASP.NET_Lab1.UI.Services.CategoryService;
using Microsoft.AspNetCore.Mvc;

namespace ASP.NET_Lab1.UI.Services.MovieService
{
    public class MemoryMovieService : IMovieService
    {
        private readonly IConfiguration _config;
        private List<Category> _categories;
        private List<Movie> _movies;

        public MemoryMovieService([FromServices] IConfiguration config, ICategoryService categoryService)
        {
            _config = config;
            _categories = categoryService.GetCategoryListAsync().Result.Data!;

            SetupData();
        }

        public void SetupData()
        {
            _movies = new List<Movie>
            {
                new Movie {Id = 1, Name = "Начало", Description = "Лучший фильм",
                           Price = 100, Image = "../Images/inception.jpg", Category = _categories.Find(c => c.NormalizedName == "science-fiction")},
                new Movie {Id = 2, Name = "Семь", Description = "Концовка огонь",
                           Price = 10, Image = "../Images/seven.jpg", Category = _categories.Find(c => c.NormalizedName == "thriller")},
                new Movie {Id = 3, Name = "Лунтик", Description = "Смешарики лучше",
                           Price = 2, Image = "../Images/luntik.jpg", Category = _categories.Find(c => c.NormalizedName == "for-children")},
                new Movie {Id = 4, Name = "Исчезнувшая", Description = "Триллер", Price = 12,
                           Image = "../Images/gone.jpg", Category = _categories.Find(c => c.NormalizedName == "thriller")}
            };
        }

        public async Task<ResponseData<Movie>> CreateProductAsync(Movie item, IFormFile? formFile)
        {
            throw new NotImplementedException();
        }

        public async Task DeleteProductAsync(int id)
        {
            throw new NotImplementedException();
        }

        public async Task<ResponseData<Movie>> GetProductByIdAsync(int id)
        {
            throw new NotImplementedException();
        }

        public async Task<ResponseData<ListModel<Movie>>> GetProductListAsync(string? categoryNormalizedName, int pageNumber = 1)
        {
            var filteredByCategory = _movies.Where(i => categoryNormalizedName == null || (i.Category != null && i?.Category?.NormalizedName == categoryNormalizedName));

            int itemsPerPage = _config.GetValue<int>("PageSettings:ItemsPerPage");
            int itemsCount = filteredByCategory.Count();
            int pageCount = (int)Math.Ceiling((double)itemsCount / itemsPerPage);

            var filtered = filteredByCategory.Skip((pageNumber - 1) * itemsPerPage).Take(itemsPerPage);

            var list = new ListModel<Movie>() { Items = [.. filtered], CurrentPage = pageNumber, TotalPages = pageCount };
            return await Task.FromResult(ResponseData<ListModel<Movie>>.Success(list));
        }

        public async Task UpdateProductAsync(int id, Movie item, IFormFile? formFile)
        {
            throw new NotImplementedException();
        }
    }
}
