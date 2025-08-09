package com.example.eventuretest.data.remote

import com.example.eventuretest.utils.AdminConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminFirestoreOperations @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getEventAnalytics(): Result<Map<String, Any>> {
        return try {
            val eventsCollection = firestore.collection(AdminConstants.EVENTS_COLLECTION)

            // Get total events count
            val totalEvents = eventsCollection.get().await().size()

            // Get events by category - using title case to match UI expectations
            val categories = mapOf(
                "Musical" to "MUSICAL",
                "Sports" to "SPORTS",
                "Food" to "FOOD",
                "Art" to "ART"
            )
            val categoryData = mutableMapOf<String, Int>()

            categories.forEach { (displayName, firestoreValue) ->
                val count = eventsCollection
                    .whereEqualTo("category", firestoreValue)
                    .get()
                    .await()
                    .size()
                categoryData[displayName] = count
            }

            // Get active events count
            val activeEvents = eventsCollection
                .whereEqualTo("status", "active")
                .get()
                .await()
                .size()

            val analytics = mapOf(
                "totalEvents" to totalEvents,
                "activeEvents" to activeEvents,
                "categoryData" to categoryData
            )

            Result.success(analytics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun batchUpdateEventStatus(eventIds: List<String>, status: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val eventsCollection = firestore.collection(AdminConstants.EVENTS_COLLECTION)

            eventIds.forEach { eventId ->
                val eventRef = eventsCollection.document(eventId)
                batch.update(eventRef, "status", status, "updatedAt", com.google.firebase.Timestamp.now())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}