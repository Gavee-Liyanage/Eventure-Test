package com.example.eventuretest.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object EventValidation {

    fun isEventDateValid(eventDate: Timestamp): Boolean {
        val currentDate = Calendar.getInstance()
        val eventCalendar = Calendar.getInstance().apply {
            time = eventDate.toDate()
        }
        return eventCalendar.after(currentDate)
    }

    fun isEventTimeValid(timeString: String): Boolean {
        return try {
            val timePattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$".toRegex()
            timePattern.matches(timeString)
        } catch (e: Exception) {
            false
        }
    }

    fun validateEventDateTime(date: Timestamp, time: String): String? {
        if (!isEventDateValid(date)) {
            return "Event date cannot be in the past"
        }

        if (!isEventTimeValid(time)) {
            return "Invalid time format. Use HH:MM format"
        }

        // Split and parse time
        val timeParts = time.split(":")
        if (timeParts.size != 2) return "Invalid time format"

        val hour = timeParts[0].toIntOrNull() ?: return "Invalid hour value"
        val minute = timeParts[1].toIntOrNull() ?: return "Invalid minute value"

        val eventCalendar = Calendar.getInstance().apply {
            timeInMillis = date.toDate().time // ✅ Fixed this line
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val oneHourFromNow = Calendar.getInstance().apply {
            add(Calendar.HOUR, 1)
        }

        if (eventCalendar.before(oneHourFromNow)) {
            return "Event must be scheduled at least 1 hour in advance"
        }

        return null
    }

    data class ValidationResult(val isValid: Boolean, val errorMessage: String = "")

    fun validateEventData(
        name: String,
        description: String,
        date: String,
        time: String,
        location: String,
        imageCount: Int
    ): ValidationResult {
        if (name.isBlank()) return ValidationResult(false, "Event name cannot be empty")
        if (description.isBlank()) return ValidationResult(false, "Description cannot be empty")
        if (date.isBlank()) return ValidationResult(false, "Please select a date")
        if (time.isBlank()) return ValidationResult(false, "Please select a time")
        if (location.isBlank()) return ValidationResult(false, "Location cannot be empty")
        if (imageCount == 0) return ValidationResult(false, "Please select at least one image")
        if (!isEventTimeValid(time)) return ValidationResult(false, "Invalid time format")

        return ValidationResult(true)
    }
}
