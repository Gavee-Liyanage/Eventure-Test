package com.example.eventuretest.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminFirebaseManager @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) {
    private val eventImagesRef: StorageReference = firebaseStorage.reference.child("event_images")

    suspend fun uploadEventImage(imageUri: Uri, eventId: String, imageIndex: Int): Result<String> {
        return try {
            val fileName = "${eventId}_image_${imageIndex}_${System.currentTimeMillis()}.jpg"
            val imageRef = eventImagesRef.child(fileName)

            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadMultipleEventImages(imageUris: List<Uri>, eventId: String): Result<List<String>> {
        return try {
            val uploadTasks = imageUris.mapIndexed { index, uri ->
                uploadEventImage(uri, eventId, index)
            }

            val downloadUrls = mutableListOf<String>()
            uploadTasks.forEach { result ->
                result.fold(
                    onSuccess = { url -> downloadUrls.add(url) },
                    onFailure = { throw it }
                )
            }

            Result.success(downloadUrls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEventImage(imageUrl: String): Result<Unit> {
        return try {
            val imageRef = firebaseStorage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMultipleEventImages(imageUrls: List<String>): Result<Unit> {
        return try {
            imageUrls.forEach { url ->
                deleteEventImage(url).getOrThrow()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}