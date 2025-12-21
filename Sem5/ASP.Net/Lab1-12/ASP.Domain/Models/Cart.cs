using ASP.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ASP.Domain.Models
{
    public class Cart
    {
        /// <summary>
        /// Cart item list
        /// key - object id
        /// </summary>
        public Dictionary<int, CartItem> CartItems { get; set; } = [];

        /// <summary>
        /// Add item to cart
        /// </summary>
        /// <param name="item">Item to add</param>
        public virtual void AddToCart(Movie item)
        {
            if (CartItems.ContainsKey(item.Id))
            {
                ++CartItems[item.Id].Quantity;
            }
            else
            {
                CartItems.Add(item.Id, new CartItem() { Movie = item, Quantity = 1 });
            }
        }

        /// <summary>
        /// Delete items from cart
        /// </summary>
        /// <param name="id">Id of items to delete</param>
        public virtual void RemoveItems(int id)
        {
            if (CartItems.ContainsKey(id))
            {
                CartItems.Remove(id);
            }
        }

        /// <summary>
        /// Clear cart
        /// </summary>
        public virtual void ClearAll()
        {
            CartItems.Clear();
        }

        /// <summary>
        /// Total item count
        /// </summary>
        public int Count
        {
            get => CartItems.Sum(item => item.Value.Quantity);
        }

        /// <summary>
        /// Total cart price
        /// </summary>
        public decimal TotalPrice
        {
            get => CartItems.Sum(item => item.Value.Quantity * item.Value.Movie.Price);
        }
    }
}
