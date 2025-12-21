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
    public class IndexModel(IMovieService movieService) : PageModel
    {
        private readonly IMovieService _movieService = movieService;

        public IList<Movie> Movies { get; set; } = [];
        public int CurrentPage;
        public int TotalPages;

        public async Task OnGetAsync(int pageNumber = 1)
        {
            var response = await _movieService.GetProductListAsync(null, pageNumber);
            if (response.IsSuccessfull && response.Data != null)
            {
                Movies = response.Data.Items;
                CurrentPage = response.Data.CurrentPage;
                TotalPages = response.Data.TotalPages;
            }
            else
            {
                Movies = [];
            }
        }
    }
}
