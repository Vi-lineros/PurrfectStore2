package com.mycat.purrfectstore2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.databinding.OrderDetailsProductItemBinding
import com.mycat.purrfectstore2.model.CartProduct
import java.text.NumberFormat
import java.util.Locale

class OrderDetailsAdapter(private var products: List<CartProduct>) : RecyclerView.Adapter<OrderDetailsAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = OrderDetailsProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<CartProduct>) {
        products = newProducts
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(private val binding: OrderDetailsProductItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

        fun bind(productItem: CartProduct) {
            binding.textViewProductQuantity.text = "x${productItem.quantity}"
            
            val productDetails = productItem.product_details
            if (productDetails != null) {
                binding.textViewProductName.text = productDetails.name

                val unitPrice = productDetails.price
                val lineTotal = unitPrice * productItem.quantity

                binding.textViewProductUnitPrice.text = "${currencyFormat.format(unitPrice)} /un"
                binding.textViewProductLineTotal.text = "Total: ${currencyFormat.format(lineTotal)}"

                val imageUrl = productDetails.images.firstOrNull()?.url
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.fresa)
                    .error(R.drawable.fresa)
                    .into(binding.imageViewProduct)
            } else {
                binding.textViewProductName.text = "Cargando..."
            }
        }
    }
}
