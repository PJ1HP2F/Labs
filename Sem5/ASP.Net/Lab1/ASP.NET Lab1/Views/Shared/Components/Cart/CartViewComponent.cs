using Microsoft.AspNetCore.Mvc;

namespace ASP.NET_Lab1.Views.Shared.Components.Cart
{
    public class CartViewComponent : ViewComponent
    {
        public IViewComponentResult Invoke()
        {
            return View();
        }
    }
}
