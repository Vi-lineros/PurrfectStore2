package com.mycat.purrfectstore2.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.databinding.ItemProductBinding
import com.mycat.purrfectstore2.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val onProductClicked: (Product) -> Unit,
    private val onProductLongClicked: (Product) -> Unit,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var products: List<Product> = emptyList()
    val selectedItems = mutableSetOf<Int>()
    var isSelectionMode = false

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

    fun toggleSelection(productId: Int) {
        if (selectedItems.contains(productId)) {
            selectedItems.remove(productId)
        } else {
            selectedItems.add(productId)
        }
        onSelectionChanged(selectedItems.size)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
            binding.textViewProductName.text = product.name
            binding.textViewProductPrice.text = currencyFormat.format(product.price)

            if (product.stock > 0) {
                binding.textViewProductStock.text = "Stock: ${product.stock}"
                binding.root.alpha = 1.0f
            } else {
                binding.textViewProductStock.text = "Sin stock"
                binding.root.alpha = 0.6f // Fade out the item
            }

            if (product.images.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(product.images[0].url)
                    .placeholder(android.R.drawable.ic_popup_sync)
                    .error(android.R.drawable.ic_menu_close_clear_cancel)
                    .into(binding.imageViewProduct)
            } else {
                binding.imageViewProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            binding.selectionOverlay.visibility = if (selectedItems.contains(product.id)) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                onProductClicked(product) // Let the fragment decide what to do
            }

            binding.root.setOnLongClickListener {
                onProductLongClicked(product)
                true
            }
        }
    }
}
