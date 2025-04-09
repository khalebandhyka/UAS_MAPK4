package com.example.ujournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ujournal.Screen
import com.example.ujournal.data.model.JournalEntry
import com.example.ujournal.data.repository.JournalRepository
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.statusBars

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    val allEntries = remember { JournalRepository.getAllEntries() }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val entriesForSelectedDate = remember(selectedDate) {
        if (selectedDate == null) emptyList()
        else {
            val calendar = Calendar.getInstance().apply { time = selectedDate!! }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            allEntries.filter { entry ->
                val entryCalendar = Calendar.getInstance().apply { time = entry.date }
                entryCalendar.get(Calendar.YEAR) == year &&
                        entryCalendar.get(Calendar.MONTH) == month &&
                        entryCalendar.get(Calendar.DAY_OF_MONTH) == day
            }
        }
    }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthName = monthFormat.format(currentMonth.time)

    val daysWithEntries = remember(currentMonth) {
        val year = currentMonth.get(Calendar.YEAR)
        val month = currentMonth.get(Calendar.MONTH)

        allEntries.filter { entry ->
            val entryCalendar = Calendar.getInstance().apply { time = entry.date }
            entryCalendar.get(Calendar.YEAR) == year &&
                    entryCalendar.get(Calendar.MONTH) == month
        }.map { entry ->
            val entryCalendar = Calendar.getInstance().apply { time = entry.date }
            entryCalendar.get(Calendar.DAY_OF_MONTH)
        }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                modifier = Modifier.padding(top = statusBarHeight)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        currentMonth.add(Calendar.MONTH, -1)
                        currentMonth = currentMonth.clone() as Calendar
                        selectedDate = null
                    }
                ) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(
                    onClick = {
                        currentMonth.add(Calendar.MONTH, 1)
                        currentMonth = currentMonth.clone() as Calendar
                        selectedDate = null
                    }
                ) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CalendarGrid(
                currentMonth = currentMonth,
                daysWithEntries = daysWithEntries,
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = date
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedDate != null && entriesForSelectedDate.isNotEmpty()) {
                Text(
                    text = "Entries for ${SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(selectedDate!!)}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(entriesForSelectedDate) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    navController.navigate("${Screen.EntryDetail.route}/${entry.id}")
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(entry.date),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            } else if (selectedDate != null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No entries for this date")
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    daysWithEntries: Set<Int>,
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit
) {
    val calendar = currentMonth.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val selectedDay = if (selectedDate != null) {
        val selectedCalendar = Calendar.getInstance().apply { time = selectedDate }
        if (selectedCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
            selectedCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        } else null
    } else null

    Column {
        var dayCounter = 1
        for (week in 0 until 6) {
            if (dayCounter > daysInMonth) break

            Row(modifier = Modifier.fillMaxWidth()) {
                for (day in 0 until 7) {
                    if (week == 0 && day < firstDayOfWeek || dayCounter > daysInMonth) {
                        // Empty cell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        val currentDay = dayCounter
                        val hasEntries = daysWithEntries.contains(currentDay)
                        val isSelected = selectedDay == currentDay

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    width = if (hasEntries) 2.dp else 0.dp,
                                    color = if (hasEntries) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = CircleShape
                                )
                                .clickable {
                                    val dateCalendar = calendar.clone() as Calendar
                                    dateCalendar.set(Calendar.DAY_OF_MONTH, currentDay)
                                    onDateSelected(dateCalendar.time)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDay.toString(),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        dayCounter++
                    }
                }
            }
        }
    }
}

