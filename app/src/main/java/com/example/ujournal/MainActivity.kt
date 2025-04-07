package com.example.ujournal

import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UJournalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UJournalApp()
                }
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

    // Only show bottom bar on main app screens, not auth screens
    val showBottomBar = when {
        currentRoute == Screen.Journey.route ||
                currentRoute == Screen.Calendar.route ||
                currentRoute == Screen.Media.route ||
                currentRoute == Screen.Atlas.route -> true
        else -> false
    }

    Scaffold(
        // Remove the topBar parameter completely
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        },
        // Use safeDrawing insets to respect system UI like status bar
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Authentication flow
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

            // Main app screens with detailed implementations
            composable(Screen.Journey.route) {
                JourneyScreen(navController = navController)
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

