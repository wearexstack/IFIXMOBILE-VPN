package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object ServerSelection : Screen("server_selection")
    object Settings : Screen("settings")
    object AdminPanel : Screen("admin_panel")
}
