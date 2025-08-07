package com.example.eventuretest.ui.admin

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventuretest.R
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.data.models.EventCategory
import com.example.eventuretest.databinding.ActivityAddEventBinding
import com.example.eventuretest.ui.adapters.ImagePreviewAdapter
import com.example.eventuretest.utils.DateUtils
import com.example.eventuretest.viewmodels.AddEventViewModel
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private val viewModel: AddEventViewModel by viewModels()
    private lateinit var imageAdapter: ImagePreviewAdapter
    private val selectedImageUris = mutableListOf<Uri>()
    private var selectedCategory: String? = null
    private var selectedDate: Calendar = Calendar.getInstance()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    selectedImageUris.add(clipData.getItemAt(i).uri)
                }
            } ?: result.data?.data?.let { uri ->
                selectedImageUris.add(uri)
            }
            imageAdapter.notifyDataSetChanged()
            updateImagePreviewVisibility()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupCategoryChips()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Add New Event"
        }
    }

    private fun setupRecyclerView() {
        imageAdapter = ImagePreviewAdapter(selectedImageUris) { position ->
            selectedImageUris.removeAt(position)
            imageAdapter.notifyItemRemoved(position)
            updateImagePreviewVisibility()
        }
        binding.recyclerViewImages.apply {
            layoutManager = LinearLayoutManager(this@AddEventActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
        updateImagePreviewVisibility()
    }

    private fun updateImagePreviewVisibility() {
        val imageCount = selectedImageUris.size
        binding.recyclerViewImages.visibility = if (imageCount > 0) View.VISIBLE else View.GONE
        binding.textViewImageCount.text = "$imageCount images selected"
    }

    private fun setupCategoryChips() {
        EventCategory.values().forEach { category ->
            val chip = Chip(this).apply {
                text = category.displayName
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedCategory = category.name
                        clearOtherChips(this)
                    }
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun clearOtherChips(selectedChip: Chip) {
        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as Chip
            if (chip != selectedChip) {
                chip.isChecked = false
            }
        }
    }

    private fun setupClickListeners() {
        binding.editTextEventDate.setOnClickListener { showDatePicker() }
        binding.editTextEventTime.setOnClickListener { showTimePicker() }
        binding.buttonSelectImages.setOnClickListener { openImagePicker() }
        binding.buttonSaveEvent.setOnClickListener { submitEvent() }
        binding.buttonCancel.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.saveResult.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, "Event added successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
        })

        viewModel.errorMessage.observe(this, Observer { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.editTextEventDate.setText(DateUtils.formatDate(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                binding.editTextEventTime.setText(DateUtils.formatTime(selectedDate.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun submitEvent() {
        val eventName = binding.editTextEventName.text.toString().trim()
        val eventDescription = binding.editTextEventDescription.text.toString().trim()
        val eventLocation = binding.editTextEventLocation.text.toString().trim()
        val eventDateStr = binding.editTextEventDate.text.toString()
        val eventTimeStr = binding.editTextEventTime.text.toString()

        if (eventName.isEmpty() || eventDescription.isEmpty() || eventLocation.isEmpty() || eventDateStr.isEmpty() || eventTimeStr.isEmpty() || selectedCategory == null) {
            Toast.makeText(this, "Please fill all fields and select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val event = Event(
            name = eventName,
            description = eventDescription,
            date = Timestamp(selectedDate.time),
            time = eventTimeStr,
            location = eventLocation,
            category = selectedCategory!!,
            // The following fields are not in the layout, using default values
            maxAttendees = 100,
            ticketPrice = 0.0,
            contactEmail = "default@email.com",
            contactPhone = "0000000000"
        )

        viewModel.saveEvent(event, selectedImageUris)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_add_event_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_clear_fields -> {
                clearAllFields()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearAllFields() {
        binding.apply {
            editTextEventName.text?.clear()
            editTextEventDescription.text?.clear()
            editTextEventLocation.text?.clear()
            editTextEventDate.text?.clear()
            editTextEventTime.text?.clear()
            chipGroupCategories.clearCheck()
            selectedCategory = null
            selectedImageUris.clear()
            imageAdapter.notifyDataSetChanged()
            updateImagePreviewVisibility()
        }
        Toast.makeText(this, "Fields cleared", Toast.LENGTH_SHORT).show()
    }
}