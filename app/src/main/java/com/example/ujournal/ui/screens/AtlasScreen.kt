package com.example.journeyjournal.screen

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ujournal.data.repository.JournalRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun AtlasScreen(navController: NavController) {
    val context = LocalContext.current
    val entriesWithLocation = remember {
        JournalRepository.getAllEntries().filter { it.hasLocation }
    }

    val initialPosition = if (entriesWithLocation.isNotEmpty() &&
        entriesWithLocation[0].latitude != null &&
        entriesWithLocation[0].longitude != null
    ) {
        LatLng(entriesWithLocation[0].latitude!!, entriesWithLocation[0].longitude!!)
    } else {
        LatLng(-6.2, 106.8167)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 10f)
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchedLocation by remember { mutableStateOf<LatLng?>(null) }

    // Function to search location by name
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

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Atlas") })
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Location") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    trailingIcon = {
                        Button(onClick = { isSearching = true }) {
                            Text("Go")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        LaunchedEffect(isSearching) {
            if (isSearching && searchQuery.isNotBlank()) {
                val location = searchLocation(searchQuery, context)
                location?.let {
                    searchedLocation = it
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
                isSearching = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                // Marker untuk hasil pencarian
                searchedLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Search Result",
                        snippet = searchQuery
                    )
                }

                // Marker dari journal entries
                entriesWithLocation.forEach { entry ->
                    entry.latitude?.let { lat ->
                        entry.longitude?.let { lng ->
                            Marker(
                                state = MarkerState(position = LatLng(lat, lng)),
                                title = entry.locationName,
                                snippet = entry.content
                            )
                        }
                    }
                }
            }
        }
    }
}