package com.example.eventuretest.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.data.repository.AdminEventRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class EventManagementViewModel(
    firestore: FirebaseFirestore
) : ViewModel() {

    private val repository = AdminEventRepository(firestore)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _currentEvent = MutableLiveData<Event?>()
    val currentEvent: LiveData<Event?> = _currentEvent

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> = _updateResult

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val result = repository.getEventById(eventId)
                if (result.isSuccess) {
                    val event = result.getOrNull()
                    if (event != null) {
                        _currentEvent.value = event
                    } else {
                        _errorMessage.value = "Event not found"
                    }
                } else {
                    _errorMessage.value = "Failed to load event: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load event: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEvent(eventId: String, event: Event) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val result = repository.updateEvent(eventId, event)
                _updateResult.value = result.isSuccess
                if (!result.isSuccess) {
                    _errorMessage.value = "Failed to update event: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
                _updateResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val result = repository.deleteEvent(eventId)
                _deleteResult.value = result.isSuccess
                if (!result.isSuccess) {
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
    // Implement duplicateEvent in repository first if needed, then:

    fun duplicateEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val result = repository.duplicateEvent(eventId)
                if (result.isSuccess) {
                    _errorMessage.value = "Event duplicated successfully"
                } else {
                    _errorMessage.value = "Failed to duplicate event: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to duplicate event: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}
