package com.example.journeyjournal.screen

import android.annotation.SuppressLint
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Atlas") })
        }
    ) { paddingValues ->
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
                // Marker untuk journal entries
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
