using ASP.Domain.Entities;
using ASP.Domain.Models;

namespace ASP.BlazorWASM.Services
{
    public interface IDataService
    {
        event Action DataLoaded;

        List<Category> Categories { get; set; }
        ListModel<Movie> Items { get; set; }

        bool IsSuccessful { get; set; }
        string ErrorMessage { get; set; }

        int TotalPages { get; set; }
        int CurrentPage { get; set; }

        Category? SelectedCategory { get; set; }

        /// <summary> 
        /// Get list of all items
        /// </summary> 
        /// <param name="pageNumber">list page number</param> 
        /// <returns></returns> 
        public Task GetProductListAsync(int pageNumber = 1);


        /// <summary> 
        /// Get list of categories
        /// </summary> 
        /// <returns></returns> 
        public Task GetCategoryListAsync();
    }
}
