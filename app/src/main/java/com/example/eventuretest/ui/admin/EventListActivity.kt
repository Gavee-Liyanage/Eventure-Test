package com.example.eventuretest.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventuretest.ui.admin.AdminEventDetailActivity
import com.example.eventuretest.R
import com.example.eventuretest.databinding.ActivityEventListBinding
import com.example.eventuretest.data.models.EventCategory
import com.example.eventuretest.ui.adapters.AdminEventListAdapter
import com.example.eventuretest.utils.AdminConstants
import com.example.eventuretest.viewmodels.AdminEventListViewModel
import com.google.android.material.chip.Chip

class EventListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventListBinding
    private lateinit var viewModel: AdminEventListViewModel
    private lateinit var eventAdapter: AdminEventListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupFilterChips()
        setupClickListeners()
        observeViewModel()

        viewModel.loadEvents()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "All Events"
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AdminEventListViewModel::class.java]
    }

    private fun setupRecyclerView() {
        eventAdapter = AdminEventListAdapter(
            onEventClick = { event ->
                val intent = Intent(this, AdminEventDetailActivity::class.java)
                intent.putExtra(AdminConstants.EXTRA_EVENT_ID, event.id)
                startActivity(intent)
            },
            onEditClick = { event ->
                val intent = Intent(this, EditEventActivity::class.java)
                intent.putExtra(EditEventActivity.EXTRA_EVENT_ID, event.id)
                startActivity(intent)
            },
            onDeleteClick = { event ->
                showDeleteConfirmationDialog(event.id, event.name)
            }
        )

        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(this@EventListActivity)
            adapter = eventAdapter
        }
    }

    private fun setupFilterChips() {
        // Add "All" chip
        val allChip = Chip(this)
        allChip.text = "All"
        allChip.isCheckable = true
        allChip.isChecked = true
        allChip.tag = null
        allChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clearOtherChips(allChip)
                viewModel.filterEvents(null)
            }
        }
        binding.chipGroupFilters.addView(allChip)

        // Add category chips
        EventCategory.values().forEach { category ->
            val chip = Chip(this)
            chip.text = category.displayName
            chip.isCheckable = true
            chip.tag = category
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    clearOtherChips(chip)
                    viewModel.filterEvents(category)
                }
            }
            binding.chipGroupFilters.addView(chip)
        }
    }

    private fun clearOtherChips(selectedChip: Chip) {
        for (i in 0 until binding.chipGroupFilters.childCount) {
            val chip = binding.chipGroupFilters.getChildAt(i) as Chip
            if (chip != selectedChip) {
                chip.isChecked = false
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddEvent.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshEvents()
        }
    }

    private fun observeViewModel() {
        viewModel.events.observe(this) { events ->
            eventAdapter.submitList(events)
            updateEmptyState(events.isEmpty())
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading && eventAdapter.itemCount == 0)
                android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.deleteResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show()
                viewModel.refreshEvents()
            } else {
                Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmptyEvents.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE
        binding.recyclerViewEvents.visibility = if (!isEmpty) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showDeleteConfirmationDialog(eventId: String, eventName: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete \"$eventName\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteEvent(eventId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_event_list_menu, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchEvents(newText ?: "")
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_sort_by_date -> {
                viewModel.sortEventsByDate()
                true
            }
            R.id.action_sort_by_name -> {
                viewModel.sortEventsByName()
                true
            }
            R.id.action_sort_by_category -> {
                viewModel.sortEventsByCategory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshEvents()
    }
}