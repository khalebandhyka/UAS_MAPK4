package com.example.ujournal.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val username = remember { mutableStateOf(currentUser?.displayName ?: "Tamu") }
    val profileImageUri = remember { mutableStateOf<Uri?>(currentUser?.photoUrl) }
    val newName = remember { mutableStateOf(username.value) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profileImageUri.value = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Gambar profil dalam bentuk lingkaran
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            ) {
                if (profileImageUri.value != null) {
                    Image(
                        painter = rememberImagePainter(profileImageUri.value),
                        contentDescription = "Foto Profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Foto Default",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol ubah foto profile
            TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Ubah foto profile")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input nama baru
            BasicTextField(
                value = newName.value,
                onValueChange = { newName.value = it },
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .border(1.dp, Color.Gray)
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        innerTextField()
                        if (newName.value.isEmpty()) {
                            Text("Masukkan nama baru", color = Color.Gray)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol simpan
            Button(onClick = {
                val user = FirebaseAuth.getInstance().currentUser
                val uri = profileImageUri.value

                if (uri != null && user != null) {
                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("profile_images/${user.uid}.jpg")

                    storageRef.putFile(uri)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let { throw it }
                            }
                            storageRef.downloadUrl
                        }.addOnSuccessListener { downloadUri ->
                            val updates = userProfileChangeRequest {
                                displayName = newName.value
                                photoUri = downloadUri
                            }
                            user.updateProfile(updates).addOnSuccessListener {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Profil berhasil disimpan")
                                }
                                profileImageUri.value = downloadUri
                            }
                        }.addOnFailureListener {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Gagal menyimpan foto profil")
                            }
                        }
                } else if (user != null) {
                    val updates = userProfileChangeRequest {
                        displayName = newName.value
                    }
                    user.updateProfile(updates).addOnSuccessListener {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Profil berhasil disimpan")
                        }
                    }
                }
            }) {
                Text("Simpan")
            }
        }
    }
}

