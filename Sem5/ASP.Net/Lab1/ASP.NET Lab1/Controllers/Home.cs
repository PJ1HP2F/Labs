using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace ASP.NET_Lab1.Controllers
{
    public class Home : Controller
    {
        // GET: Home
        public ActionResult Index()
        {
            return View();
        }
    }
}
