package com.example.ujournal.data.repository

import android.net.Uri
import com.example.ujournal.data.model.JournalEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

object JournalRepository {
    private val entries = mutableListOf<JournalEntry>()
    private val firestore = FirebaseFirestore.getInstance().apply {
        useEmulator("10.0.2.2", 8080) // Untuk Firestore Emulator
    }
    private val storage = FirebaseStorage.getInstance()

    fun getAllEntries(): List<JournalEntry> {
        return entries.sortedByDescending { it.date }
    }

    fun getEntryById(id: String): JournalEntry? {
        return entries.find { it.id == id }
    }

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
                    imageUri = doc.getString("imageUri")?.let { Uri.parse(it) }
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
        var uploadedImageUrl: String? = null

        if (hasImage && imageUri != null) {
            val storageRef = storage.reference.child("journal_images/$id.jpg")
            val uploadTask = storageRef.putFile(imageUri).await()
            uploadedImageUrl = storageRef.downloadUrl.await().toString()
        }

        val entry = JournalEntry(
            id = id,
            content = content,
            date = date,
            hasImage = hasImage,
            hasLocation = hasLocation,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            imageUri = uploadedImageUrl?.let { Uri.parse(it) }
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
            "imageUri" to uploadedImageUrl
        )

        firestore.collection("journal_entries").document(id).set(firestoreEntry).await()
        entries.add(entry)
        return id
    }

    suspend fun updateEntry(
        id: String,
        content: String,
        hasImage: Boolean,
        hasLocation: Boolean,
        locationName: String = "",
        latitude: Double? = null,
        longitude: Double? = null,
        imageUri: Uri? = null
    ) {
        var uploadedImageUrl: String? = null

        if (hasImage && imageUri != null) {
            val storageRef = storage.reference.child("journal_images/$id.jpg")
            storageRef.putFile(imageUri).await()
            uploadedImageUrl = storageRef.downloadUrl.await().toString()
        }

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
                imageUri = uploadedImageUrl?.let { Uri.parse(it) } ?: oldEntry.imageUri
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
            "imageUri" to uploadedImageUrl
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
