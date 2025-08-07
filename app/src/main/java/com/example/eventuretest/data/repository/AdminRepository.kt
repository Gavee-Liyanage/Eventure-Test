package com.example.eventuretest.data.repository

import com.example.eventuretest.data.models.AdminUser
import com.example.eventuretest.utils.AdminConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val adminCollection = firestore.collection(AdminConstants.ADMIN_USERS_COLLECTION)

    suspend fun getCurrentAdmin(): Result<AdminUser?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val document = adminCollection.document(currentUser.uid).get().await()
                val admin = document.toObject(AdminUser::class.java)
                Result.success(admin)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAdminProfile(admin: AdminUser): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                adminCollection.document(currentUser.uid).set(admin).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No authenticated user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAdminProfile(adminUser: AdminUser): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val newAdmin = adminUser.copy(
                    id = currentUser.uid,
                    email = currentUser.email ?: ""
                )
                adminCollection.document(currentUser.uid).set(newAdmin).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No authenticated user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}