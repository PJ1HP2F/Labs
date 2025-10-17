using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using ASP.NET_Lab1.Models;
using Microsoft.AspNetCore.Mvc.Rendering;

namespace ASP.NET_Lab1.Controllers
{
    public class HomeController : Controller
    {
        // GET: Home
        public ActionResult Index()
        {
            ViewData["LabWork"] = "Лабораторная работа 2";

            var items = new List<ListDemo>
            {
                new ListDemo { Id = 1, Name = "Элемент 1"},
                new ListDemo { Id = 2, Name = "Элемент 2"},
                new ListDemo { Id = 3, Name = "Элемент 3"}
            };

            ViewBag.Items = new SelectList(items, "Id", "Name");

            return View();
        }
    }
}
