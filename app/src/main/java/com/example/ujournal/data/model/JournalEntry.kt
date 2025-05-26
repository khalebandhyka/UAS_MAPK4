package com.example.ujournal.data.model

import android.net.Uri
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
    val imageUri: Uri? = null,
    val imageUrl: String? = null
)
