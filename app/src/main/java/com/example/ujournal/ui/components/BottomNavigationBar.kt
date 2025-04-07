package com.example.ujournal.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.ujournal.R
import com.example.ujournal.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Photo

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Journey") },
            label = { Text("Journey") },
            selected = currentRoute == Screen.Journey.route,
            onClick = {
                if (currentRoute != Screen.Journey.route) {
                    navController.navigate(Screen.Journey.route) {
                        popUpTo(Screen.Journey.route) { inclusive = true }
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Calendar") },
            label = { Text("Calendar") },
            selected = currentRoute == Screen.Calendar.route,
            onClick = {
                if (currentRoute != Screen.Calendar.route) {
                    navController.navigate(Screen.Calendar.route) {
                        popUpTo(Screen.Journey.route)
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Photo, contentDescription = "Media") },
            label = { Text("Media") },
            selected = currentRoute == Screen.Media.route,
            onClick = {
                if (currentRoute != Screen.Media.route) {
                    navController.navigate(Screen.Media.route) {
                        popUpTo(Screen.Journey.route)
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Explore, contentDescription = "Atlas") },
            label = { Text("Atlas") },
            selected = currentRoute == Screen.Atlas.route,
            onClick = {
                if (currentRoute != Screen.Atlas.route) {
                    navController.navigate(Screen.Atlas.route) {
                        popUpTo(Screen.Journey.route)
                    }
                }
            }
        )
    }
}

