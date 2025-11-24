package com.mycat.purrfectstore2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.databinding.CartItemLayoutBinding
import com.mycat.purrfectstore2.model.CartProduct
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val onQuantityChanged: (cartProduct: CartProduct) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var items: MutableList<CartProduct> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun getItems(): List<CartProduct> = items

    fun updateItems(newItems: List<CartProduct>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class CartViewHolder(private val binding: CartItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

        fun bind(cartProduct: CartProduct) {
            binding.textViewCartItemQuantity.text = cartProduct.quantity.toString()

            val product = cartProduct.product_details
            if (product != null) {
                binding.textViewCartItemName.text = product.name
                val imageUrl = product.images.firstOrNull()?.url
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.fresa)
                    .error(R.drawable.fresa)
                    .into(binding.imageViewCartItem)
                
                updatePrice(cartProduct)

            } else {
                binding.textViewCartItemName.text = "Cargando..."
                binding.imageViewCartItem.setImageResource(R.drawable.fresa)
                binding.textViewCartItemUnitPrice.text = ""
                binding.textViewCartItemTotalPrice.text = ""
            }

            binding.buttonDecreaseQuantity.setOnClickListener {
                if (cartProduct.quantity > 0) { 
                    cartProduct.quantity--
                    updatePriceAndNotify(cartProduct)
                }
            }

            binding.buttonIncreaseQuantity.setOnClickListener {
                val stock = cartProduct.product_details?.stock ?: 0
                if (cartProduct.quantity < stock) {
                    cartProduct.quantity++
                    updatePriceAndNotify(cartProduct)
                } else {
                    Toast.makeText(itemView.context, "No hay más stock disponible", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun updatePriceAndNotify(cartProduct: CartProduct) {
            binding.textViewCartItemQuantity.text = cartProduct.quantity.toString()
            updatePrice(cartProduct)
            onQuantityChanged(cartProduct)
        }

        private fun updatePrice(cartProduct: CartProduct) {
            cartProduct.product_details?.let { product ->
                val unitPrice = product.price
                val totalPrice = unitPrice * cartProduct.quantity

                binding.textViewCartItemUnitPrice.text = "${currencyFormat.format(unitPrice)} /un"
                binding.textViewCartItemTotalPrice.text = "Total: ${currencyFormat.format(totalPrice)}"
            }
        }
    }
}
