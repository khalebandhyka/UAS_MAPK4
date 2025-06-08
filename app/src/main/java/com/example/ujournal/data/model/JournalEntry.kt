// ✅ Migrated JournalEntry.kt (from imageBase64 to imageUrl)
package com.example.ujournal.data.model

import java.util.*

data class JournalEntry(
    val id: String,
    val content: String,
    val date: Date,
    val hasImage: Boolean,
    val hasLocation: Boolean,
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String? = null // now stores download URL from Storage
)
