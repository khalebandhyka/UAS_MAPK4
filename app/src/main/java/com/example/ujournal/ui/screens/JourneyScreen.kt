package com.example.ujournal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ujournal.Screen
import com.example.ujournal.data.model.JournalEntry
import com.example.ujournal.data.repository.JournalRepository
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyScreen(navController: NavController) {
    val journalEntries = remember { JournalRepository.getAllEntries() }
    val username = "John" // This would come from user preferences or authentication
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Halo, $username") },
                actions = {
                    IconButton(onClick = { /* TODO: Implement search */ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* TODO: Navigate to profile */ }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
                    }
                },
                // Add padding to respect status bar
                modifier = Modifier.padding(top = statusBarHeight)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.NewEntry.route) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Entry")
            }
        }
    ) { innerPadding ->
        if (journalEntries.isEmpty()) {
            EmptyJourneyContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            JourneyContent(
                entries = journalEntries,
                onEntryClick = { entryId ->
                    navController.navigate("${Screen.EntryDetail.route}/$entryId")
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
fun EmptyJourneyContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No entries yet. Tap + to create your first journal entry.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun JourneyContent(
    entries: List<JournalEntry>,
    onEntryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(entries) { entry ->
            JournalEntryCard(
                entry = entry,
                onClick = { onEntryClick(entry.id) }
            )
        }
    }
}

@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(entry.date)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (entry.hasImage) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📷 Photo attached",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (entry.hasLocation) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 ${entry.locationName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

