package com.example.eventuretest.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eventuretest.R
import com.example.eventuretest.databinding.ItemAdminEventListBinding
import com.example.eventuretest.ui.admin.AdminEventDetailActivity
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.ui.admin.EditEventActivity
import com.example.eventuretest.utils.AdminConstants
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

                // Set category indicator color and chip background
                val categoryColor = when (event.category.uppercase()) {
                    "MUSICAL" -> R.color.admin_theme
                    "SPORTS" -> R.color.admin_theme
                    "FOOD" -> R.color.category_food
                    "ART" -> R.color.admin_theme
                    else -> R.color.category_default
                }

                // Set category indicator color
                viewCategoryIndicator.setBackgroundColor(
                    binding.root.context.getColor(categoryColor)
                )

                // Set chip background color
                chipCategory.setChipBackgroundColorResource(categoryColor)

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

                // Main click listener - Fixed: Remove duplicate callback and ensure proper navigation
                root.setOnClickListener {
                    try {
                        val context = binding.root.context
                        val intent = Intent(context, AdminEventDetailActivity::class.java).apply {
                            putExtra(AdminConstants.EXTRA_EVENT_ID, event.id)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Show error message
                        Toast.makeText(
                            binding.root.context,
                            "Failed to open event details: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        // Fallback callback if needed
                        onEventClick(event)
                    }
                }

                // Edit button click - Fixed: Add debouncing
                buttonEdit.setOnClickListener { view ->
                    view.isEnabled = false
                    try {
                        val context = binding.root.context
                        val intent = Intent(context, EditEventActivity::class.java).apply {
                            putExtra(AdminConstants.EXTRA_EVENT_ID, event.id)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(binding.root.context, "Failed to open edit event: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    view.postDelayed({ view.isEnabled = true }, 1000)
                }


                // Delete button click - Fixed: Add debouncing
                buttonDelete.setOnClickListener { view ->
                    view.isEnabled = false
                    try {
                        onDeleteClick(event)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    view.postDelayed({ view.isEnabled = true }, 1000)
                }

                // Menu button click (if you want to add menu functionality)
                btnMenu.setOnClickListener {
                    // Add popup menu functionality here if needed
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