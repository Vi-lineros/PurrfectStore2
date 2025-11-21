package com.mycat.purrfectstore2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.databinding.OrderDetailsProductItemBinding
import com.mycat.purrfectstore2.model.CartProduct
import com.mycat.purrfectstore2.ui.fragments.UserOrderDetailsFragmentDirections
import java.text.NumberFormat
import java.util.Locale

class OrderDetailProductAdapter(private var products: List<CartProduct>) : RecyclerView.Adapter<OrderDetailProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = OrderDetailsProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<CartProduct>) {
        this.products = newProducts
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(private val binding: OrderDetailsProductItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = products[position]
                    val action = UserOrderDetailsFragmentDirections.actionUserOrderDetailsFragmentToProductDetailsOrderFragment(
                        productId = product.product_id,
                        quantity = product.quantity
                    )
                    itemView.findNavController().navigate(action)
                }
            }
        }

        fun bind(cartProduct: CartProduct) {
            val product = cartProduct.product_details
            binding.textViewProductName.text = product?.name ?: "Producto no disponible"
            binding.textViewProductQuantity.text = "x${cartProduct.quantity}"

            val unitPrice = product?.price ?: 0.0
            binding.textViewProductUnitPrice.text = currencyFormat.format(unitPrice)

            val lineTotal = unitPrice * cartProduct.quantity
            binding.textViewProductLineTotal.text = String.format(Locale.US, "Total: %.2f", lineTotal)

            if (product?.images?.isNotEmpty() == true) {
                Glide.with(itemView.context)
                    .load(product.images.first().url)
                    .placeholder(R.drawable.fresa)
                    .error(R.drawable.fresa)
                    .into(binding.imageViewProduct)
            } else {
                binding.imageViewProduct.setImageResource(R.drawable.fresa)
            }
        }
    }
}
