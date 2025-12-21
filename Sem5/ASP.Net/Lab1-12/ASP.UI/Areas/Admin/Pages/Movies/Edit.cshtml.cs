using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using ASP.Domain.Entities;
using ASP.UI.Services.CategoryService;
using System.Net.Http;
using ASP.UI.Services.MovieService;
using System.Net.Http.Headers;

namespace ASP.UI.Areas.Admin.Pages.Movies
{
    public class EditModel(IMovieService movieService, ICategoryService categoryService, IHttpClientFactory httpClientFactory) : PageModel
    {
        private readonly IMovieService _movieService = movieService;
        private readonly ICategoryService _categoryService = categoryService;
        private readonly IHttpClientFactory _httpClientFactory = httpClientFactory;

        [BindProperty]
        public Movie Movie { get; set; } = new();
        [BindProperty]
        public IFormFile? FormFile { get; set; }

        public async Task<IActionResult> OnGetAsync(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var response = await _movieService.GetProductByIdAsync(id.Value);
            if (!response.IsSuccessfull || response.Data == null)
            {
                return NotFound();
            }

            Movie = response.Data;
            var categories = await _categoryService.GetCategoryListAsync();
            ViewData["CategoryId"] = new SelectList(categories.Data, "Id", "Name");

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
            //    if (!string.IsNullOrEmpty(Item.Image))
            //    {
            //        await DeleteImageFromApiAsync(Item.Image);
            //    }

            //    var imageUrl = await UploadImageToApiAsync(FormFile);
            //    if (string.IsNullOrEmpty(imageUrl))
            //    {
            //        ModelState.AddModelError("", "Unable to upload image");
            //        return Page();
            //    }

            //    Item.Image = imageUrl;
            //}

            await _movieService.UpdateProductAsync(Movie.Id, Movie, FormFile);

            return RedirectToPage("./Index");
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

        private async Task DeleteImageFromApiAsync(string imageUrl)
        {
            var client = _httpClientFactory.CreateClient();
            var apiUrl = "https://localhost:7002/api/files";

            var uri = new Uri(imageUrl);
            var fileName = Path.GetFileName(uri.LocalPath);

            var requestUri = $"{apiUrl}?fileName={fileName}";

            await client.DeleteAsync(requestUri);
        }
    }
}
