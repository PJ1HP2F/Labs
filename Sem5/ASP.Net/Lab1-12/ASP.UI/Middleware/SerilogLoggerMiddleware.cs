using Microsoft.AspNetCore.Http;

namespace ASP.UI.Middleware
{
    public class SerilogLoggerMiddleware(RequestDelegate requestDelegate, ILogger<SerilogLoggerMiddleware> logger)
    {
        private readonly RequestDelegate _requestDelegate = requestDelegate;
        private readonly ILogger<SerilogLoggerMiddleware> _logger = logger;

        public async Task Invoke(HttpContext httpContext)
        {
            try
            {
                await _requestDelegate(httpContext);

                if ((httpContext.Response.StatusCode < 200 || httpContext.Response.StatusCode >= 300) && httpContext.Response.StatusCode != 500)
                {
                    _logger.LogInformation($" ---> request {httpContext.Request.Path} returns {httpContext.Response.StatusCode}");
                }
            }
            catch (Exception)
            {
                _logger.LogInformation($" ---> request {httpContext.Request.Path} returns {500}");

                throw;
            }
        }
    }
}
