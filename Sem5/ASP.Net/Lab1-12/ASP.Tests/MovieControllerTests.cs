using ASP.Domain.Entities;
using ASP.Domain.Models;
using ASP.UI.Controllers;
using ASP.UI.Services.CategoryService;
using ASP.UI.Services.MovieService;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using NSubstitute;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ASP.Tests
{
    public class MovieControllerTests
    {
        private readonly IMovieService _productService = Substitute.For<IMovieService>();
        private readonly ICategoryService _categoryService = Substitute.For<ICategoryService>();

        private ProductController CreateController()
        {
            return new ProductController(_categoryService, _productService);
        }

        [Fact]
        public async Task Index_ReturnsNotFound_WhenCategoriesNotLoaded()
        {
            var controller = CreateController();
            _categoryService.GetCategoryListAsync().Returns(Task.FromResult(ResponseData<List<Category>>.Error("Error receiving category list")));

            var result = await controller.Index(null);

            var notFoundResult = Assert.IsType<NotFoundObjectResult>(result);
            Assert.Equal("Error receiving category list", notFoundResult.Value);
        }
        [Fact]
        public async Task Index_ReturnsNotFound_WhenItemsNotLoaded()
        {
            var controller = CreateController();
            var category = "TestCategory";
            _categoryService.GetCategoryListAsync().Returns(Task.FromResult(ResponseData<List<Category>>.Success([new() { Name = "TestCategory", NormalizedName = "TESTCATEGORY" }])));
            _productService.GetProductListAsync(category, 1).Returns(Task.FromResult(ResponseData<ListModel<Movie>>.Error("Error receiving items")));

            var result = await controller.Index(category);

            var notFoundResult = Assert.IsType<NotFoundObjectResult>(result);
            Assert.Equal("Error receiving items", notFoundResult.Value);
        }
        [Fact]
        public async Task Index_PopulatesViewDataWithCategories_WhenCategoriesAreSuccessfullyLoaded()
        {
            var controller = CreateController();
            var httpContext = new DefaultHttpContext();
            controller.ControllerContext = new ControllerContext()
            {
                HttpContext = httpContext
            };
            httpContext.Request.Headers["X-Requested-With"] = "";

            var expectedCategories = new List<Category> {
                new() { Name = "Триллеры", NormalizedName = "thriller" },
                new() { Name = "Фантастика", NormalizedName = "science-fiction" }
            };
            _categoryService.GetCategoryListAsync().Returns(Task.FromResult(ResponseData<List<Category>>.Success(expectedCategories)));
            _productService.GetProductListAsync(null, 1).Returns(Task.FromResult(ResponseData<ListModel<Movie>>.Success(new ListModel<Movie>())));

            var result = await controller.Index(null);

            var viewResult = Assert.IsType<ViewResult>(result);
            Assert.NotNull(viewResult.ViewData["Categories"]);
            var categoriesInViewData = viewResult.ViewData["Categories"] as List<Category>;
            Assert.Equal(expectedCategories, categoriesInViewData);
        }
        [Fact]
        public async Task Index_SetsCurrentCategoryToAll_WhenCategoryIsNull()
        {
            var controller = CreateController();
            var httpContext = new DefaultHttpContext();
            controller.ControllerContext = new ControllerContext()
            {
                HttpContext = httpContext
            };

            _categoryService.GetCategoryListAsync().Returns(Task.FromResult(ResponseData<List<Category>>.Success(new List<Category>())));
            _productService.GetProductListAsync(null, 1).Returns(Task.FromResult(ResponseData<ListModel<Movie>>.Success(new ListModel<Movie>())));

            var result = await controller.Index(null);

            var viewResult = Assert.IsType<ViewResult>(result);
            Assert.Equal("All items", viewResult.ViewData["CurrentCategory"]);
        }
        [Fact]
        public async Task Index_SetsCurrentCategoryCorrectly_WhenCategoryIsSpecified()
        {
            var controller = CreateController();
            var httpContext = new DefaultHttpContext();
            controller.ControllerContext = new ControllerContext()
            {
                HttpContext = httpContext
            };

            string category = "science-fiction";
            var categories = new List<Category>
            {
                new() { Id = 1, Name = "Триллеры", NormalizedName = "thrillers"},
                new() { Id = 2, Name = "Фантастика", NormalizedName = "science-fiction"},
                new() { Id = 3, Name = "Сategory3", NormalizedName = "category3"},
                new() { Id = 4, Name = "Сategory4", NormalizedName = "category4"},
                new() { Id = 5, Name = "Сategory5", NormalizedName = "category5"},
                new() { Id = 6, Name = "Сategory6", NormalizedName = "category6"},
                new() { Id = 7, Name = "Сategory7", NormalizedName = "category7"},
                new() { Id = 8, Name = "Сategory8", NormalizedName = "category8"},
                new() { Id = 9, Name = "Сategory9", NormalizedName = "category9"},
            };
            _categoryService.GetCategoryListAsync().Returns(Task.FromResult(ResponseData<List<Category>>.Success(categories)));
            _productService.GetProductListAsync(category, 1).Returns(Task.FromResult(ResponseData<ListModel<Movie>>.Success(new ListModel<Movie>())));

            var result = await controller.Index(category);

            var viewResult = Assert.IsType<ViewResult>(result);
            Assert.Equal("Фантастика", viewResult.ViewData["CurrentCategory"]);
        }
        [Fact]
        public async Task Index_ReturnsViewWithItemListModel_WhenDataIsSuccessfullyLoaded()
        {
            var controller = CreateController();
            var httpContext = new DefaultHttpContext();
            controller.ControllerContext = new ControllerContext
            {
                HttpContext = httpContext
            };

            string category = "science-fiction";
            var categories = new List<Category>
            {
                new() { Id = 1, Name = "Триллеры", NormalizedName = "thrillers"},
                new() { Id = 2, Name = "Фантастика", NormalizedName = "science-fiction"},
                new() { Id = 3, Name = "Сategory3", NormalizedName = "category3"},
                new() { Id = 4, Name = "Сategory4", NormalizedName = "category4"},
                new() { Id = 5, Name = "Сategory5", NormalizedName = "category5"},
                new() { Id = 6, Name = "Сategory6", NormalizedName = "category6"},
                new() { Id = 7, Name = "Сategory7", NormalizedName = "category7"},
                new() { Id = 8, Name = "Сategory8", NormalizedName = "category8"},
                new() { Id = 9, Name = "Сategory9", NormalizedName = "category9"},
            };
            var expectedItems = new ListModel<Movie>
            {
                Items = [new(), new(), new()],
                CurrentPage = 1,
                TotalPages = 2
            };

            _categoryService.GetCategoryListAsync().Returns(Task.FromResult(ResponseData<List<Category>>.Success(categories)));
            _productService.GetProductListAsync(category, 1).Returns(Task.FromResult(ResponseData<ListModel<Movie>>.Success(expectedItems)));

            var result = await controller.Index(category);

            var viewResult = Assert.IsType<ViewResult>(result);
            var model = Assert.IsType<ListModel<Movie>>(viewResult.Model);
            Assert.Equal(expectedItems, model);
        }
    }
}
