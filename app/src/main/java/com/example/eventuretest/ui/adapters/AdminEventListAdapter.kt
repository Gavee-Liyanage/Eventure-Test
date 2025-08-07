package com.example.eventuretest.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eventuretest.R
import com.example.eventuretest.databinding.ItemAdminEventListBinding
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.utils.DateUtils

class AdminEventListAdapter(
    private val onEventClick: (Event) -> Unit,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
) : ListAdapter<Event, AdminEventListAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemAdminEventListBinding.inflate(
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
        private val binding: ItemAdminEventListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                EventName.text = event.name
                EventDate.text = "${event.date} at ${event.time}"
                EventLocation.text = event.location
                chipCategory.text = event.category

                // Load thumbnail image
                if (event.imageUrls.isNotEmpty()) {
                    Glide.with(binding.root.context)
                        .load(event.imageUrls.first())
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(EventImage)
                } else {
                    EventImage.setImageResource(android.R.drawable.ic_menu_report_image)
                }

                // Category string (uppercase assumed) to display name & color
                val categoryDisplayName = when (event.category.uppercase()) {
                    "MUSICAL" -> "Musical"
                    "SPORTS" -> "Sports"
                    "FOOD" -> "Food"
                    "ART" -> "Art"
                    else -> "Other"
                }
                chipCategory.text = categoryDisplayName

                // Set category indicator color
                val categoryColor = when (event.category) {
                    "MUSICAL" -> R.color.category_musical
                    "SPORTS" -> R.color.category_sports
                    "FOOD" -> R.color.category_food
                    "ART" -> R.color.category_art
                    else -> R.color.category_default
                }
                viewCategoryIndicator.setBackgroundColor(
                    binding.root.context.getColor(categoryColor)
                )

                // Set event status
                val isUpcoming = DateUtils.isEventUpcoming(event.date)
                textViewEventStatus.text = if (isUpcoming) "Upcoming" else "Past"
                textViewEventStatus.setTextColor(
                    binding.root.context.getColor(
                        if (isUpcoming) R.color.status_upcoming else R.color.status_past
                    )
                )

                // Show participant count if available
                textViewParticipantCount.text = "${event.participantCount ?: 0} participants"

                // Click listeners
                root.setOnClickListener { onEventClick(event) }
                buttonEdit.setOnClickListener {
                    it.isEnabled = false
                    onEditClick(event)
                    it.postDelayed({ it.isEnabled = true }, 1000)
                }
                buttonDelete.setOnClickListener {
                    it.isEnabled = false
                    onDeleteClick(event)
                    it.postDelayed({ it.isEnabled = true }, 1000)
                }
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
