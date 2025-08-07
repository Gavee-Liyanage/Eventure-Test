package com.example.eventuretest.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventuretest.R
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.databinding.ActivityAdminEventDetailBinding
import com.example.eventuretest.ui.adapters.EventImageAdapter
import com.example.eventuretest.utils.DateUtils
import com.example.eventuretest.viewmodels.EventManagementViewModel
import com.google.android.material.snackbar.Snackbar

class AdminEventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminEventDetailBinding
    private val viewModel: EventManagementViewModel by viewModels()
    private lateinit var imageAdapter: EventImageAdapter
    private var eventId: String? = null
    private var currentEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventId = intent.getStringExtra("EVENT_ID")

        setupToolbar()
        setupImageRecyclerView()
        observeViewModel()

        eventId?.let { viewModel.loadEvent(it) }
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
        imageAdapter = EventImageAdapter(
            emptyList() // Assuming adapter handles updates via method
        )
        binding.recyclerViewImages.apply {
            layoutManager = LinearLayoutManager(this@AdminEventDetailActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.currentEvent.observe(this, Observer { event ->
            event?.let {
                currentEvent = it
                displayEventDetails(it)
            }
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        })

        viewModel.errorMessage.observe(this, Observer { error ->
            if (!error.isNullOrBlank()) showSnackbar(error)
        })

        viewModel.deleteResult.observe(this, Observer { success ->
            if (success) {
                showSnackbar(getString(R.string.event_deleted_successfully))
                finish()
            }
        })
    }

    private fun displayEventDetails(event: Event) {
        binding.apply {
            tvEventName.text = event.name
            tvEventDescription.text = event.description
            tvEventLocation.text = event.location
            tvEventDate.text = DateUtils.formatTimestamp(event.date)
            tvEventTime.text = event.time
            chipCategory.text = event.category
            chipCategory.setChipBackgroundColorResource(getCategoryColor(event.category))

            if (event.imageUrls.isNotEmpty()) {
                imageAdapter.updateImages(event.imageUrls)
                recyclerViewImages.visibility = android.view.View.VISIBLE
                tvNoImages.visibility = android.view.View.GONE
            } else {
                recyclerViewImages.visibility = android.view.View.GONE
                tvNoImages.visibility = android.view.View.VISIBLE
            }

            tvContactEmail.text = event.contactEmail
            tvContactPhone.text = event.contactPhone
            tvCreatedAt.text = "Created: ${DateUtils.formatTimestamp(event.createdAt)}"
            tvUpdatedAt.text = "Updated: ${DateUtils.formatTimestamp(event.updatedAt)}"
            tvMaxAttendees.text = event.maxAttendees.toString()
            tvCurrentAttendees.text = event.currentAttendees.toString()
            tvTicketPrice.text = "$${event.ticketPrice}"
        }
    }

    private fun getCategoryColor(categoryName: String): Int {
        return when (categoryName.lowercase()) {
            "musical" -> R.color.category_musical
            "sports" -> R.color.category_sports
            "food" -> R.color.category_food
            "art" -> R.color.category_art
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
                    val intent = Intent(this, EditEventActivity::class.java)
                    intent.putExtra("EVENT_ID", event.id)
                    startActivity(intent)
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
        eventId?.let { viewModel.loadEvent(it) }
    }
}
