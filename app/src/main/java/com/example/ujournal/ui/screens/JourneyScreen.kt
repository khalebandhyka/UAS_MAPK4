package com.example.ujournal.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.webkit.Profile
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyScreen(navController: NavController) {
    val journalEntries by produceState<List<JournalEntry>>(initialValue = emptyList()) {
        value = JournalRepository.fetchEntriesFromFirestore()
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val username = currentUser?.displayName ?: "Guest"

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val photoUrl = currentUser?.photoUrl

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Halo, $username") },
                actions = {
                    IconButton(onClick = { /* TODO: Implement search */ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Profile.route)
                    }) {
                        if (photoUrl != null) {
                            Image(
                                painter = rememberImagePainter(data = photoUrl),
                                contentDescription = "Foto Profil",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                },
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
                journal_entries = journalEntries,
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
    journal_entries: List<JournalEntry>,
    onEntryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(journal_entries) { entry ->
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
