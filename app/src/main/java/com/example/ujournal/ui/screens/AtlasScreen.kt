package com.example.ujournal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ujournal.data.repository.JournalRepository
import androidx.compose.foundation.layout.statusBars

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtlasScreen(navController: NavController) {
    val entriesWithLocation = remember {
        JournalRepository.getAllEntries().filter { it.hasLocation }
    }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Atlas") },
                modifier = Modifier.padding(top = statusBarHeight)
            )
        }
    ) { innerPadding ->
        if (entriesWithLocation.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No locations found")
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Map placeholder
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Map would be displayed here")
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Locations: ${entriesWithLocation.size}")
                        }
                    }
                }
            }
        }
    }
}

