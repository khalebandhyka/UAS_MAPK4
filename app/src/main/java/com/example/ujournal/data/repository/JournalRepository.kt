package com.example.ujournal.data.repository

import android.net.Uri
import com.example.ujournal.data.model.JournalEntry
import java.util.*
import kotlin.collections.ArrayList

object JournalRepository {
    private val entries = ArrayList<JournalEntry>()

    init {
        // Add some sample entries if needed
    }

    fun getAllEntries(): List<JournalEntry> {
        return entries.sortedByDescending { it.date }
    }

    fun getEntryById(id: String): JournalEntry? {
        return entries.find { it.id == id }
    }

    fun addEntry(
        content: String,
        date: Date,
        hasImage: Boolean,
        hasLocation: Boolean,
        locationName: String = "",
        imageUri: Uri? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val entry = JournalEntry(
            id = id,
            content = content,
            date = date,
            hasImage = hasImage,
            hasLocation = hasLocation,
            locationName = locationName,
            imageUri = imageUri
        )
        entries.add(entry)
        return id
    }

    fun updateEntry(
        id: String,
        content: String,
        hasImage: Boolean,
        hasLocation: Boolean,
        locationName: String = "",
        imageUri: Uri? = null
    ) {
        val index = entries.indexOfFirst { it.id == id }
        if (index != -1) {
            val oldEntry = entries[index]
            entries[index] = oldEntry.copy(
                content = content,
                hasImage = hasImage,
                hasLocation = hasLocation,
                locationName = locationName,
                imageUri = imageUri ?: oldEntry.imageUri
            )
        }
    }

    fun deleteEntry(id: String) {
        entries.removeIf { it.id == id }
    }
}
