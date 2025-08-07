package com.example.eventuretest.utils

import android.util.Patterns
import com.example.eventuretest.data.models.Event

import java.util.regex.Pattern

object AdminValidation {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )

    fun validateEvent(event: Event): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate event name
        if (event.name.isBlank()) {
            errors.add("Event name is required")
        } else if (event.name.length < 3) {
            errors.add("Event name must be at least 3 characters")
        } else if (event.name.length > 100) {
            errors.add("Event name must not exceed 100 characters")
        }

        // Validate description
        if (event.description.isBlank()) {
            errors.add("Event description is required")
        } else if (event.description.length < 10) {
            errors.add("Event description must be at least 10 characters")
        } else if (event.description.length > 1000) {
            errors.add("Event description must not exceed 1000 characters")
        }

        // Validate category
        if (event.category.isBlank()) {
            errors.add("Event category is required")
        }

        // Validate location
        if (event.location.isBlank()) {
            errors.add("Event location is required")
        } else if (event.location.length < 3) {
            errors.add("Event location must be at least 3 characters")
        }

        // Validate time
        if (event.time.isBlank()) {
            errors.add("Event time is required")
        }

        // Validate organizer
        if (event.organizer.isBlank()) {
            errors.add("Organizer name is required")
        }

        // Validate contact email
        if (event.contactEmail.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(event.contactEmail).matches()) {
            errors.add("Invalid contact email format")
        }

        // Validate contact phone
        if (event.contactPhone.isNotBlank() && !isValidPhoneNumber(event.contactPhone)) {
            errors.add("Invalid contact phone number format")
        }

        // Validate max attendees
        if (event.maxAttendees < 0) {
            errors.add("Max attendees cannot be negative")
        }

        // Validate ticket price
        if (event.ticketPrice < 0) {
            errors.add("Ticket price cannot be negative")
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val phonePattern = Pattern.compile("^[+]?[0-9]{10,15}$")
        return phonePattern.matcher(phone.replace("\\s".toRegex(), "")).matches()
    }

    fun validateImageUrls(imageUrls: List<String>): ValidationResult {
        val errors = mutableListOf<String>()

        if (imageUrls.isEmpty()) {
            errors.add("At least one event image is required")
        } else if (imageUrls.size > 5) {
            errors.add("Maximum 5 images allowed per event")
        }

        imageUrls.forEach { url ->
            if (!Patterns.WEB_URL.matcher(url).matches()) {
                errors.add("Invalid image URL format: $url")
            }
        }

        return ValidationResult(errors.isEmpty(), errors)
    }
}