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
    public class DeleteModel(IMovieService movieService) : PageModel
    {
        private readonly IMovieService _movieService = movieService;

        [BindProperty]
        public Movie Movie { get; set; } = default!;

        public async Task<IActionResult> OnGetAsync(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var movie = await _movieService.GetProductByIdAsync(id.Value);

            if (movie is not null)
            {
                Movie = movie.Data;

                return Page();
            }

            return NotFound();
        }

        public async Task<IActionResult> OnPostAsync(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            await _movieService.DeleteProductAsync(id.Value);

            return RedirectToPage("./Index");
        }
    }
}
