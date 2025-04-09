package com.example.ujournal.ui.screens

import android.net.Uri
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
import com.example.ujournal.ui.components.ImagePicker
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.statusBars
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(navController: NavController) {
    var entryContent by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-6.2, 106.8167), 10f) // Jakarta default
    }

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
                                val newEntryId = JournalRepository.addEntry(
                                    content = entryContent,
                                    date = currentDate,
                                    hasImage = selectedImageUri != null,
                                    hasLocation = selectedLatLng != null,
                                    locationName = if (selectedLatLng != null) "Lokasi Ditentukan" else "",
                                    latitude = selectedLatLng?.latitude,
                                    longitude = selectedLatLng?.longitude,
                                    imageUri = selectedImageUri
                                )
                                navController.popBackStack()
                            }
                        }
                    )
                    {
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
                        containerColor = if (showImagePicker || selectedImageUri != null)
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

            if (showImagePicker || selectedImageUri != null) {
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
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        selectedLatLng = latLng
                    }
                ) {
                    selectedLatLng?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Lokasi Dipilih",
                            snippet = "Klik simpan untuk menyimpan lokasi ini"
                        )
                    }
                }
                if (selectedLatLng != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lokasi dipilih: ${selectedLatLng!!.latitude}, ${selectedLatLng!!.longitude}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
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