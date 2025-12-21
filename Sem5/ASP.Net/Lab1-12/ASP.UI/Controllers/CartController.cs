using ASP.UI.Services.MovieService;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace ASP.UI.Controllers
{
    public class CartController(IMovieService movieService, Cart cart) : Controller
    {
        private readonly IMovieService _movieService = movieService;
        private readonly Cart _cart = cart;

        public async Task<IActionResult> Add(int id, string returnUrl)
        {
            var item = await _movieService.GetProductByIdAsync(id);

            if (item != null && item.IsSuccessfull && item.Data != null)
            {
                _cart.AddToCart(item.Data);
            }

            return Redirect(returnUrl);
        }

        public IActionResult Remove(int id, string returnUrl)
        {
            _cart.RemoveItems(id);
            return Redirect(returnUrl);
        }

        public IActionResult Clear(string returnUrl)
        {
            _cart.ClearAll();
            return Redirect(returnUrl);
        }

        public IActionResult Index()
        {
            return View(_cart);
        }
    }
}
