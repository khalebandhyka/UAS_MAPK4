package com.example.ujournal.ui.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ujournal.data.repository.JournalRepository
import com.example.ujournal.Screen
import com.example.ujournal.ui.components.ImagePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun NewEntryScreen(navController: NavController) {
    val context = LocalContext.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var entryContent by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Menyimpan tanggal yang dipilih
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }

    // Format tanggal
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(selectedDate)

    // Menambahkan status pencarian
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-6.2, 106.8167), 10f) // Jakarta
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
                    // Tombol untuk membuka DatePickerDialog
                    IconButton(onClick = { showDatePickerDialog(context, onDateSelected = { selectedDate = it }) }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Pick Date")
                    }
                    IconButton(
                        onClick = {
                            if (entryContent.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        JournalRepository.addEntry(
                                            context = context,
                                            content = entryContent,
                                            date = selectedDate,
                                            hasImage = selectedImageUri != null,
                                            hasLocation = selectedLatLng != null,
                                            locationName = if (selectedLatLng != null) "Lokasi Ditentukan" else "",
                                            latitude = selectedLatLng?.latitude,
                                            longitude = selectedLatLng?.longitude,
                                            imageUri = selectedImageUri
                                        )
                                        snackbarHostState.showSnackbar("Jurnal Published")
                                        navController.navigate(Screen.Journey.route) {
                                            popUpTo(Screen.Journey.route) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Gagal menyimpan entri. Coba lagi.")
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Isi catatan tidak boleh kosong.")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
                modifier = Modifier.padding(top = statusBarHeight)
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
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

                selectedLatLng?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lokasi dipilih: ${it.latitude}, ${it.longitude}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

fun showDatePickerDialog(context: Context, onDateSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            onDateSelected(selectedCalendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}
