package com.mycat.purrfectstore2.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.databinding.ItemProductBinding
import com.mycat.purrfectstore2.model.Product

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

            binding.selectionOverlay.visibility = if (selectedItems.contains(product.id)) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                // The fragment will now handle this logic based on isSelectionMode
                onProductClicked(product)
            }

            binding.root.setOnLongClickListener {
                // Fragment handles this
                onProductLongClicked(product)
                true
            }
        }
    }
}
