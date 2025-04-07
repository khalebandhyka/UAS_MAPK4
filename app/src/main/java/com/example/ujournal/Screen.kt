package com.example.ujournal

sealed class Screen(val route: String) {
    object Journey : Screen("journey")
    object NewEntry : Screen("new_entry")
    object EntryDetail : Screen("entry_detail")
    object EditEntry : Screen("edit_entry")
    object Calendar : Screen("calendar")
    object Media : Screen("media")
    object Atlas : Screen("atlas")
}

