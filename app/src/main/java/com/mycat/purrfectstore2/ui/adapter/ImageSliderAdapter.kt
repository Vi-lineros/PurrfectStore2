package com.mycat.purrfectstore2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.databinding.ItemImageSliderBinding
import com.mycat.purrfectstore2.model.ProductImage

class ImageSliderAdapter(private val images: List<ProductImage>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {
    inner class ImageViewHolder(val binding: ItemImageSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(image: ProductImage) {
            Glide.with(itemView.context)
                .load(image.url)
                .into(binding.sliderImageView)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageSliderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }
    override fun getItemCount(): Int = images.size
}
