package com.example.eventuretest.data.repository

import android.net.Uri
import com.example.eventuretest.data.models.Event
import com.example.eventuretest.utils.AdminConstants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminEventRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val eventsCollection = firestore.collection(AdminConstants.EVENTS_COLLECTION)

    suspend fun createEvent(event: Event): Result<String> {
        return try {
            val documentRef = eventsCollection.add(event.toMap()).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvent(eventId: String, event: Event): Result<Unit> {
        return try {
            eventsCollection.document(eventId).set(event.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun duplicateEvent(eventId: String): Result<Unit> {
        return try {
            val eventResult = getEventById(eventId)
            if (eventResult.isSuccess) {
                val event = eventResult.getOrNull()
                if (event != null) {
                    // Create a new event with no id (Firestore will generate new id)
                    val newEvent = event.copy(id = "")
                    createEvent(newEvent)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Original event not found"))
                }
            } else {
                Result.failure(eventResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getEventById(eventId: String): Result<Event?> {
        return try {
            val document = eventsCollection.document(eventId).get().await()
            val event = document.toObject(Event::class.java)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllEvents(): Result<List<Event>> {
        return try {
            val querySnapshot = eventsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val events = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Event::class.java)?.copy(id = document.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventsByCategory(category: String): Result<List<Event>> {
        return try {
            val querySnapshot = eventsCollection
                .whereEqualTo("category", category)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()

            val events = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Event::class.java)?.copy(id = document.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventsByStatus(status: String): Result<List<Event>> {
        return try {
            val querySnapshot = eventsCollection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val events = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Event::class.java)?.copy(id = document.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchEvents(query: String): Result<List<Event>> {
        return try {
            val querySnapshot = eventsCollection
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            val events = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Event::class.java)?.copy(id = document.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addEvent(event: Event, imageUris: List<Uri>): Boolean {
        return try {
            // You can extend this to upload images and get URLs
            // For now, just save event data
            createEvent(event)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveDraftEvent(event: Event, imageUris: List<Uri>): Boolean {
        return try {
            // Mark event as draft, e.g., set status = "draft"
            val draftEvent = event.copy(status = "draft")
            createEvent(draftEvent)
            true
        } catch (e: Exception) {
            false
        }
    }

}