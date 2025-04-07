package com.example.ujournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ujournal.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val gradientBackground = Brush.horizontalGradient(
        colors = listOf(
            Color(36 / 255f, 194 / 255f, 229 / 255f),
            Color(255 / 255f, 255 / 255f, 255 / 255f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "U-Journal",
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold
        )
    }

    LaunchedEffect(key1 = true) {
        delay(3000) // Misal 3 detik
        navController.navigate("welcome") {
            popUpTo("splash") { inclusive = true } // Supaya Splash tidak bisa di-back
        }
    }
}