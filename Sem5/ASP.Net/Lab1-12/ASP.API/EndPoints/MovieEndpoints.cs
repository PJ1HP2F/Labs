using Microsoft.EntityFrameworkCore;
using ASP.API.Data;
using ASP.Domain.Entities;
using Microsoft.AspNetCore.Http.HttpResults;
using Microsoft.AspNetCore.OpenApi;
using ASP.Domain.Models;
using MediatR;
using ASP.API.Use_Cases;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Caching.Hybrid;
namespace ASP.API.EndPoints;

public static class MovieEndpoints
{
    public static void MapMovieEndpoints(this IEndpointRouteBuilder routes)
    {
        var group = routes.MapGroup("/api/movies").WithTags(nameof(Movie)).DisableAntiforgery();

        group.MapGet("/{id:int}", async Task<Results<Ok<ResponseData<Movie>>, NotFound>> (IMediator mediator, int id) =>
        {
            var data = await mediator.Send(new GetMovieById(id));
            return data.IsSuccessfull ? TypedResults.Ok(data) : TypedResults.NotFound();
        })
        .WithName("GetMovieById")
        .WithOpenApi()
        .RequireAuthorization();

        group.MapGet("/{category:alpha?}", async (IMediator mediator, HybridCache cache, string? category, int pageNumber = 1, int pageSize = 3) =>
        {
            var cachedData = await cache.GetOrCreateAsync($"items_{category}_{pageNumber}",
                async data => await mediator.Send(new GetListOfMovies(category, pageNumber, pageSize)),
                options: new HybridCacheEntryOptions
                {
                    Expiration = TimeSpan.FromMinutes(1),
                    LocalCacheExpiration = TimeSpan.FromSeconds(30)
                });

            return TypedResults.Ok(cachedData);
        })
        .WithName("GetAllMovies")
        .WithOpenApi()
        .AllowAnonymous();

        //group.MapPut("/{id}", async Task<Results<Ok, NotFound>> (int id, Item item, AppDbContext db) =>
        //{
        //    var affected = await db.Items
        //        .Where(model => model.Id == id)
        //        .ExecuteUpdateAsync(setters => setters
        //            .SetProperty(m => m.Id, item.Id)
        //            .SetProperty(m => m.Name, item.Name)
        //            .SetProperty(m => m.Description, item.Description)
        //            .SetProperty(m => m.CategoryId, item.CategoryId)
        //            .SetProperty(m => m.Price, item.Price)
        //            .SetProperty(m => m.Image, item.Image)
        //            );
        //    return affected == 1 ? TypedResults.Ok() : TypedResults.NotFound();
        //})
        //.WithName("UpdateItem")
        //.WithOpenApi();

        group.MapPut("/{id}", async Task<Results<Ok, NotFound>> ([FromForm] string movie, [FromForm] IFormFile? file, AppDbContext db, IMediator mediator) =>
        {
            var updatedMovie = JsonSerializer.Deserialize<Movie>(movie);
            if (file != null)
            {
                await mediator.Send(new DeleteImage(updatedMovie!.Id));
                updatedMovie!.Image = await mediator.Send(new SaveImage(file));
            }
            var affected = await db.Movies
                .Where(model => model.Id == updatedMovie!.Id)
                .ExecuteUpdateAsync(setters => setters
                    .SetProperty(m => m.Id, updatedMovie!.Id)
                    .SetProperty(m => m.Name, updatedMovie!.Name)
                    .SetProperty(m => m.Description, updatedMovie!.Description)
                    .SetProperty(m => m.CategoryId, updatedMovie!.CategoryId)
                    .SetProperty(m => m.Price, updatedMovie!.Price)
                    .SetProperty(m => m.Image, updatedMovie!.Image)
                    );
            return affected == 1 ? TypedResults.Ok() : TypedResults.NotFound();
        })
        .WithName("UpdateMovie")
        .WithOpenApi()
        .RequireAuthorization("admin");

        //group.MapPost("/", async (Item item, AppDbContext db) =>
        //{
        //    db.Items.Add(item);
        //    await db.SaveChangesAsync();
        //    return TypedResults.Created($"/api/Item/{item.Id}",item);
        //})
        //.WithName("CreateItem")
        //.WithOpenApi();

        group.MapPost("/", async ([FromForm] string movie, [FromForm] IFormFile? file, AppDbContext db, IMediator mediator) =>
        {
            var newMovie = JsonSerializer.Deserialize<Movie>(movie);
            if (file != null)
            {
                newMovie!.Image = await mediator.Send(new SaveImage(file));
            }
            db.Movies.Add(newMovie!);
            await db.SaveChangesAsync();
            return TypedResults.Created($"/api/Movie/{newMovie!.Id}", newMovie);
        })
        .WithName("CreateMovie")
        .WithOpenApi()
        .RequireAuthorization("admin");

        group.MapDelete("/{id}", async Task<Results<Ok, NotFound>> (int id, AppDbContext db, IMediator mediator) =>
        {
            await mediator.Send(new DeleteImage(id));
            var affected = await db.Movies
                .Where(model => model.Id == id)
                .ExecuteDeleteAsync();
            return affected == 1 ? TypedResults.Ok() : TypedResults.NotFound();
        })
        .WithName("DeleteMovie")
        .WithOpenApi()
        .RequireAuthorization("admin");
    }
}
