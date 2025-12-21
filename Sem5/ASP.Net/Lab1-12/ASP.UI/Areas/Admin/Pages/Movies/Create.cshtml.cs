using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Microsoft.AspNetCore.Mvc.Rendering;
using ASP.Domain.Entities;
using ASP.UI.Services.MovieService;
using System.Net.Http.Headers;

namespace ASP.UI.Areas.Admin.Pages.Movies
{
    public class CreateModel(IMovieService productService, ICategoryService categoryService, IHttpClientFactory httpClientFactory) : PageModel
    {
        private readonly IMovieService _productService = productService;
        private readonly ICategoryService _categoryService = categoryService;
        private readonly IHttpClientFactory _httpClientFactory = httpClientFactory;

        [BindProperty]
        public Movie Movie { get; set; } = new();
        [BindProperty]
        public IFormFile FormFile { get; set; } = null!;

        public async Task<IActionResult> OnGet()
        {
            var categoriesResponse = await _categoryService.GetCategoryListAsync();
            ViewData["CategoryId"] = new SelectList(categoriesResponse.Data, "Id", "Name");
            return Page();
        }

        public async Task<IActionResult> OnPostAsync()
        {
            if (!ModelState.IsValid)
            {
                return Page();
            }

            //if (FormFile != null)
            //{
            //    //var imageUrl = await UploadImageToApiAsync(FormFile);
            //    //if (string.IsNullOrEmpty(imageUrl))
            //    //{
            //    //    ModelState.AddModelError("", "Unable to upload image");
            //    //    return Page();
            //    //}

            //    Item.Image = imageUrl;
            //}

            var response = await _productService.CreateProductAsync(Movie, FormFile);
            if (response.IsSuccessfull)
            {
                return RedirectToPage("./Index");
            }

            ModelState.AddModelError("", "Unable to create item: " + response.ErrorMessage);
            return Page();
        }

        private async Task<string> UploadImageToApiAsync(IFormFile imageFile)
        {
            var client = _httpClientFactory.CreateClient();
            var apiUrl = "https://localhost:7002/api/files";

            using var content = new MultipartFormDataContent();
            using var fileStreamContent = new StreamContent(imageFile.OpenReadStream());
            fileStreamContent.Headers.ContentType = new MediaTypeHeaderValue(imageFile.ContentType);
            content.Add(fileStreamContent, "file", imageFile.FileName);

            var response = await client.PostAsync(apiUrl, content);

            if (response.IsSuccessStatusCode)
            {
                var imageUrl = await response.Content.ReadAsStringAsync();
                return imageUrl.Trim('"');
            }

            return "";
        }
    }
}
