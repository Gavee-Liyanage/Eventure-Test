package com.example.eventuretest.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventuretest.databinding.FragmentAdminEventListBinding
import com.example.eventuretest.data.models.EventCategory
import com.example.eventuretest.ui.adapters.AdminEventAdapter
import com.example.eventuretest.utils.AdminConstants
import com.example.eventuretest.viewmodels.AdminEventListViewModel
import com.google.android.material.chip.Chip

class AdminEventListFragment : Fragment() {

    private var _binding: FragmentAdminEventListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminEventListViewModel
    private lateinit var eventAdapter: AdminEventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminEventListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupFilterChips()
        setupClickListeners()
        observeViewModel()

        viewModel.loadEvents()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AdminEventListViewModel::class.java]
    }

    private fun setupRecyclerView() {
        eventAdapter = AdminEventAdapter(
            onEventClick = { event ->
                val intent = Intent(requireContext(), AdminEventDetailActivity::class.java)
                intent.putExtra(AdminConstants.EXTRA_EVENT_ID, event.id)
                startActivity(intent)
            },
            onEditClick = { event ->
                val intent = Intent(requireContext(), EditEventActivity::class.java)
                intent.putExtra(EditEventActivity.EXTRA_EVENT_ID, event.id)
                startActivity(intent)
            },
            onDeleteClick = { event ->
                showDeleteConfirmationDialog(event.id, event.name)
            }
        )

        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun setupFilterChips() {
        // Add "All" chip
        val allChip = Chip(requireContext())
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
            val chip = Chip(requireContext())
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
            val intent = Intent(requireContext(), AddEventActivity::class.java)
            startActivity(intent)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshEvents()
        }
    }

    private fun observeViewModel() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
            updateEmptyState(events.isEmpty())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading && eventAdapter.itemCount == 0)
                View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show()
                viewModel.refreshEvents()
            } else {
                Toast.makeText(requireContext(), "Failed to delete event", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmptyEvents.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewEvents.visibility = if (!isEmpty) View.VISIBLE else View.GONE
    }

    private fun showDeleteConfirmationDialog(eventId: String, eventName: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete \"$eventName\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteEvent(eventId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun searchEvents(query: String) {
        viewModel.searchEvents(query)
    }

    fun sortEventsByDate() {
        viewModel.sortEventsByDate()
    }

    fun sortEventsByName() {
        viewModel.sortEventsByName()
    }

    fun sortEventsByCategory() {
        viewModel.sortEventsByCategory()
    }
}
