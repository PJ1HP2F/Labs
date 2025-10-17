namespace ASP.NET_Lab1.UI.Services.CategoryService
{
    public class MemoryCategoryService : ICategoryService
    {
        public Task<ResponseData<List<Category>>> GetCategoryListAsync()
        {
            var categories = new List<Category>
        {
            new Category {Id=1, Name="Триллеры", NormalizedName="thriller"},
            new Category {Id=2, Name="Фантастика", NormalizedName="science-fiction"},
            new Category {Id=3, Name="Детективы", NormalizedName="detective"},
            new Category {Id=4, Name="Для детей", NormalizedName="for-children"},
            new Category {Id=5, Name="Для взрослых", NormalizedName="for-adults"},
        };
            var result = ResponseData<List<Category>>.Success(categories);

            return Task.FromResult(result);
        }
    }
}
