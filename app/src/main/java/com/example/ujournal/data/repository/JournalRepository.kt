package com.example.ujournal.data.repository

import com.example.ujournal.data.model.JournalEntry
import java.util.*

/**
 * A simple in-memory repository for journal entries.
 * In a real app, this would be backed by a database.
 */
object JournalRepository {
    private val entries = mutableListOf<JournalEntry>()

    init {
        // Add some sample entries
        val calendar = Calendar.getInstance()

        // Today's entry
        addEntry(
            content = "Today was a great day! I went for a hike and saw some beautiful scenery.",
            date = calendar.time,
            hasImage = true,
            hasLocation = true,
            locationName = "Mountain Trail"
        )

        // Yesterday's entry
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        addEntry(
            content = "Spent the day reading a new book. It's really captivating!",
            date = calendar.time,
            hasImage = false,
            hasLocation = false
        )

        // Entry from last week
        calendar.add(Calendar.DAY_OF_MONTH, -6)
        addEntry(
            content = "Visited the beach with friends. The weather was perfect!",
            date = calendar.time,
            hasImage = true,
            hasLocation = true,
            locationName = "Sunny Beach"
        )

        // Entry from last month
        calendar.add(Calendar.MONTH, -1)
        addEntry(
            content = "Started a new project today. Excited to see how it turns out!",
            date = calendar.time,
            hasImage = false,
            hasLocation = true,
            locationName = "Home Office"
        )
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
        hasImage: Boolean = false,
        hasLocation: Boolean = false,
        locationName: String = ""
    ): String {
        val id = UUID.randomUUID().toString()
        val entry = JournalEntry(
            id = id,
            content = content,
            date = date,
            hasImage = hasImage,
            hasLocation = hasLocation,
            locationName = locationName
        )
        entries.add(entry)
        return id
    }

    fun updateEntry(
        id: String,
        content: String,
        hasImage: Boolean,
        hasLocation: Boolean,
        locationName: String
    ): Boolean {
        val index = entries.indexOfFirst { it.id == id }
        if (index == -1) return false

        val updatedEntry = entries[index].copy(
            content = content,
            hasImage = hasImage,
            hasLocation = hasLocation,
            locationName = locationName
        )
        entries[index] = updatedEntry
        return true
    }

    fun deleteEntry(id: String): Boolean {
        val entry = entries.find { it.id == id } ?: return false
        return entries.remove(entry)
    }
}

