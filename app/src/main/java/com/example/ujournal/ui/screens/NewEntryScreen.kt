package com.example.ujournal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ujournal.data.repository.JournalRepository
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.statusBars

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(navController: NavController) {
    var entryContent by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val currentDate = remember { Calendar.getInstance().time }
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(currentDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(formattedDate) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (entryContent.isNotBlank()) {
                                // Save the entry
                                val newEntryId = JournalRepository.addEntry(
                                    content = entryContent,
                                    date = currentDate,
                                    hasImage = showImagePicker,
                                    hasLocation = showLocationPicker,
                                    locationName = if (showLocationPicker) "Selected Location" else ""
                                )
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
                modifier = Modifier.padding(top = statusBarHeight)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = entryContent,
                onValueChange = { entryContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("What's on your mind today?") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showImagePicker = !showImagePicker },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showImagePicker)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Filled.Image, contentDescription = "Add Media")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Media")
                }

                Button(
                    onClick = { showLocationPicker = !showLocationPicker },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showLocationPicker)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Add Location")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Location")
                }
            }

            if (showImagePicker) {
                Spacer(modifier = Modifier.height(16.dp))
                ImagePickerPlaceholder()
            }

            if (showLocationPicker) {
                Spacer(modifier = Modifier.height(16.dp))
                LocationPickerPlaceholder()
            }
        }
    }
}

@Composable
fun ImagePickerPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Image Picker would appear here")
        }
    }
}

@Composable
fun LocationPickerPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Location Picker would appear here")
        }
    }
}

