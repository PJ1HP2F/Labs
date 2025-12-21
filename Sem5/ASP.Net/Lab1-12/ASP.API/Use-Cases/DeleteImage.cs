using ASP.API.Data;
using MediatR;

namespace ASP.API.Use_Cases
{
    public sealed record DeleteImage(int id) : IRequest;

    public class DeleteImageHandler(IWebHostEnvironment environment, AppDbContext context) : IRequestHandler<DeleteImage>
    {
        public Task Handle(DeleteImage request, CancellationToken cancellationToken)
        {
            var imagePath = Path.Combine(environment.WebRootPath, "Images");
            var movie = context.Movies.FirstOrDefault(i => i.Id == request.id);

            if (movie == null)
            {
                return Task.FromException(new InvalidDataException());
            }

            var fileName = movie.Image;
            var filePath = Path.Combine(imagePath, fileName!);

            if (File.Exists(filePath))
            {
                File.Delete(filePath);
            }

            return Task.CompletedTask;
        }
    }
}
