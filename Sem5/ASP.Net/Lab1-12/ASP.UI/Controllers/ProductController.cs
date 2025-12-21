using ASP.UI.Services.CategoryService;
using ASP.UI.Services.MovieService;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using ASP.UI.Extensions;

namespace ASP.UI.Controllers
{
    [Route("Catalog")]
    public class ProductController(ICategoryService categoryService, IMovieService movieService) : Controller
    {
        ICategoryService _categoryService = categoryService;
        IMovieService _movieService = movieService;

        [Route("")]
        [Route("{category?}")]
        public async Task<IActionResult> Index(string? category, int pageNumber = 1)
        {
            var categoryResponse = await _categoryService.GetCategoryListAsync();
            if (!categoryResponse.IsSuccessfull)
            {
                return NotFound(categoryResponse.ErrorMessage);
            }

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
            if (!productResponse.IsSuccessfull)
            {
                return NotFound(productResponse.ErrorMessage);
            }

            ViewData["currentCategory"] = currentCategory;
            ViewBag.Categories = categoryResponse.Data;
            ViewBag.CurrentPage = pageNumber;
            ViewBag.TotalPages = productResponse.Data!.TotalPages;

            if (Request.IsAjaxRequest())
            {
                return PartialView("_ProductListPartial", productResponse.Data);
            }

            return View(productResponse.Data);
        }
    }
}
