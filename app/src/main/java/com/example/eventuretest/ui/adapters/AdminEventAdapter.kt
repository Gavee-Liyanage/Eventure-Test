package com.example.eventuretest.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eventuretest.R
import com.example.eventuretest.databinding.ItemAdminEventCardBinding
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.utils.DateUtils

class AdminEventAdapter(
    private val onEventClick: (Event) -> Unit,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
) : ListAdapter<Event, AdminEventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemAdminEventCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(
        private val binding: ItemAdminEventCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                EventName.text = event.name
                EventDescription.text = event.description
                EventLocation.text = event.location
                chipCategory.text = event.category

                val formattedDateTime = "${DateUtils.formatDate(event.date.toDate())} • ${event.time}"
                EventDateTime.text = formattedDateTime

                // Category string (uppercase assumed) to display name & color
                val categoryDisplayName = when (event.category.uppercase()) {
                    "MUSICAL" -> "Musical"
                    "SPORTS" -> "Sports"
                    "FOOD" -> "Food"
                    "ART" -> "Art"
                    else -> "Other"
                }
                chipCategory.text = categoryDisplayName

                // Set category color
                val categoryColor = when (event.category) {
                    "MUSICAL" -> R.color.category_musical
                    "SPORTS" -> R.color.category_sports
                    "FOOD" -> R.color.category_food
                    "ART" -> R.color.category_art
                    else -> R.color.category_default
                }
                chipCategory.setChipBackgroundColorResource(categoryColor)
                chipCategory.text = event.category

                // Load first image if available
                if (event.imageUrls.isNotEmpty()) {
                    Glide.with(binding.root.context)
                        .load(event.imageUrls.first())
                        .placeholder(R.drawable.placeholder_event)
                        .error(R.drawable.placeholder_event)
                        .into(EventImage)
                } else {
                    EventImage.setImageResource(R.drawable.placeholder_event)
                }

                // Set event status based on date
                val isUpcoming = DateUtils.isEventUpcoming(event.date)
                EventStatus.text = if (isUpcoming) "Upcoming" else "Past"
                EventStatus.setTextColor(
                    binding.root.context.getColor(
                        if (isUpcoming) R.color.status_upcoming else R.color.status_past
                    )
                )

                // Click listeners
                root.setOnClickListener { onEventClick(event) }
                btnEdit.setOnClickListener { onEditClick(event) }
                btnEdit.setOnClickListener { onDeleteClick(event) }
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}