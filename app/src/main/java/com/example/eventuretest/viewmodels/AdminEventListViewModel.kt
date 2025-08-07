package com.example.eventuretest.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.data.models.EventCategory
import com.example.eventuretest.data.repository.AdminEventRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AdminEventListViewModel(
    private val repository: AdminEventRepository
) : ViewModel() {

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    private var allEvents = listOf<Event>()
    private var filteredEvents = listOf<Event>()
    private var currentFilter: EventCategory? = null
    private var currentSearchQuery = ""

    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val result = repository.getAllEvents()
                if (result.isSuccess) {
                    val eventList = result.getOrNull() ?: emptyList()
                    allEvents = eventList
                    filteredEvents = eventList
                    _events.value = eventList
                } else {
                    _errorMessage.value = "Failed to load events: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load events: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshEvents() {
        loadEvents()
    }

    fun filterEvents(category: EventCategory?) {
        currentFilter = category
        applyFiltersAndSearch()
    }

    fun searchEvents(query: String) {
        currentSearchQuery = query
        applyFiltersAndSearch()
    }

    private fun applyFiltersAndSearch() {
        var result = allEvents

        // Apply category filter - comparing EventCategory with event.category String name
        currentFilter?.let { category ->
            result = result.filter { event ->
                event.category == category.name
            }
        }

        // Apply search query
        if (currentSearchQuery.isNotEmpty()) {
            result = result.filter { event ->
                event.name.contains(currentSearchQuery, ignoreCase = true) ||
                        event.description.contains(currentSearchQuery, ignoreCase = true) ||
                        event.location.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        filteredEvents = result
        _events.value = result
    }

    fun sortEventsByDate() {
        val sorted = filteredEvents.sortedBy { event ->
            try {
                event.date.toDate()
            } catch (e: Exception) {
                null
            }
        }
        _events.value = sorted
    }


    fun sortEventsByName() {
        val sorted = filteredEvents.sortedBy { it.name.lowercase(Locale.getDefault()) }
        _events.value = sorted
    }

    fun sortEventsByCategory() {
        // If your EventCategory is enum with displayName, use it; else just use name
        val sorted = filteredEvents.sortedBy { it.category }
        _events.value = sorted
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val result = repository.deleteEvent(eventId)
                if (result.isSuccess) {
                    _deleteResult.value = true
                    allEvents = allEvents.filter { it.id != eventId }
                    filteredEvents = filteredEvents.filter { it.id != eventId }
                    _events.value = filteredEvents
                } else {
                    _deleteResult.value = false
                    _errorMessage.value = "Failed to delete event: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
                _deleteResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getEventStatistics(): Map<String, Int> {
        val now = System.currentTimeMillis()

        val upcomingCount = allEvents.count { event ->
            try {
                val eventDate = event.date.toDate().time
                eventDate >= now
            } catch (e: Exception) {
                false
            }
        }
        val pastCount = allEvents.size - upcomingCount

        return mapOf(
            "total" to allEvents.size,
            "upcoming" to upcomingCount,
            "past" to pastCount,
            "musical" to allEvents.count { it.category == EventCategory.MUSICAL.name },
            "sports" to allEvents.count { it.category == EventCategory.SPORTS.name },
            "food" to allEvents.count { it.category == EventCategory.FOOD.name },
            "art" to allEvents.count { it.category == EventCategory.ART.name }
        )
    }
}
