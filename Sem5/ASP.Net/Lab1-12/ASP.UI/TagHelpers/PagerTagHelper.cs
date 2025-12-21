using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.AspNetCore.Razor.TagHelpers;

namespace ASP.UI.TagHelpers
{
    [HtmlTargetElement("pager")]
    public class PagerTagHelper(LinkGenerator linkGenerator, IHttpContextAccessor httpContextAccessor) : TagHelper
    {
        private readonly LinkGenerator _linkGenerator = linkGenerator;
        private readonly IHttpContextAccessor _httpContextAccessor = httpContextAccessor;

        public int PageNumber { get; set; }
        public int TotalPages { get; set; }
        public string Category { get; set; }
        public bool Admin { get; set; }

        public override void Process(TagHelperContext context, TagHelperOutput output)
        {
            output.TagName = "ul";

            output.Attributes.Add("class", "pagination justify-content-center");

            if (PageNumber > 1)
            {
                output.Content.AppendHtml(GeneratePageItem(PageNumber - 1, "Previous"));
            }

            for (int i = PageNumber - 2 > 0 ? PageNumber - 2 : 1; i <= (PageNumber + 2 <= TotalPages ? PageNumber + 2 : TotalPages); ++i)
            {
                output.Content.AppendHtml(GeneratePageItem(i, $"{i}"));
            }

            if (PageNumber < TotalPages)
            {
                output.Content.AppendHtml(GeneratePageItem(PageNumber + 1, "Next"));
            }
        }

        private TagBuilder GeneratePageItem(int pageNumber, string text)
        {
            var li = new TagBuilder("li");
            li.AddCssClass("page-item");
            if (pageNumber == PageNumber)
            {
                li.AddCssClass("active");
            }

            var a = new TagBuilder("a");
            a.AddCssClass("page-link");
            a.Attributes["href"] = GeneratePageLink(pageNumber);
            a.InnerHtml.Append(text);

            li.InnerHtml.AppendHtml(a);
            return li;
        }

        private string GeneratePageLink(int pageNumber)
        {
            var httpContext = _httpContextAccessor.HttpContext;
            ArgumentNullException.ThrowIfNull(httpContext);

            string? url = null;

            var values = new RouteValueDictionary()
            {
                { "pageNumber", pageNumber }
            };

            if (!string.IsNullOrEmpty(Category))
            {
                values["category"] = Category;
            }

            if (Admin)
            {
                values["area"] = "Admin";
                url = _linkGenerator.GetPathByPage(page: "/items/Index", values: values, httpContext: httpContext);
            }
            else
            {
                url = _linkGenerator.GetPathByAction(action: "Index", controller: "Product", values: values, httpContext: httpContext);
            }

            return url ?? "#";
        }
    }
}
