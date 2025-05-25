package com.example.ujournal.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ujournal.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    // Hapus remember dan LaunchedEffect - langsung gunakan instance Firebase Auth
    val auth = FirebaseAuth.getInstance()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Validation functions
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    // Sign up function with improved error handling
    fun performSignUp() {
        when {
            name.isBlank() -> {
                Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
            }
            email.isBlank() || !isValidEmail(email) -> {
                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            }
            pass.isBlank() || !isValidPassword(pass) -> {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
            else -> {
                isLoading = true

                // Log untuk debugging
                Log.d("SignUp", "Attempting to create user with email: $email")
                Log.d("SignUp", "Firebase Auth instance: $auth")

                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            Log.d("SignUp", "User created successfully!")
                            Log.d("SignUp", "User UID: ${user?.uid}")
                            Log.d("SignUp", "User Email: ${user?.email}")

                            // Update user profile with display name
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()

                            user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    Log.d("SignUp", "User profile updated successfully")
                                    Toast.makeText(
                                        context,
                                        "Account created successfully!\nWelcome, $name!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("JourneyScreen") {
                                        popUpTo("signup_screen") { inclusive = true }
                                    }
                                } else {
                                    Log.e("SignUp", "Failed to update profile", profileTask.exception)
                                    Toast.makeText(context, "Account created but failed to save name", Toast.LENGTH_SHORT).show()
                                    navController.navigate("JourneyScreen")
                                }
                            }
                        } else {
                            val exception = task.exception
                            val errorMessage = when (exception) {
                                is FirebaseAuthException -> {
                                    Log.e("SignUp", "Firebase Auth Error Code: ${exception.errorCode}")
                                    Log.e("SignUp", "Firebase Auth Error: ${exception.message}")
                                    when (exception.errorCode) {
                                        "ERROR_WEAK_PASSWORD" -> "Password is too weak. Please choose a stronger password."
                                        "ERROR_INVALID_EMAIL" -> "Invalid email address format."
                                        "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
                                        "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
                                        else -> "Sign up failed: ${exception.message}"
                                    }
                                }
                                else -> {
                                    Log.e("SignUp", "Unknown error: ${exception?.message}", exception)
                                    "Sign up failed: ${exception?.message ?: "Unknown error"}"
                                }
                            }
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        isLoading = false
                        Log.e("SignUp", "Sign up operation failed", exception)
                        Toast.makeText(context, "Network error: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = statusBarHeight + 40.dp,
                start = 40.dp,
                end = 40.dp,
                bottom = 40.dp
            ),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.Start
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_button)
            )
        }

        Text(
            modifier = Modifier.width(250.dp),
            text = stringResource(R.string.create_new_account),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 48.sp
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = stringResource(R.string.full_name)) },
            placeholder = { Text(text = stringResource(R.string.enter_your_name)) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() }, // Trim whitespace
            label = { Text(text = stringResource(R.string.email_address)) },
            placeholder = { Text(text = stringResource(R.string.sample_email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = email.isNotBlank() && !isValidEmail(email)
        )

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text(text = stringResource(R.string.create_password)) },
            placeholder = { Text(text = stringResource(R.string.enter_your_password)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            enabled = !isLoading,
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = pass.isNotBlank() && !isValidPassword(pass),
            supportingText = if (pass.isNotBlank() && !isValidPassword(pass)) {
                { Text("Password must be at least 6 characters") }
            } else null
        )

        Button(
            onClick = { performSignUp() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(red = 36, green = 194, blue = 229, alpha = 255),
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Creating Account...")
                }
            } else {
                Text(text = stringResource(R.string.sign_up))
            }
        }
    }
}