using ASP.UI.Extensions;
using ASP.UI.Services.CartService;
using Microsoft.AspNetCore.Mvc;

namespace ASP.Views.Shared.Components.Cart
{
    public class CartViewComponent : ViewComponent
    {
        public IViewComponentResult Invoke()
        {
            var cart = HttpContext.Session.Get<SessionCart>("cart");
            return View(cart);
        }
    }
}
