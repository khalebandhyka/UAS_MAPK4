package com.example.ujournal.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.ujournal.data.model.JournalEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

object JournalRepository {
    private val entries = mutableListOf<JournalEntry>()
    private val firestore = FirebaseFirestore.getInstance().apply {
        useEmulator("10.0.2.2", 8080) // Firestore Emulator
    }

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
                    imageBase64 = doc.getString("imageBase64")
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

        val imageBase64: String? = if (hasImage && imageUri != null) {
            encodeImageToBase64(context, imageUri)
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
            imageBase64 = imageBase64
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
            "imageBase64" to imageBase64
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
        val imageBase64: String? = if (hasImage && imageUri != null) {
            encodeImageToBase64(context, imageUri)
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
                imageBase64 = imageBase64 ?: oldEntry.imageBase64
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
            "imageBase64" to imageBase64
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

    // 🔽 Tambahan: fungsi encode Base64 dari Uri gambar
    private fun encodeImageToBase64(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
}
