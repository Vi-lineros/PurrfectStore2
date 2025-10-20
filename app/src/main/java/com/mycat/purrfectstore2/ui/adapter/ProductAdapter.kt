package com.mycat.purrfectstore2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.databinding.ItemProductBinding
import com.mycat.purrfectstore2.model.Product
import com.mycat.purrfectstore2.ui.fragments.ProductFragmentDirections
import android.util.Log
class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    private var products: List<Product> = emptyList()
    inner class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedProduct = products[position]
                    Log.d("ProductAdapter", "Haciendo clic en producto ID: ${clickedProduct.id}, Nombre: ${clickedProduct.name}")
                    val action = ProductFragmentDirections.actionNavProductToNavProductDetails2(clickedProduct.id)
                    itemView.findNavController().navigate(action)
                }
            }
        }
        fun bind(product: Product) {
            binding.textViewProductName.text = product.name
            binding.textViewProductPrice.text = "$ ${product.price}"
            binding.textViewProductStock.text = "Stock: ${product.stock}"
            if (product.images.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(product.images[0].url)
                    .placeholder(android.R.drawable.ic_popup_sync)
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .into(binding.imageViewProduct)
            } else {
                binding.imageViewProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }
    override fun getItemCount() = products.size
    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
