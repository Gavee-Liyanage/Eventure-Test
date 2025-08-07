package com.example.eventuretest.data.remote


import com.example.eventuretest.utils.AdminConstants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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

            // Get events by category
            val categories = listOf("Musical", "Sports", "Food", "Art")
            val categoryData = mutableMapOf<String, Int>()

            categories.forEach { category ->
                val count = eventsCollection
                    .whereEqualTo("category", category)
                    .get()
                    .await()
                    .size()
                categoryData[category] = count
            }

            // Get active events count
            val activeEvents = eventsCollection
                .whereEqualTo("status", "active")
                .get()
                .await()
                .size()

            // Get recent events (last 30 days)
            val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
            val recentEvents = eventsCollection
                .whereGreaterThan("createdAt", com.google.firebase.Timestamp(thirtyDaysAgo / 1000, 0))
                .get()
                .await()
                .size()

            val analytics = mapOf(
                "totalEvents" to totalEvents,
                "activeEvents" to activeEvents,
                "recentEvents" to recentEvents,
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