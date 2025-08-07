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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventuretest.R
import com.example.eventuretest.databinding.ActivityAddEventBinding
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.data.models.EventCategory
import com.example.eventuretest.ui.adapters.ImagePreviewAdapter
import com.example.eventuretest.viewmodels.AddEventViewModel
import com.example.eventuretest.utils.EventValidation
import com.example.eventuretest.utils.DateUtils
import com.google.android.material.chip.Chip
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding
    private lateinit var viewModel: AddEventViewModel
    private lateinit var imageAdapter: ImagePreviewAdapter
    private val selectedImages = mutableListOf<Uri>()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { intent ->
                if (intent.clipData != null) {
                    // Multiple images selected
                    for (i in 0 until intent.clipData!!.itemCount) {
                        val imageUri = intent.clipData!!.getItemAt(i).uri
                        selectedImages.add(imageUri)
                    }
                } else {
                    // Single image selected
                    intent.data?.let { uri ->
                        selectedImages.add(uri)
                    }
                }
                imageAdapter.updateImages(selectedImages)
                updateImageCountDisplay()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupImageRecyclerView()
        setupEventCategoryChips()
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

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AddEventViewModel::class.java]
    }

    private fun setupImageRecyclerView() {
        imageAdapter = ImagePreviewAdapter(selectedImages) { position ->
            selectedImages.removeAt(position)
            imageAdapter.notifyItemRemoved(position)
            updateImageCountDisplay()
        }

        binding.recyclerViewImages.apply {
            layoutManager = LinearLayoutManager(this@AddEventActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    private fun setupEventCategoryChips() {
        EventCategory.values().forEach { category ->
            val chip = Chip(this)
            chip.text = category.displayName
            chip.isCheckable = true
            chip.tag = category
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun setupClickListeners() {
        binding.buttonSelectImages.setOnClickListener { openImagePicker() }
        binding.editTextEventDate.setOnClickListener { showDatePicker() }
        binding.editTextEventTime.setOnClickListener { showTimePicker() }
        binding.buttonSaveEvent.setOnClickListener { validateAndSaveEvent() }
        binding.buttonCancel.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonSaveEvent.isEnabled = !isLoading
        }

        viewModel.saveResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to add event. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        imagePickerLauncher.launch(intent)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                binding.editTextEventDate.setText(DateUtils.formatDate(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                binding.editTextEventTime.setText(DateUtils.formatTime(selectedTime.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    private fun validateAndSaveEvent() {
        val eventName = binding.editTextEventName.text.toString().trim()
        val eventDescription = binding.editTextEventDescription.text.toString().trim()
        val eventDate = binding.editTextEventDate.text.toString().trim()
        val eventTime = binding.editTextEventTime.text.toString().trim()
        val eventLocation = binding.editTextEventLocation.text.toString().trim()
        val selectedCategory = getSelectedCategory()

        val validationResult = EventValidation.validateEventData(
            name = eventName,
            description = eventDescription,
            date = eventDate,
            time = eventTime,
            location = eventLocation,
            imageCount = selectedImages.size
        )

        if (!validationResult.isValid) {
            Toast.makeText(this, validationResult.errorMessage, Toast.LENGTH_LONG).show()
            return
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Please select an event category", Toast.LENGTH_SHORT).show()
            return
        }

        val parsedDate = DateUtils.parseDate(eventDate)
        val eventTimestamp = parsedDate?.let { com.google.firebase.Timestamp(it) } ?: com.google.firebase.Timestamp.now()

        val event = Event(
            name = eventName,
            description = eventDescription,
            date = eventTimestamp,
            time = eventTime,
            location = eventLocation,
            category = selectedCategory.name,
            imageUrls = emptyList()
        )

        viewModel.saveEvent(event, selectedImages)
    }


    private fun getSelectedCategory(): EventCategory? {
        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as Chip
            if (chip.isChecked) {
                return chip.tag as EventCategory
            }
        }
        return null
    }

    private fun updateImageCountDisplay() {
        binding.textViewImageCount.text = "${selectedImages.size} images selected"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_add_event_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                finish()
                true
            }
            R.id.action_save_draft -> {
                // Implement save as draft functionality
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}