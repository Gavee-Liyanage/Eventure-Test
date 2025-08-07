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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventuretest.R
import com.example.eventuretest.databinding.ActivityEditEventBinding
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.data.models.EventCategory
import com.example.eventuretest.ui.adapters.ImagePreviewAdapter
import com.example.eventuretest.utils.DateUtils
import com.example.eventuretest.utils.EventValidation
import com.example.eventuretest.viewmodels.EventManagementViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class EditEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditEventBinding
    private lateinit var viewModel: EventManagementViewModel
    private lateinit var imageAdapter: ImagePreviewAdapter
    private val selectedImages = mutableListOf<Uri>()
    private var currentEvent: Event? = null
    private var eventId: String = ""

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                if (intent.clipData != null) {
                    for (i in 0 until intent.clipData!!.itemCount) {
                        val imageUri = intent.clipData!!.getItemAt(i).uri
                        selectedImages.add(imageUri)
                    }
                } else {
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
        binding = ActivityEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: ""
        if (eventId.isEmpty()) {
            finish()
            return
        }

        setupToolbar()
        setupViewModel()
        setupImageRecyclerView()
        setupEventCategoryDropdown()
        setupClickListeners()
        observeViewModel()
        loadEventData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Edit Event"
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[EventManagementViewModel::class.java]
    }

    private fun setupImageRecyclerView() {
        imageAdapter = ImagePreviewAdapter(selectedImages) { position ->
            selectedImages.removeAt(position)
            imageAdapter.notifyItemRemoved(position)
            updateImageCountDisplay()
        }

        binding.recyclerViewImages.apply {
            layoutManager = LinearLayoutManager(this@EditEventActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    private fun setupEventCategoryDropdown() {
        val categories = EventCategory.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvEventCategory.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.btnAddImage.setOnClickListener { openImagePicker() }
        binding.editTextEventDate.setOnClickListener { showDatePicker() }
        binding.editTextEventTime.setOnClickListener { showTimePicker() }
        binding.btnUpdateEvent.setOnClickListener { validateAndUpdateEvent() }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnUpdateEvent.isEnabled = !isLoading
        }

        viewModel.currentEvent.observe(this) { event ->
            currentEvent = event
            event?.let { populateEventData(it) }
        }

        viewModel.updateResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update event. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadEventData() {
        viewModel.loadEvent(eventId)
    }

    private fun populateEventData(event: Event) {
        binding.editTextEventName.setText(event.name)
        binding.editTextEventDescription.setText(event.description)
        binding.editTextEventDate.setText(DateUtils.formatDate(event.date.toDate()))
        binding.editTextEventTime.setText(event.time)
        binding.editTextEventLocation.setText(event.location)
        binding.actvEventCategory.setText(event.category)
        updateImageCountDisplay()
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

    private fun validateAndUpdateEvent() {
        val eventName = binding.editTextEventName.text.toString().trim()
        val eventDescription = binding.editTextEventDescription.text.toString().trim()
        val eventDateStr = binding.editTextEventDate.text.toString().trim()
        val eventTime = binding.editTextEventTime.text.toString().trim()
        val eventLocation = binding.editTextEventLocation.text.toString().trim()
        val selectedCategory = binding.actvEventCategory.text.toString()

        val validationResult = EventValidation.validateEventData(
            eventName, eventDescription, eventDateStr, eventTime, eventLocation,
            selectedImages.size + (currentEvent?.imageUrls?.size ?: 0)
        )

        if (!validationResult.isValid) {
            Toast.makeText(this, validationResult.errorMessage, Toast.LENGTH_LONG).show()
            return
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select an event category", Toast.LENGTH_SHORT).show()
            return
        }

        val parsedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(eventDateStr)
        val timestamp = parsedDate?.let { Timestamp(it) } ?: return

        currentEvent?.let { event ->
            val updatedEvent = event.copy(
                name = eventName,
                description = eventDescription,
                date = timestamp,
                time = eventTime,
                location = eventLocation,
                category = selectedCategory
            )
            viewModel.updateEvent(eventId, updatedEvent)
        }
    }

    private fun updateImageCountDisplay() {
        val totalImages = selectedImages.size + (currentEvent?.imageUrls?.size ?: 0)
        binding.textViewNoImages.text = "$totalImages image(s) selected"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_edit_event_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_delete_event -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteEvent(eventId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}