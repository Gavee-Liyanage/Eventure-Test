package com.example.eventuretest.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.data.repository.AdminEventRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val repository = AdminEventRepository(firestore)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun saveEvent(event: Event, imageUris: List<Uri>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = ""

                val success = repository.addEvent(event, imageUris)
                _saveResult.value = success

                if (!success) {
                    _errorMessage.value = "Failed to save event. Please check your internet connection and try again."
                }

            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.message}"
                _saveResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveDraft(event: Event, imageUris: List<Uri>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val success = repository.saveDraftEvent(event, imageUris)
                if (success) {
                    _errorMessage.value = "Draft saved successfully"
                } else {
                    _errorMessage.value = "Failed to save draft"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save draft: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}