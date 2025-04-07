package com.example.ujournal.data.model

import java.util.*

data class JournalEntry(
    val id: String,
    val content: String,
    val date: Date,
    val hasImage: Boolean = false,
    val hasLocation: Boolean = false,
    val locationName: String = ""
)

