package com.example.eventuretest.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Event(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val date: Timestamp = Timestamp.now(),
    val time: String = "",
    val location: String = "",
    val imageUrls: List<String> = emptyList(),
    val participantCount: Int? = null,
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val status: String = "active", // active, inactive, cancelled
    val maxAttendees: Int = 0,
    val currentAttendees: Int = 0,
    val ticketPrice: Double = 0.0,
    val organizer: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val tags: List<String> = emptyList()
) : Serializable {
    // No-argument constructor for Firestore
    constructor() : this("")

    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "description" to description,
            "category" to category,
            "date" to date,
            "time" to time,
            "location" to location,
            "imageUrls" to imageUrls,
            "createdBy" to createdBy,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "status" to status,
            "maxAttendees" to maxAttendees,
            "currentAttendees" to currentAttendees,
            "ticketPrice" to ticketPrice,
            "organizer" to organizer,
            "contactEmail" to contactEmail,
            "contactPhone" to contactPhone,
            "tags" to tags
        )
    }
}