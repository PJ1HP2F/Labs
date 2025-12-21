using ASP.API.Data;
using ASP.Domain.Entities;
using ASP.Domain.Models;
using MediatR;

namespace ASP.API.Use_Cases
{
    public sealed record GetMovieById(int id) : IRequest<ResponseData<Movie>>;

    public class GetMovieByIdHandler(AppDbContext context) : IRequestHandler<GetMovieById, ResponseData<Movie>>
    {
        public async Task<ResponseData<Movie>> Handle(GetMovieById request, CancellationToken cancellationToken)
        {
            var movie = context.Movies.FirstOrDefault(i => i.Id == request.id);
            if (movie == null)
            {
                return await Task.FromResult(ResponseData<Movie>.Error("Movie not found"));
            }
            return await Task.FromResult(ResponseData<Movie>.Success(movie));
        }
    }
}
