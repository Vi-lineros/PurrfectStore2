package com.mycat.purrfectstore2.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.databinding.ItemImagePreviewBinding

class ImagePreviewAdapter(private val imageUris: List<Uri>) : RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>() {
    inner class ImageViewHolder(val binding: ItemImagePreviewBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImagePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = imageUris[position]
        Glide.with(holder.itemView.context)
            .load(uri)
            .centerCrop()
            .into(holder.binding.imageViewPreview)
    }
    override fun getItemCount(): Int {
        return imageUris.size
    }
}
