using MediatR;

namespace ASP.API.Use_Cases
{
    public sealed record SaveImage(IFormFile FormFile) : IRequest<string>;

    public class SaveImageHandler(IWebHostEnvironment environment, IHttpContextAccessor httpContextAccessor) : IRequestHandler<SaveImage, string>
    {
        public async Task<string> Handle(SaveImage request, CancellationToken cancellationToken)
        {
            var imagePath = Path.Combine(environment.WebRootPath, "Images");
            var host = httpContextAccessor.HttpContext!.Request.Host;

            var fileName = Guid.NewGuid().ToString() + Path.GetExtension(request.FormFile.FileName);
            var filePath = Path.Combine(imagePath, fileName);

            if (File.Exists(filePath))
            {
                File.Delete(filePath);
            }

            using (var fileStream = new FileStream(filePath, FileMode.Create))
            {
                await request.FormFile.CopyToAsync(fileStream, cancellationToken);
            }

            return await Task.FromResult($"https://{host}/Images/{fileName}");
        }
    }
}
