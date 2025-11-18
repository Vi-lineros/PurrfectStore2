package com.mycat.purrfectstore2.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.databinding.ItemImagePreviewBinding
import com.mycat.purrfectstore2.model.ProductImage

class ImagePreviewAdapter(
    private val items: MutableList<Any>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ImagePreviewAdapter.ImagePreviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePreviewViewHolder {
        val binding = ItemImagePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImagePreviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagePreviewViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ImagePreviewViewHolder(private val binding: ItemImagePreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Any) {
            val imageUrl = when (item) {
                is Uri -> item
                is ProductImage -> item.url
                else -> null
            }

            Glide.with(itemView.context)
                .load(imageUrl)
                .into(binding.imageViewPreview)

            binding.buttonDeleteImage.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDelete(position)
                }
            }
        }
    }
}