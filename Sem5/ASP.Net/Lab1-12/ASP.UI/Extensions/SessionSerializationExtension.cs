using System.Text.Json;

namespace ASP.UI.Extensions
{
    public static class SessionDataSerializationExtensions
    {
        public static void Set<T>(this ISession session, string key, T data)
        {
            session.SetString(key, JsonSerializer.Serialize(data));
        }

        public static T? Get<T>(this ISession session, string key)
        {
            var data = session.GetString(key);
            return data == null ? default : JsonSerializer.Deserialize<T>(data);
        }
    }
}
