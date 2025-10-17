namespace ASP.NET_Lab1.UI.Services.MovieService
{
    public interface IMovieService
    {
        Task<ResponseData<ListModel<Movie>>> GetProductListAsync(string? categoryNormalizedName, int pageNumber = 1);

        Task<ResponseData<Movie>> GetProductByIdAsync(int id);
       
        Task<ResponseData<Movie>> CreateProductAsync(Movie item, IFormFile? formFile);
        
        Task UpdateProductAsync(int id, Movie item, IFormFile? formFile);
        
        Task DeleteProductAsync(int id);
    }
}
