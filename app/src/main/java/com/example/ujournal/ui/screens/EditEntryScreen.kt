package com.example.ujournal.ui.screens

import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
import com.example.ujournal.data.model.JournalEntry
import com.example.ujournal.data.repository.JournalRepository
import com.example.ujournal.ui.components.ImagePicker
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(navController: NavController, entryId: String) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var entry by remember { mutableStateOf<JournalEntry?>(null) }

    var entryContent by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Fetch entry
    LaunchedEffect(entryId) {
        entry = JournalRepository.getEntryById(entryId)
        entry?.let {
            entryContent = it.content
            showImagePicker = it.hasImage
            showLocationPicker = it.hasLocation
        }
    }

    if (entry == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Entry not found")
        }
        return
    }

    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(entry!!.date)

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
                                        hasImage = selectedImageUri != null,
                                        hasLocation = showLocationPicker,
                                        locationName = if (showLocationPicker) "Updated Location" else "",
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

            if (showImagePicker || selectedImageUri != null || entry?.imageBase64 != null) {
                Spacer(modifier = Modifier.height(16.dp))

                // Gunakan ImagePicker jika user memilih gambar baru
                ImagePicker(
                    imageUri = selectedImageUri,
                    onImageSelected = { uri ->
                        selectedImageUri = uri
                        showImagePicker = true
                    }
                )

                // Tampilkan gambar lama jika belum ada gambar baru
                if (selectedImageUri == null && entry?.imageBase64 != null) {
                    val imageBytes = Base64.decode(entry!!.imageBase64, Base64.DEFAULT)
                    val imageFile = File.createTempFile("temp", ".jpg", context.cacheDir).apply {
                        FileOutputStream(this).use { it.write(imageBytes) }
                    }
                    val tempUri = Uri.fromFile(imageFile)
                    Image(
                        painter = rememberAsyncImagePainter(tempUri),
                        contentDescription = "Saved Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            if (showLocationPicker) {
                Spacer(modifier = Modifier.height(16.dp))
                LocationPickerPlaceholder()
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
fun LocationPickerPlaceholder() {
    Card(modifier = Modifier.fillMaxWidth()) {
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
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
