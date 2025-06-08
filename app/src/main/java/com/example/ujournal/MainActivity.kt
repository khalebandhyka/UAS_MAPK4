package com.example.ujournal

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ujournal.ui.screens.*
import com.example.ujournal.ui.components.BottomNavigationBar
import com.example.ujournal.ui.theme.UJournalTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.Alignment
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import com.example.journeyjournal.screen.AtlasScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private var isEmulatorInitialized = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase", e)
        }

        // Initialize Firebase Emulators (only once)
        initializeFirebaseEmulators()

        setContent {
            UJournalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UJournalApp()
                }
            }
        }
    }

    private fun initializeFirebaseEmulators() {
        if (!isEmulatorInitialized) {
            try {
                // Firebase Auth Emulator
                FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
                Log.d( TAG, "Firebase Auth Emulator connected to 10.0.2.2:9099")

                FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
                Log.d(TAG, "Firebase Firestore Emulator connected to 10.0.2.2:8080")

                // Firebase Storage Emulator (untuk nanti)
                FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9198)
                Log.d(TAG, "Firebase Storage Emulator connected to 10.0.2.2:9199")

                isEmulatorInitialized = true
                Log.d(TAG, "All Firebase Emulators initialized successfully")

                // Test connection
                testFirebaseConnection()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Firebase Emulators", e)
            }
        } else {
            Log.d(TAG, "Firebase Emulators already initialized")
        }
    }

    private fun testFirebaseConnection() {
        val auth = FirebaseAuth.getInstance()
        Log.d(TAG, "Firebase Auth instance: ${auth}")
        Log.d(TAG, "Current user: ${auth.currentUser}")

        // Test dengan anonymous sign in untuk memastikan koneksi
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Firebase Auth connection test successful")
                    // Sign out immediately after test
                    auth.signOut()
                } else {
                    Log.e(TAG, "Firebase Auth connection test failed", task.exception)
                }
            }
    }
}

@Composable
fun UJournalApp() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentRoute = currentDestination?.route ?: "splash"

    val showBottomBar = when {
        currentRoute == Screen.Journey.route ||
                currentRoute == Screen.Calendar.route ||
                currentRoute == Screen.Media.route ||
                currentRoute == Screen.Atlas.route -> true
        else -> false
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen(navController)
            }
            composable("welcome") {
                WelcomeScreen(navController)
            }
            composable("signup_screen") {
                SignUpScreen(navController)
            }
            composable("login_screen") {
                LoginScreen(navController)
            }
            composable("JourneyScreen") {
                JourneyScreen(navController)
            }

            composable(Screen.Journey.route) {
                JourneyScreen(navController = navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }
            composable(Screen.NewEntry.route) {
                NewEntryScreen(navController = navController)
            }
            composable(
                route = "${Screen.EntryDetail.route}/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
                EntryDetailScreen(navController = navController, entryId = entryId)
            }
            composable(
                route = "${Screen.EditEntry.route}/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
                EditEntryScreen(navController = navController, entryId = entryId)
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(navController = navController)
            }
            composable(Screen.Media.route) {
                MediaScreen(navController = navController)
            }
            composable(Screen.Atlas.route) {
                AtlasScreen(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    UJournalTheme {
        UJournalApp()
    }
}
