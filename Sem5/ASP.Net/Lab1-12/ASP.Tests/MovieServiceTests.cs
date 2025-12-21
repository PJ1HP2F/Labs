using ASP.API.Data;
using ASP.Domain.Entities;
using ASP.API.Use_Cases;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ASP.Tests
{
    public class MovieServiceTests
    {
        private AppDbContext CreateInMemoryDbContext()
        {
            var options = new DbContextOptionsBuilder<AppDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            var context = new AppDbContext(options);
            return context;
        }

        [Fact]
        public async Task GetMovieListAsync_ReturnsFirstPageWithThreeItemsAndCalculatesTotalPagesCorrectly()
        {
            var context = CreateInMemoryDbContext();

            var categories = new List<Category>
            {
                new() { Id = 1, Name = "Триллеры", NormalizedName = "thriller"},
                new() { Id = 2, Name = "Фантастика", NormalizedName = "science-fiction"},
                new() { Id = 3, Name = "Детективы", NormalizedName = "detective"},
            };

            await context.Categories.AddRangeAsync(categories);
            await context.SaveChangesAsync();

            context.Movies.AddRange(
                new() { Name = "Item 1", NormalizedName = "item1", Description = "Description 1", Price = 200, Category = categories[0] },
                new() { Name = "Item 2", NormalizedName = "item2", Description = "Description 2", Price = 210, Category = categories[1] },
                new() { Name = "Item 3", NormalizedName = "item3", Description = "Description 3", Price = 220, Category = categories[1] },
                new() { Name = "Item 4", NormalizedName = "item4", Description = "Description 4", Price = 230, Category = categories[2] },
                new() { Name = "Item 5", NormalizedName = "item5", Description = "Description 5", Price = 240, Category = categories[0] },
                new() { Name = "Item 6", NormalizedName = "item6", Description = "Description 6", Price = 250, Category = categories[1] }
            );

            await context.SaveChangesAsync();

            var request = new GetListOfMovies(null);
            var handler = new GetListOfMoviesHandler(context);
            var result = await handler.Handle(request, CancellationToken.None);

            Assert.True(result.IsSuccessfull);
            Assert.NotNull(result.Data);
            Assert.Equal(3, result.Data.Items.Count);

            int totalPages = (int)Math.Ceiling(6 / (double)3);
            Assert.Equal(totalPages, result.Data.TotalPages);
        }

        [Fact]
        public async Task GetMovieListAsync_ReturnsCorrectPage_WhenSpecificPageIsRequested()
        {
            var context = CreateInMemoryDbContext();

            var categories = new List<Category>
            {
                new() { Id = 1, Name = "Триллеры", NormalizedName = "thriller"}
            };

            await context.Categories.AddRangeAsync(categories);
            await context.SaveChangesAsync();

            context.Movies.AddRange(
                new() { Name = "Item 1", NormalizedName = "item1", Description = "Description 1", Price = 200, Category = categories[0] },
                new() { Name = "Item 2", NormalizedName = "item2", Description = "Description 2", Price = 210, Category = categories[0] },
                new() { Name = "Item 3", NormalizedName = "item3", Description = "Description 3", Price = 220, Category = categories[0] },
                new() { Name = "Item 4", NormalizedName = "item4", Description = "Description 4", Price = 230, Category = categories[0] },
                new() { Name = "Item 5", NormalizedName = "item5", Description = "Description 5", Price = 240, Category = categories[0] },
                new() { Name = "Item 6", NormalizedName = "item6", Description = "Description 6", Price = 250, Category = categories[0] }
            );

            await context.SaveChangesAsync();

            int requestedPageNo = 2;
            int pageSize = 3;

            var request = new GetListOfMovies(null, requestedPageNo, pageSize);
            var handler = new GetListOfMoviesHandler(context);
            var result = await handler.Handle(request, CancellationToken.None);

            Assert.True(result.IsSuccessfull);
            Assert.NotNull(result.Data);
            Assert.Equal(3, result.Data.Items.Count);
            Assert.Equal(requestedPageNo, result.Data.CurrentPage);

            Assert.Contains(result.Data.Items, m => m.Name == "Item 4");
            Assert.Contains(result.Data.Items, m => m.Name == "Item 5");
            Assert.Contains(result.Data.Items, m => m.Name == "Item 6");
        }

        [Fact]
        public async Task GetMovieListAsync_FiltersItemsByCategoryCorrectly()
        {
            var context = CreateInMemoryDbContext();

            var categories = new List<Category>
            {
                new() { Id = 1, Name = "Триллеры", NormalizedName = "thriller"},
                new() { Id = 2, Name = "Фантастика", NormalizedName = "science-fiction"},
            };

            await context.Categories.AddRangeAsync(categories);
            await context.SaveChangesAsync();

            context.Movies.AddRange(
                new() { Name = "Item 1", NormalizedName = "item1", Description = "Description 1", Price = 200, Category = categories[0] },
                new() { Name = "Item 2", NormalizedName = "item2", Description = "Description 2", Price = 210, Category = categories[0] },
                new() { Name = "Item 3", NormalizedName = "item3", Description = "Description 3", Price = 220, Category = categories[1] },
                new() { Name = "Item 4", NormalizedName = "item4", Description = "Description 4", Price = 230, Category = categories[1] },
                new() { Name = "Item 5", NormalizedName = "item5", Description = "Description 5", Price = 240, Category = categories[1] }
            );

            await context.SaveChangesAsync();

            string categoryNormalizedName = "science-fiction".ToLower();

            var request = new GetListOfMovies(categoryNormalizedName);
            var handler = new GetListOfMoviesHandler(context);
            var result = await handler.Handle(request, CancellationToken.None);

            Assert.True(result.IsSuccessfull);
            Assert.NotNull(result.Data);
            Assert.Equal(3, result.Data.Items.Count);

            Assert.Equal(1, result.Data.CurrentPage);
            Assert.Equal(1, result.Data.TotalPages);


            foreach (var item in result.Data.Items)
            {
                Assert.Equal("Фантастика", item.Category.Name);
            }
        }

        [Fact]
        public async Task GetProductListAsync_DoesNotAllowPageSizeGreaterThanMaxPageSize()
        {
            var context = CreateInMemoryDbContext();

            var categories = new List<Category>
            {
                new() { Id = 1, Name = "Триллеры", NormalizedName = "thrillers"}
            };

            await context.Categories.AddRangeAsync(categories);
            await context.SaveChangesAsync();

            for (int i = 1; i <= 25; i++)
            {
                context.Movies.Add(new() { Name = $"Item {i}", NormalizedName = $"item{i}", Description = $"Description {i}", Price = 200 + 10 * i, Category = categories[0] });
            }

            await context.SaveChangesAsync();

            int requestedPageSize = 50;
            int maxPageSize = 20;

            var request = new GetListOfMovies(null, 1, requestedPageSize);
            var handler = new GetListOfMoviesHandler(context);
            var result = await handler.Handle(request, CancellationToken.None);

            Assert.True(result.IsSuccessfull);
            Assert.NotNull(result.Data);
            Assert.Equal(maxPageSize, result.Data.Items.Count);
            Assert.Equal(1, result.Data.CurrentPage);
        }
        [Fact]
        public async Task GetProductListAsync_ReturnsError_WhenPageNumberExceedsTotalPages()
        {
            var context = CreateInMemoryDbContext();

            var categories = new List<Category>
            {
                new() { Id = 1, Name = "Триллеры", NormalizedName = "thriller"}
            };

            await context.Categories.AddRangeAsync(categories);
            await context.SaveChangesAsync();

            for (int i = 1; i <= 5; i++)
            {
                context.Movies.Add(new() { Name = $"Item {i}", NormalizedName = $"item{i}", Description = $"Description {i}", Price = 200 + 10 * i, Category = categories[0] });
            }

            await context.SaveChangesAsync();

            int requestedPageNo = 3;
            int pageSize = 3;

            var request = new GetListOfMovies(null, requestedPageNo, pageSize);
            var handler = new GetListOfMoviesHandler(context);
            var result = await handler.Handle(request, CancellationToken.None);

            Assert.False(result.IsSuccessfull);
            Assert.Equal("No such page", result.ErrorMessage);
        }
    }
}
