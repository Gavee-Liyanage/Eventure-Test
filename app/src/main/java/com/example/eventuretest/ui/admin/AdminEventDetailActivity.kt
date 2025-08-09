package com.example.eventuretest.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventuretest.R
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.databinding.ActivityAdminEventDetailBinding
import com.example.eventuretest.ui.adapters.EventImageAdapter
import com.example.eventuretest.utils.AdminConstants
import com.example.eventuretest.utils.DateUtils
import com.example.eventuretest.viewmodels.EventManagementViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class AdminEventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminEventDetailBinding
    private lateinit var viewModel: EventManagementViewModel
    private lateinit var imageAdapter: EventImageAdapter
    private var eventId: String? = null
    private var currentEvent: Event? = null

    companion object {
        private const val TAG = "AdminEventDetail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityAdminEventDetailBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize ViewModel with FirebaseFirestore instance
            val firestore = FirebaseFirestore.getInstance()
            viewModel = EventManagementViewModel(firestore)

            // Get event ID from intent with multiple fallback keys
            eventId = intent.getStringExtra("eventId")
                ?: intent.getStringExtra("EVENT_ID")
                        ?: intent.getStringExtra(AdminConstants.EXTRA_EVENT_ID)

            Log.d(TAG, "onCreate: eventId = $eventId")

            if (eventId.isNullOrEmpty()) {
                Log.e(TAG, "No event ID provided in intent")
                showSnackbar("Error: Event ID not found")
                finish()
                return
            }

            setupToolbar()
            setupImageRecyclerView()
            setupButtonListeners()
            observeViewModel()

            // Show loading initially
            binding.progressBar.visibility = View.VISIBLE

            // Load event data
            viewModel.loadEvent(eventId!!)

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            showSnackbar("Error loading event details")
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.event_details)
        }
    }

    private fun setupImageRecyclerView() {
        imageAdapter = EventImageAdapter(emptyList())
        binding.recyclerViewImages.apply {
            layoutManager = LinearLayoutManager(
                this@AdminEventDetailActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = imageAdapter
        }
    }

    private fun setupButtonListeners() {
        binding.btnEditEvent.setOnClickListener {
            currentEvent?.let { event ->
                try {
                    Log.d(TAG, "Edit button clicked for event: ${event.id}")
                    val intent = Intent(this, EditEventActivity::class.java).apply {
                        putExtra("eventId", event.id)
                        putExtra("EVENT_ID", event.id)
                        putExtra(AdminConstants.EXTRA_EVENT_ID, event.id)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening edit activity", e)
                    showSnackbar("Error opening edit screen: ${e.message}")
                }
            } ?: run {
                Log.w(TAG, "Edit clicked but currentEvent is null")
                showSnackbar("Event not loaded yet")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentEvent.observe(this) { event ->
            Log.d(TAG, "Received event: ${event?.name}")
            if (event != null) {
                currentEvent = event
                displayEventDetails(event)
                binding.progressBar.visibility = View.GONE
            } else {
                Log.e(TAG, "Received null event")
                showSnackbar("Event not found")
                // Don't finish immediately, wait a bit for user to see the message
                binding.root.postDelayed({ finish() }, 2000)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            Log.d(TAG, "Loading state: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            Log.e(TAG, "Error message: $error")
            if (!error.isNullOrBlank()) {
                binding.progressBar.visibility = View.GONE
                showSnackbar(error)
            }
        }

        viewModel.deleteResult.observe(this) { success ->
            if (success) {
                showSnackbar(getString(R.string.event_deleted_successfully))
                setResult(RESULT_OK) // Notify parent activity of changes
                finish()
            }
        }
    }

    private fun displayEventDetails(event: Event) {
        Log.d(TAG, "Displaying event details for: ${event.name}")

        try {
            binding.apply {
                // Basic info
                tvEventName.text = event.name
                tvEventDescription.text = event.description
                tvEventLocation.text = event.location
                tvEventDate.text = DateUtils.formatTimestamp(event.date)
                tvEventTime.text = event.time
                tvEventOrganizer.text = event.organizer ?: "Unknown"

                // Category
                val categoryDisplayName = when (event.category.uppercase()) {
                    "MUSICAL" -> "Musical"
                    "SPORTS" -> "Sports"
                    "FOOD" -> "Food"
                    "ART" -> "Art"
                    else -> event.category
                }
                chipCategory.text = categoryDisplayName
                chipCategory.setChipBackgroundColorResource(getCategoryColor(event.category))

                // Event status
                val isUpcoming = DateUtils.isEventUpcoming(event.date)
                tvEventStatus.text = if (isUpcoming) "UPCOMING" else "PAST"
                tvEventStatus.setBackgroundColor(
                    getColor(if (isUpcoming) R.color.status_upcoming else R.color.status_past)
                )

                // Images
                if (event.imageUrls.isNotEmpty()) {
                    imageAdapter.updateImages(event.imageUrls)
                    recyclerViewImages.visibility = View.VISIBLE
                    tvNoImages?.visibility = View.GONE
                } else {
                    recyclerViewImages.visibility = View.GONE
                    tvNoImages?.visibility = View.VISIBLE
                }

                // Contact info
                tvContactEmail?.text = event.contactEmail ?: "Not provided"
                tvContactPhone?.text = event.contactPhone ?: "Not provided"

                // Timestamps
                tvCreatedAt?.text = "Created: ${DateUtils.formatTimestamp(event.createdAt)}"
                tvUpdatedAt?.text = "Updated: ${DateUtils.formatTimestamp(event.updatedAt)}"

                // Statistics
                tvMaxAttendees?.text = event.maxAttendees?.toString() ?: "0"
                tvCurrentAttendees?.text = event.currentAttendees?.toString() ?: "0"
                tvTicketPrice?.text = "$${event.ticketPrice ?: 0}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying event details", e)
            showSnackbar("Error displaying event details")
        }
    }

    private fun getCategoryColor(categoryName: String): Int {
        return when (categoryName.uppercase()) {
            "MUSICAL" -> R.color.category_musical
            "SPORTS" -> R.color.category_sports
            "FOOD" -> R.color.category_food
            "ART" -> R.color.category_art
            else -> R.color.admin_primary
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_event_detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_edit -> {
                currentEvent?.let { event ->
                    try {
                        val intent = Intent(this, EditEventActivity::class.java).apply {
                            putExtra("eventId", event.id)
                            putExtra("EVENT_ID", event.id)
                            putExtra(AdminConstants.EXTRA_EVENT_ID, event.id)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error opening edit from menu", e)
                        showSnackbar("Error opening edit screen")
                    }
                }
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_event))
            .setMessage(getString(R.string.confirm_delete_event))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                currentEvent?.let { event -> viewModel.deleteEvent(event.id) }
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning from edit
        eventId?.let {
            Log.d(TAG, "onResume: Reloading event $it")
            viewModel.loadEvent(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity destroyed")
    }
}