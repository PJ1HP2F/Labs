using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ASP.Domain.Entities
{
    public class CartItem
    {
        public Movie Movie { get; set; }
        public int Quantity { get; set; }
    }
}
