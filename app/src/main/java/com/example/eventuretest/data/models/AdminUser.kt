package com.example.eventuretest.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class AdminUser(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "admin",
    val permissions: List<String> = listOf("create_event", "edit_event", "delete_event", "view_analytics"),
    val createdAt: Timestamp = Timestamp.now(),
    val lastLogin: Timestamp = Timestamp.now(),
    val isActive: Boolean = true,
    val profileImageUrl: String = "",
    val department: String = "",
    val phoneNumber: String = ""
) {
    constructor() : this("")

    fun hasPermission(permission: String): Boolean {
        return permissions.contains(permission)
    }
}