// ✅ Full Edited EditEntryScreen.kt with working GoogleMap location picker
package com.example.ujournal.ui.screens

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ujournal.data.repository.JournalRepository
import com.example.ujournal.ui.components.ImagePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(navController: NavController, entryId: String) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val entry = remember { JournalRepository.getEntryById(entryId) }
    var entryContent by remember { mutableStateOf(entry?.content ?: "") }
    var showImagePicker by remember { mutableStateOf(entry?.hasImage ?: false) }
    var showLocationPicker by remember { mutableStateOf(entry?.hasLocation ?: false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedLatLng by remember {
        mutableStateOf(entry?.let {
            if (it.latitude != null && it.longitude != null) LatLng(it.latitude, it.longitude) else null
        })
    }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng ?: LatLng(-6.2, 106.8167), 10f)
    }

    suspend fun searchLocation(query: String, context: Context): LatLng? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(query, 1)
                if (!addresses.isNullOrEmpty()) {
                    LatLng(addresses[0].latitude, addresses[0].longitude)
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    LaunchedEffect(isSearching) {
        if (isSearching && searchQuery.isNotBlank()) {
            val location = searchLocation(searchQuery, context)
            location?.let {
                selectedLatLng = it
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }
            isSearching = false
        }
    }

    if (entry == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Entry not found")
        }
        return
    }

    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(entry.date)

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
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                    IconButton(
                        onClick = {
                            if (entryContent.isNotBlank()) {
                                coroutineScope.launch {
                                    JournalRepository.updateEntry(
                                        context = context,
                                        id = entryId,
                                        content = entryContent,
                                        hasImage = selectedImageUri != null || entry.imageUrl != null,
                                        hasLocation = showLocationPicker,
                                        locationName = if (showLocationPicker && selectedLatLng != null) "Updated Location" else entry.locationName,
                                        latitude = selectedLatLng?.latitude ?: entry.latitude,
                                        longitude = selectedLatLng?.longitude ?: entry.longitude,
                                        imageUri = selectedImageUri
                                    )
                                    navController.popBackStack()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                }
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
                        containerColor = if (showImagePicker || selectedImageUri != null || entry.imageUrl != null)
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

            if (showImagePicker || selectedImageUri != null || entry.imageUrl != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ImagePicker(
                    imageUri = selectedImageUri,
                    onImageSelected = { uri ->
                        selectedImageUri = uri
                        showImagePicker = true
                    }
                )
            }

            if (showLocationPicker) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari lokasi...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            if (searchQuery.isNotBlank()) {
                                isSearching = true
                            }
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng -> selectedLatLng = latLng }
                ) {
                    selectedLatLng?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Lokasi Dipilih",
                            snippet = "Klik simpan untuk menyimpan lokasi ini"
                        )
                    }
                }

                selectedLatLng?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lokasi dipilih: ${it.latitude}, ${it.longitude}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (showDeleteConfirmation) {
            DeleteConfirmationDialog(
                onConfirm = {
                    coroutineScope.launch {
                        JournalRepository.deleteEntry(entryId)
                        showDeleteConfirmation = false
                        navController.popBackStack()
                    }
                },
                onDismiss = {
                    showDeleteConfirmation = false
                }
            )
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Entry") },
        text = { Text("Are you sure you want to delete this entry? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
