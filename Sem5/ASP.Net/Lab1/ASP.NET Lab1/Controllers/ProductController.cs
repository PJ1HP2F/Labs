using ASP.NET_Lab1.UI.Services.CategoryService;
using ASP.NET_Lab1.UI.Services.MovieService;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace ASP.NET_Lab1.UI.Controllers
{
    public class ProductController(ICategoryService categoryService, IMovieService movieService) : Controller
    {
        ICategoryService _categoryService = categoryService;
        IMovieService _movieService = movieService;

        public async Task<IActionResult> Index(string? category, int pageNumber = 1)
        {
            var categoryResponse = await _categoryService.GetCategoryListAsync();

            var currentCategory = category;
            if (string.IsNullOrEmpty(currentCategory))
            {
                currentCategory = "All items";
            }
            else
            {
                var selectedCategory = categoryResponse.Data!.FirstOrDefault(c => c.NormalizedName == currentCategory);
                currentCategory = selectedCategory?.Name ?? "All items";
            }

            var productResponse = await _movieService.GetProductListAsync(category, pageNumber);

            ViewData["currentCategory"] = currentCategory;
            ViewBag.Categories = categoryResponse.Data;
            ViewBag.CurrentPage = pageNumber;
            ViewBag.TotalPages = productResponse.Data!.TotalPages;

            return View(productResponse.Data);
        }
    }
}
