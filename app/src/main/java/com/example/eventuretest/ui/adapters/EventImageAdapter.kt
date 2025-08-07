package com.example.eventuretest.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eventuretest.R


class EventImageAdapter(
    private var imageUrls: List<String>
) : RecyclerView.Adapter<EventImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewEventImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageUrls.size

    fun updateImages(newImageUrls: List<String>) {
        imageUrls = newImageUrls
        notifyDataSetChanged()
    }
}