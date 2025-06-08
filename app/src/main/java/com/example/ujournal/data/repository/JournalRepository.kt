// ✅ Migrated JournalRepository.kt
package com.example.ujournal.data.repository

import android.content.Context
import android.net.Uri
import com.example.ujournal.data.model.JournalEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

object JournalRepository {
    private val entries = mutableListOf<JournalEntry>()
    private val firestore = FirebaseFirestore.getInstance().apply {
        useEmulator("10.0.2.2", 8080)
    }
    private val storage = FirebaseStorage.getInstance().apply {
        useEmulator("10.0.2.2", 9199)
    }

    fun getAllEntries(): List<JournalEntry> = entries.sortedByDescending { it.date }

    fun getEntryById(id: String): JournalEntry? = entries.find { it.id == id }

    suspend fun fetchEntriesFromFirestore(): List<JournalEntry> {
        val snapshot = firestore.collection("journal_entries").get().await()
        val fetchedEntries = snapshot.documents.mapNotNull { doc ->
            try {
                JournalEntry(
                    id = doc.getString("id") ?: return@mapNotNull null,
                    content = doc.getString("content") ?: "",
                    date = doc.getDate("date") ?: Date(),
                    hasImage = doc.getBoolean("hasImage") ?: false,
                    hasLocation = doc.getBoolean("hasLocation") ?: false,
                    locationName = doc.getString("locationName") ?: "",
                    latitude = doc.getDouble("latitude"),
                    longitude = doc.getDouble("longitude"),
                    imageUrl = doc.getString("imageUrl")
                )
            } catch (e: Exception) {
                null
            }
        }
        entries.clear()
        entries.addAll(fetchedEntries)
        return entries.sortedByDescending { it.date }
    }

    suspend fun addEntry(
        context: Context,
        content: String,
        date: Date,
        hasImage: Boolean,
        hasLocation: Boolean,
        locationName: String = "",
        latitude: Double? = null,
        longitude: Double? = null,
        imageUri: Uri? = null
    ): String {
        val id = UUID.randomUUID().toString()

        val imageUrl: String? = if (hasImage && imageUri != null) {
            val storageRef = storage.reference.child("images/$id.jpg")
            val stream = context.contentResolver.openInputStream(imageUri)
            stream?.let {
                storageRef.putStream(it).await()
                it.close()
                storageRef.downloadUrl.await().toString()
            }
        } else null

        val entry = JournalEntry(
            id = id,
            content = content,
            date = date,
            hasImage = hasImage,
            hasLocation = hasLocation,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            imageUrl = imageUrl
        )

        val firestoreEntry = hashMapOf(
            "id" to id,
            "content" to content,
            "date" to date,
            "hasImage" to hasImage,
            "hasLocation" to hasLocation,
            "locationName" to locationName,
            "latitude" to latitude,
            "longitude" to longitude,
            "imageUrl" to imageUrl
        )

        firestore.collection("journal_entries").document(id).set(firestoreEntry).await()
        entries.add(entry)
        return id
    }

    suspend fun updateEntry(
        context: Context,
        id: String,
        content: String,
        hasImage: Boolean,
        hasLocation: Boolean,
        locationName: String = "",
        latitude: Double? = null,
        longitude: Double? = null,
        imageUri: Uri? = null
    ) {
        val imageUrl: String? = if (hasImage && imageUri != null) {
            val storageRef = storage.reference.child("images/$id.jpg")
            val stream = context.contentResolver.openInputStream(imageUri)
            stream?.let {
                storageRef.putStream(it).await()
                it.close()
                storageRef.downloadUrl.await().toString()
            }
        } else null

        val entryIndex = entries.indexOfFirst { it.id == id }
        if (entryIndex != -1) {
            val oldEntry = entries[entryIndex]
            val updatedEntry = oldEntry.copy(
                content = content,
                hasImage = hasImage,
                hasLocation = hasLocation,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                imageUrl = imageUrl ?: oldEntry.imageUrl
            )
            entries[entryIndex] = updatedEntry
        }

        val updates = hashMapOf<String, Any?>(
            "content" to content,
            "hasImage" to hasImage,
            "hasLocation" to hasLocation,
            "locationName" to locationName,
            "latitude" to latitude,
            "longitude" to longitude,
            "imageUrl" to imageUrl
        ).filterValues { it != null }

        firestore.collection("journal_entries").document(id).update(updates).await()
    }

    fun deleteEntry(id: String) {
        entries.removeIf { it.id == id }
        firestore.collection("journal_entries").document(id).delete()
    }

    fun clearAll() {
        entries.clear()
    }
}
