namespace ASP.UI.Services.FileServices
{
    public interface IFileService
    {
        Task<string> SaveFileAsync(IFormFile file);
    }
}
