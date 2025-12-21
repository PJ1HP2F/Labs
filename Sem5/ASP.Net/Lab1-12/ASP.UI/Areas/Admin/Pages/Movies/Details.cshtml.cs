using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Microsoft.EntityFrameworkCore;
using ASP.Domain.Entities;
using ASP.UI.Services.MovieService;

namespace ASP.UI.Areas.Admin.Pages.Movies
{
    public class DetailsModel(IMovieService movieService) : PageModel
    {
        private readonly IMovieService _movieService = movieService;

        public Movie Movie { get; set; } = new();

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
            return Page();
        }
    }
}
