using ASP.API.Data;
using ASP.Domain.Entities;
using ASP.Domain.Models;
using MediatR;

namespace ASP.API.Use_Cases
{
    public sealed record GetListOfMovies(string? CategoryNormalizedName, int PageNumber = 1, int PageSize = 3) : IRequest<ResponseData<ListModel<Movie>>>;

    public class GetListOfMoviesHandler(AppDbContext context) : IRequestHandler<GetListOfMovies, ResponseData<ListModel<Movie>>>
    {
        private readonly int _maxPageSize = 20;

        public async Task<ResponseData<ListModel<Movie>>> Handle(GetListOfMovies request, CancellationToken cancellationToken)
        {
            var pageSize = request.PageSize;
            if (pageSize > _maxPageSize)
            {
                pageSize = _maxPageSize;
            }

            var filteredByCategory = context.Movies.Where(i => request.CategoryNormalizedName == null || (i.Category != null && i.Category.NormalizedName == request.CategoryNormalizedName));

            int itemsCount = filteredByCategory.Count();
            int pageCount = (int)Math.Ceiling((double)itemsCount / pageSize);

            if (request.PageNumber > pageCount)
            {
                return await Task.FromResult(ResponseData<ListModel<Movie>>.Error("No such page"));
            }

            var filtered = filteredByCategory.Skip((request.PageNumber - 1) * pageSize).Take(pageSize);

            var list = new ListModel<Movie>() { Items = [.. filtered], CurrentPage = request.PageNumber, TotalPages = pageCount };
            return await Task.FromResult(ResponseData<ListModel<Movie>>.Success(list));
        }
    }
}
