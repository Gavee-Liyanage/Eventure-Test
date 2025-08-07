package com.example.eventuretest.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventuretest.data.models.AdminUser
import com.example.eventuretest.data.remote.AdminFirestoreOperations
import com.example.eventuretest.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminMainViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val firestoreOperations: AdminFirestoreOperations
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<Map<String, Any>>(emptyMap())
    val dashboardState: StateFlow<Map<String, Any>> = _dashboardState.asStateFlow()

    private val _adminUser = MutableStateFlow<AdminUser?>(null)
    val adminUser: StateFlow<AdminUser?> = _adminUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadCurrentAdmin()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            firestoreOperations.getEventAnalytics()
                .onSuccess { analytics ->
                    _dashboardState.value = analytics
                }
                .onFailure { exception ->
                    _errorMessage.value = "Failed to load dashboard data: ${exception.message}"
                }

            _isLoading.value = false
        }
    }

    private fun loadCurrentAdmin() {
        viewModelScope.launch {
            adminRepository.getCurrentAdmin()
                .onSuccess { admin ->
                    _adminUser.value = admin
                }
                .onFailure { exception ->
                    _errorMessage.value = "Failed to load admin profile: ${exception.message}"
                }
        }
    }
}