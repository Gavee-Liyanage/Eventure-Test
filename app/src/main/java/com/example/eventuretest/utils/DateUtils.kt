package com.example.eventuretest.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun formatTime(date: Date): String {
        return timeFormat.format(date)
    }

    fun formatDateTime(date: Date): String {
        return dateTimeFormat.format(date)
    }

    fun parseDate(dateString: String): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun formatTimestamp(timestamp: Timestamp): String {
        return dateTimeFormat.format(timestamp.toDate())
    }


    fun parseDateToTimestamp(dateString: String): Timestamp? {
        return try {
            val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = format.parse(dateString)
            date?.let { Timestamp(it) }
        } catch (e: Exception) {
            null
        }
    }


    fun isToday(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.time

        calendar.time = date
        val eventDay = calendar.get(Calendar.DAY_OF_YEAR)
        val eventYear = calendar.get(Calendar.YEAR)

        calendar.time = today
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)

        return eventDay == todayDay && eventYear == todayYear
    }

    fun isFuture(date: Date): Boolean {
        return date.after(Date())
    }

    fun isEventUpcoming(date: String, time: String): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val eventDateTime = format.parse("$date $time")
            val now = Date()
            eventDateTime?.after(now) ?: false
        } catch (e: Exception) {
            false
        }
    }

    // <- Add this here
    fun isEventUpcoming(timestamp: Timestamp): Boolean {
        return timestamp.toDate().after(Date())
    }

}